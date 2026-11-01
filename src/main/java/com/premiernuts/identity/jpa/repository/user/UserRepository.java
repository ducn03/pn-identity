package com.premiernuts.identity.jpa.repository.user;

import com.premiernuts.identity.core.security.UserPrincipal;
import com.premiernuts.identity.jpa.domain.role.Role;
import com.premiernuts.identity.jpa.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<User> findAllByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT r.level FROM User u, Role r WHERE u.id = :userId AND u.roleId = r.id")
    Optional<Integer> findRoleLevelByUserId(@Param("userId") Long userId);

    default Integer resolveActorLevel(UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return 99;
        }
        Optional<Integer> levelOpt = findRoleLevelByUserId(principal.getUserId());
        if (levelOpt.isPresent()) {
            return levelOpt.get();
        }
        return 99;
    }
}
