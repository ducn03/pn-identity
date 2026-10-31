package com.premiernuts.identity.jpa.repository.role;

import com.premiernuts.identity.jpa.domain.role.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findAllByRoleId(Long roleId);

    void deleteAllByRoleId(Long roleId);
}
