package com.premiernuts.identity.features.user.service;

import com.premiernuts.identity.features.user.dto.ChangePasswordDTO;
import com.premiernuts.identity.features.user.dto.CreateUserDTO;
import com.premiernuts.identity.features.user.dto.UpdateStatusDTO;
import com.premiernuts.identity.features.user.dto.UpdateUserDTO;
import com.premiernuts.identity.features.user.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserDTO> getUsers(Long companyId, String keyword, String status, Long departmentId, Pageable pageable);

    UserDTO getUserById(Long id);

    UserDTO createUser(CreateUserDTO request, Long defaultCompanyId);

    UserDTO updateUser(Long id, UpdateUserDTO request);

    void updateStatus(Long id, UpdateStatusDTO request);

    void changePassword(Long userId, ChangePasswordDTO request);

    void resetPassword(Long id, String newPassword);
}
