package com.premiernuts.identity.jpa.repository.role;

import com.premiernuts.identity.jpa.domain.role.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findAllByRoleId(Long roleId);

    List<RolePermission> findAllByRoleIdIn(List<Long> roleIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM t_role_permission WHERE role_id = :roleId", nativeQuery = true)
    void deleteAllByRoleId(@Param("roleId") Long roleId);
}
