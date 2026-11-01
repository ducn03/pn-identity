package com.premiernuts.identity.jpa.repository.role;

import com.premiernuts.identity.jpa.domain.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    List<Role> findAllByIdIn(List<Long> ids);

    List<Role> findAllByLevelGreaterThan(Integer level);

    @Query("SELECT MIN(r.level) FROM Role r WHERE r.status = 'ACTIVE'")
    Optional<Integer> findMinRoleLevel();
}
