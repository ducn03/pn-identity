package com.premiernuts.identity.features.user.controller;

import com.premiernuts.identity.core.aop.LogActivity;
import com.premiernuts.identity.core.constants.CommonConstants;
import com.premiernuts.identity.core.dto.PagingDTO;
import com.premiernuts.identity.core.dto.ResponseData;
import com.premiernuts.identity.core.security.UserContext;
import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.core.utils.MessageHelper;
import com.premiernuts.identity.core.utils.ResponseUtils;
import com.premiernuts.identity.features.user.dto.ChangePasswordDTO;
import com.premiernuts.identity.features.user.dto.CreateUserDTO;
import com.premiernuts.identity.features.user.dto.UpdateStatusDTO;
import com.premiernuts.identity.features.user.dto.UpdateUserDTO;
import com.premiernuts.identity.features.user.dto.UserDTO;
import com.premiernuts.identity.features.user.service.UserService;
import com.premiernuts.identity.features.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserValidator userValidator;

    @GetMapping
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<List<UserDTO>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_INDEX) int page,
            @RequestParam(defaultValue = CommonConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        UserPrincipal principal = UserContext.get();
        Long companyId = null;
        if (principal != null) {
            companyId = principal.getCompanyId();
        }

        int pageIndex = Math.max(1, page) - 1;
        int pageSize = Math.max(1, Math.min(CommonConstants.MAX_PAGE_SIZE, size));
        PageRequest pageRequest = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<UserDTO> userPage = userService.getUsers(companyId, keyword, status, departmentId, pageRequest);

        PagingDTO pagingDTO = new PagingDTO();
        pagingDTO.update(userPage);

        return ResponseUtils.toSuccessArray(userPage.getContent(), pagingDTO);
    }

    @GetMapping("/{id}")
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseUtils.toSuccess(user);
    }

    @PostMapping
    @LogActivity(requireAuth = true, roles = {"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResponseData<UserDTO>> createUser(@RequestBody CreateUserDTO request) {
        userValidator.validateCreate(request);

        UserPrincipal principal = UserContext.get();
        Long companyId = CommonConstants.DEFAULT_COMPANY_ID;
        if (principal != null && principal.getCompanyId() != null) {
            companyId = principal.getCompanyId();
        }

        UserDTO created = userService.createUser(request, companyId);
        return ResponseUtils.toSuccess(created);
    }

    @PutMapping("/{id}")
    @LogActivity(requireAuth = true, roles = {"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResponseData<UserDTO>> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserDTO request
    ) {
        userValidator.validateUpdate(request);
        UserDTO updated = userService.updateUser(id, request);
        return ResponseUtils.toSuccess(updated);
    }

    @PatchMapping("/{id}/status")
    @LogActivity(requireAuth = true, roles = {"SUPER_ADMIN", "ROLE_ADMIN"})
    public ResponseEntity<ResponseData<String>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusDTO request
    ) {
        userValidator.validateUpdateStatus(request);
        userService.updateStatus(id, request);
        return ResponseUtils.toSuccess(MessageHelper.getMessage(CommonConstants.MSG_USER_STATUS_UPDATED));
    }

    @PostMapping("/change-password")
    @LogActivity(requireAuth = true)
    public ResponseEntity<ResponseData<String>> changePassword(
            @RequestBody ChangePasswordDTO request
    ) {
        userValidator.validateChangePassword(request);
        UserPrincipal principal = UserContext.get();
        if (principal != null) {
            userService.changePassword(principal.getUserId(), request);
        }
        return ResponseUtils.toSuccess(MessageHelper.getMessage(CommonConstants.MSG_PASSWORD_CHANGED));
    }
}
