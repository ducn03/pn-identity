package com.premiernuts.identity.features.user.service.impl;

import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
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
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.domain.user.User;
import com.premiernuts.identity.jpa.domain.user.UserRole;
import com.premiernuts.identity.jpa.repository.company.DepartmentRepository;
import com.premiernuts.identity.jpa.repository.role.RoleRepository;
import com.premiernuts.identity.jpa.repository.user.UserRepository;
import com.premiernuts.identity.jpa.repository.user.UserRoleRepository;
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
    private final UserRoleRepository userRoleRepository;
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

        // Tối ưu batch fetch roles
        List<Long> userIds = page.getContent().stream().map(User::getId).toList();
        List<UserRole> allUserRoles = userRoleRepository.findAllByUserIdIn(userIds);
        List<Long> roleIds = allUserRoles.stream().map(UserRole::getRoleId).distinct().toList();

        Map<Long, Role> roleMap = new HashMap<>();
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllByIdIn(roleIds);
            for (Role r : roles) {
                roleMap.put(r.getId(), r);
            }
        }

        Map<Long, List<UserRole>> userRoleMap = allUserRoles.stream()
                .collect(Collectors.groupingBy(UserRole::getUserId));

        return page.map(user -> {
            List<UserRole> userRoles = userRoleMap.getOrDefault(user.getId(), Collections.emptyList());
            List<Long> userRoleIds = userRoles.stream().map(UserRole::getRoleId).toList();
            List<String> roleCodes = new ArrayList<>();
            for (Long rId : userRoleIds) {
                if (roleMap.containsKey(rId)) {
                    roleCodes.add(roleMap.get(rId).getCode());
                }
            }

            String deptName = null;
            if (user.getDepartmentId() != null) {
                deptName = deptNameMap.get(user.getDepartmentId());
            }

            return userMapper.toDTO(user, deptName, roleCodes, userRoleIds);
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

        List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).toList();
        List<String> roleCodes = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            List<Role> roles = roleRepository.findAllByIdIn(roleIds);
            for (Role r : roles) {
                roleCodes.add(r.getCode());
            }
        }

        return userMapper.toDTO(user, deptName, roleCodes, roleIds);
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

        User user = userMapper.toEntity(request, defaultCompanyId);
        user = userRepository.save(user);

        // Lưu vai trò
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
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

        userMapper.updateEntity(user, request);
        user = userRepository.save(user);

        // Cập nhật vai trò nếu có truyền vào
        if (request.getRoleIds() != null) {
            userRoleRepository.deleteAllByUserId(user.getId());
            for (Long roleId : request.getRoleIds()) {
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
            }
        }

        return getUserById(user.getId());
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
