package com.premiernuts.identity.features.user.service.impl;

import com.premiernuts.identity.core.enums.ERole;
import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.PasswordHelper;
import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.auth.service.TokenStorageService;
import com.premiernuts.identity.features.user.dto.ChangePasswordDTO;
import com.premiernuts.identity.features.user.dto.CreateUserDTO;
import com.premiernuts.identity.features.user.dto.UpdateStatusDTO;
import com.premiernuts.identity.features.user.dto.UpdateUserDTO;
import com.premiernuts.identity.features.user.dto.UserDTO;
import com.premiernuts.identity.features.user.dto.UserSearchDTO;
import com.premiernuts.identity.features.user.mapper.UserMapper;
import com.premiernuts.identity.features.user.service.UserService;
import com.premiernuts.identity.jpa.domain.company.Department;
import com.premiernuts.identity.jpa.domain.role.Permission;
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.domain.role.RolePermission;
import com.premiernuts.identity.jpa.domain.user.User;
import com.premiernuts.identity.jpa.domain.user.UserPermission;
import com.premiernuts.identity.jpa.repository.company.DepartmentRepository;
import com.premiernuts.identity.jpa.repository.role.PermissionRepository;
import com.premiernuts.identity.jpa.repository.role.RolePermissionRepository;
import com.premiernuts.identity.jpa.repository.role.RoleRepository;
import com.premiernuts.identity.jpa.repository.user.UserPermissionRepository;
import com.premiernuts.identity.jpa.repository.user.UserRepository;
import com.premiernuts.identity.jpa.repository.user.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final TokenStorageService tokenStorageService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(UserSearchDTO searchDTO, Pageable pageable) {
        Page<User> page = userRepository.findAll(UserSpecification.filter(searchDTO), pageable);
        if (page.isEmpty()) {
            return page.map(u -> null);
        }

        // Tối ưu batch fetch departments
        List<Long> deptIds = page.getContent().stream()
                .map(User::getDepartmentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        Map<Long, String> deptNameMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            List<Department> departments = departmentRepository.findAllById(deptIds);
            for (Department d : departments) {
                deptNameMap.put(d.getId(), d.getName());
            }
        }

        // Tối ưu batch fetch roles từ T_USER.role_id
        List<Long> roleIds = page.getContent().stream()
                .map(User::getRoleId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        Map<Long, Role> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllByIdIn(roleIds);
            for (Role r : roles) {
                roleMap.put(r.getId(), r);
            }
        }

        // Tối ưu batch fetch user permissions từ T_USER_PERMISSION
        List<Long> userIds = page.getContent().stream().map(User::getId).toList();
        List<UserPermission> allUserPerms = userPermissionRepository.findAllByUserIdIn(userIds);
        List<Long> allPermIds = allUserPerms.stream().map(UserPermission::getPermissionId).distinct().toList();
        Map<Long, Permission> permMap = new HashMap<>();
        if (!allPermIds.isEmpty()) {
            List<Permission> perms = permissionRepository.findAllById(allPermIds);
            for (Permission p : perms) {
                permMap.put(p.getId(), p);
            }
        }
        Map<Long, List<UserPermission>> userPermMap = allUserPerms.stream()
                .collect(Collectors.groupingBy(UserPermission::getUserId));

        return page.map(user -> {
            Long primaryRoleId = user.getRoleId();
            String primaryRoleCode = null;
            String primaryRoleName = null;
            List<String> roleCodes = new ArrayList<>();
            List<Long> userRoleIds = new ArrayList<>();

            if (primaryRoleId != null && roleMap.containsKey(primaryRoleId)) {
                Role r = roleMap.get(primaryRoleId);
                primaryRoleCode = r.getCode();
                primaryRoleName = r.getName();
                roleCodes.add(primaryRoleCode);
                userRoleIds.add(primaryRoleId);
            }

            String deptName = null;
            if (user.getDepartmentId() != null) {
                deptName = deptNameMap.get(user.getDepartmentId());
            }

            List<UserPermission> uPerms = userPermMap.getOrDefault(user.getId(), Collections.emptyList());
            List<Long> uPermIds = new ArrayList<>();
            List<String> uPermCodes = new ArrayList<>();
            for (UserPermission up : uPerms) {
                uPermIds.add(up.getPermissionId());
                if (permMap.containsKey(up.getPermissionId())) {
                    uPermCodes.add(permMap.get(up.getPermissionId()).getCode());
                }
            }

            return userMapper.toDTO(
                    user,
                    deptName,
                    primaryRoleId,
                    primaryRoleCode,
                    primaryRoleName,
                    roleCodes,
                    userRoleIds,
                    uPermIds,
                    uPermCodes
            );
        });
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.USER.USER_NOT_FOUND);
        }
        User user = userOpt.get();

        String deptName = null;
        if (user.getDepartmentId() != null) {
            Optional<Department> deptOpt = departmentRepository.findById(user.getDepartmentId());
            if (deptOpt.isPresent()) {
                deptName = deptOpt.get().getName();
            }
        }

        Long primaryRoleId = user.getRoleId();
        String primaryRoleCode = null;
        String primaryRoleName = null;
        List<String> roleCodes = new ArrayList<>();
        List<Long> roleIds = new ArrayList<>();

        if (primaryRoleId != null) {
            Optional<Role> roleOpt = roleRepository.findById(primaryRoleId);
            if (roleOpt.isPresent()) {
                Role r = roleOpt.get();
                primaryRoleCode = r.getCode();
                primaryRoleName = r.getName();
                roleCodes.add(primaryRoleCode);
                roleIds.add(primaryRoleId);
            }
        }

        // Lấy trực tiếp permissions từ T_USER_PERMISSION
        List<UserPermission> userPerms = userPermissionRepository.findAllByUserId(user.getId());
        List<Long> permIds = userPerms.stream().map(UserPermission::getPermissionId).toList();
        List<String> permCodes = new ArrayList<>();
        if (!permIds.isEmpty()) {
            List<Permission> perms = permissionRepository.findAllById(permIds);
            for (Permission p : perms) {
                permCodes.add(p.getCode());
            }
        }

        return userMapper.toDTO(
                user,
                deptName,
                primaryRoleId,
                primaryRoleCode,
                primaryRoleName,
                roleCodes,
                roleIds,
                permIds,
                permCodes
        );
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO request, Long defaultCompanyId) {
        String username = StringHelper.toLowerCase(request.getUsername());
        if (userRepository.existsByUsername(username)) {
            throw new PnException(ErrorCodes.USER.USERNAME_ALREADY_EXISTS);
        }

        if (StringHelper.isNotBlank(request.getEmail())) {
            String email = StringHelper.toLowerCase(request.getEmail());
            if (userRepository.existsByEmail(email)) {
                throw new PnException(ErrorCodes.USER.EMAIL_ALREADY_EXISTS);
            }
        }

        Long selectedRoleId = request.getRoleId();
        if (selectedRoleId == null && request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            selectedRoleId = request.getRoleIds().get(0);
        }

        // Chỉ Admin mới có quyền gán quyền Admin
        validateAdminRoleAssignment(selectedRoleId);

        User user = userMapper.toEntity(request, defaultCompanyId);
        user.setRoleId(selectedRoleId);
        user = userRepository.save(user);

        // Lưu permissions trực tiếp vào T_USER_PERMISSION
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            // Quản trị viên tùy biến danh sách quyền cho nhân viên
            for (Long pId : request.getPermissionIds()) {
                UserPermission up = new UserPermission();
                up.setUserId(user.getId());
                up.setPermissionId(pId);
                userPermissionRepository.save(up);
            }
        } else if (selectedRoleId != null) {
            // Tự động sao chép danh sách quyền mặc định từ Role Template
            List<RolePermission> templatePerms = rolePermissionRepository.findAllByRoleId(selectedRoleId);
            for (RolePermission rp : templatePerms) {
                UserPermission up = new UserPermission();
                up.setUserId(user.getId());
                up.setPermissionId(rp.getPermissionId());
                userPermissionRepository.save(up);
            }
        }

        return getUserById(user.getId());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserDTO request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.USER.USER_NOT_FOUND);
        }
        User user = userOpt.get();

        if (StringHelper.isNotBlank(request.getEmail())) {
            String email = StringHelper.toLowerCase(request.getEmail());
            Optional<User> existing = userRepository.findByEmail(email);
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                throw new PnException(ErrorCodes.USER.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(email);
        }

        Long targetRoleId = request.getRoleId();
        if (targetRoleId == null && request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            targetRoleId = request.getRoleIds().get(0);
        }

        validateAdminRoleAssignment(targetRoleId);

        userMapper.updateEntity(user, request);
        if (targetRoleId != null) {
            user.setRoleId(targetRoleId);
        }
        user = userRepository.save(user);

        // Cập nhật T_USER_PERMISSION nếu có truyền danh sách mới
        if (request.getPermissionIds() != null) {
            userPermissionRepository.deleteAllByUserId(user.getId());
            for (Long pId : request.getPermissionIds()) {
                UserPermission up = new UserPermission();
                up.setUserId(user.getId());
                up.setPermissionId(pId);
                userPermissionRepository.save(up);
            }
        }

        return getUserById(user.getId());
    }

    private void validateAdminRoleAssignment(Long roleId) {
        if (roleId == null) {
            return;
        }

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            return;
        }

        Role role = roleOpt.get();
        boolean isAdminRole = ERole.ROLE_ADMIN.name().equalsIgnoreCase(role.getCode())
                || ERole.SUPER_ADMIN.name().equalsIgnoreCase(role.getCode());

        if (isAdminRole) {
            UserPrincipal principal = UserContext.get();
            if (principal == null || (!principal.hasRole(ERole.ROLE_ADMIN.name()) && !principal.hasRole(ERole.SUPER_ADMIN.name()))) {
                throw new PnException(ErrorCodes.FORBIDDEN.ROLE_NOT_ALLOWED);
            }
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UpdateStatusDTO request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.USER.USER_NOT_FOUND);
        }
        User user = userOpt.get();

        user.setStatus(StringHelper.toUpperCase(request.getStatus()));
        userRepository.save(user);

        // Nếu trạng thái khác ACTIVE -> thu hồi toàn bộ token qua TokenStorageService
        if (!EStatus.ACTIVE.name().equalsIgnoreCase(request.getStatus())) {
            tokenStorageService.revokeAllUserTokens(user.getId());
            log.info("Revoked active tokens for user id {} due to status change to {}", user.getId(), request.getStatus());
        }
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDTO request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.USER.USER_NOT_FOUND);
        }
        User user = userOpt.get();

        if (!PasswordHelper.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new PnException(ErrorCodes.AUTH.PASSWORD_INCORRECT);
        }

        user.setPasswordHash(PasswordHelper.hash(request.getNewPassword()));
        userRepository.save(user);

        // Đổi mật khẩu thành công thì hủy các token cũ để bắt buộc đăng nhập lại
        tokenStorageService.revokeAllUserTokens(user.getId());
        log.info("Password changed and active sessions revoked for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            throw new PnException(ErrorCodes.USER.USER_NOT_FOUND);
        }
        User user = userOpt.get();

        user.setPasswordHash(PasswordHelper.hash(newPassword));
        userRepository.save(user);

        tokenStorageService.revokeAllUserTokens(user.getId());
        log.info("Admin reset password for user: {}", user.getUsername());
    }
}

