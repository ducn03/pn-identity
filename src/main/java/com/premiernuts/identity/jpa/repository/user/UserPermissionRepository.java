package com.premiernuts.identity.jpa.repository.user;

import com.premiernuts.identity.jpa.domain.user.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    List<UserPermission> findAllByUserId(Long userId);

    List<UserPermission> findAllByUserIdIn(List<Long> userIds);

    void deleteAllByUserId(Long userId);
}
