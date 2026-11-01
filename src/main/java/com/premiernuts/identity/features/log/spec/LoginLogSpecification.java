package com.premiernuts.identity.features.log.spec;

import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.log.dto.LogSearchDTO;
import com.premiernuts.identity.jpa.domain.auth.LoginLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class LoginLogSpecification {

    private LoginLogSpecification() {
    }

    public static Specification<LoginLog> filter(LogSearchDTO searchDTO) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchDTO == null) {
                return cb.conjunction();
            }

            if (StringHelper.isNotBlank(searchDTO.getUsername())) {
                predicates.add(cb.like(cb.lower(root.get("username")), StringHelper.toLikePattern(searchDTO.getUsername())));
            }

            if (StringHelper.isNotBlank(searchDTO.getStatus())) {
                predicates.add(cb.equal(root.get("status"), StringHelper.toUpperCase(searchDTO.getStatus())));
            }

            if (StringHelper.isNotBlank(searchDTO.getKeyword())) {
                String pattern = StringHelper.toLikePattern(searchDTO.getKeyword());
                Predicate userPredicate = cb.like(cb.lower(root.get("username")), pattern);
                Predicate ipPredicate = cb.like(cb.lower(root.get("ipAddress")), pattern);
                Predicate reasonPredicate = cb.like(cb.lower(root.get("failureReason")), pattern);
                predicates.add(cb.or(userPredicate, ipPredicate, reasonPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
