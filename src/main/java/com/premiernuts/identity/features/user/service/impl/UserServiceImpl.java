package com.premiernuts.identity.features.user.service.impl;

import com.premiernuts.identity.core.constants.CommonConstants;
import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.exception.ErrorCodes;
import com.premiernuts.identity.core.exception.PnException;
import com.premiernuts.identity.core.utils.PasswordHelper;
import com.premiernuts.identity.features.user.dto.ChangePasswordDTO;
import com.premiernuts.identity.features.user.dto.CreateUserDTO;
import com.premiernuts.identity.features.user.dto.UpdateStatusDTO;
import com.premiernuts.identity.features.user.dto.UpdateUserDTO;
import com.premiernuts.identity.features.user.dto.UserDTO;
import com.premiernuts.identity.features.user.service.UserService;
import com.premiernuts.identity.jpa.domain.company.Department;
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.domain.user.User;
import com.premiernuts.identity.jpa.domain.user.UserRole;
import com.premiernuts.identity.jpa.repository.auth.TokenRepository;
import com.premiernuts.identity.jpa.repository.company.DepartmentRepository;
import com.premiernuts.identity.jpa.repository.role.RoleRepository;
import com.premiernuts.identity.jpa.repository.user.UserRepository;
import com.premiernuts.identity.jpa.repository.user.UserRoleRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final TokenRepository tokenRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(Long companyId, String keyword, String status, Long departmentId, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (companyId != null) {
                predicates.add(cb.equal(root.get("companyId"), companyId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("departmentId"), departmentId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate pUsername = cb.like(cb.lower(root.get("username")), pattern);
                Predicate pFullName = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate pEmail = cb.like(cb.lower(root.get("email")), pattern);
                Predicate pPhone = cb.like(cb.lower(root.get("phone")), pattern);
                predicates.add(cb.or(pUsername, pFullName, pEmail, pPhone));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> page = userRepository.findAll(spec, pageable);
        if (page.isEmpty()) {
            return page.map(u -> null);
        }

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

            return toDTO(user, deptName, roleCodes, userRoleIds);
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

        return toDTO(user, deptName, roleCodes, roleIds);
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserDTO request, Long defaultCompanyId) {
        String username = request.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new PnException(ErrorCodes.USER.USERNAME_ALREADY_EXISTS);
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(email)) {
                throw new PnException(ErrorCodes.USER.EMAIL_ALREADY_EXISTS);
            }
        }

        Long companyId = defaultCompanyId;
        if (request.getCompanyId() != null) {
            companyId = request.getCompanyId();
        }
        if (companyId == null) {
            companyId = CommonConstants.DEFAULT_COMPANY_ID;
        }

        User user = new User();
        user.setCompanyId(companyId);
        user.setDepartmentId(request.getDepartmentId());
        user.setUsername(username);
        user.setPasswordHash(PasswordHelper.hash(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        user.setStatus(EStatus.ACTIVE.name());

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

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String email = request.getEmail().trim().toLowerCase();
            Optional<User> existing = userRepository.findByEmail(email);
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                throw new PnException(ErrorCodes.USER.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(email);
        }

        user.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        user.setDepartmentId(request.getDepartmentId());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

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

        user.setStatus(request.getStatus().trim().toUpperCase());
        userRepository.save(user);

        // Nếu trạng thái khác ACTIVE -> thu hồi toàn bộ token đăng nhập
        if (!EStatus.ACTIVE.name().equalsIgnoreCase(request.getStatus())) {
            tokenRepository.deleteAllByUserId(user.getId());
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
        tokenRepository.deleteAllByUserId(user.getId());
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

        tokenRepository.deleteAllByUserId(user.getId());
        log.info("Admin reset password for user: {}", user.getUsername());
    }

    private UserDTO toDTO(User user, String deptName, List<String> roleCodes, List<Long> roleIds) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setCompanyId(user.getCompanyId());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setDepartmentName(deptName);
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRoles(roleCodes);
        dto.setRoleIds(roleIds);
        return dto;
    }
}
