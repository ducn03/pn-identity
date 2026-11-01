package com.premiernuts.identity.features.user.spec;

import com.premiernuts.identity.core.utils.StringHelper;
import com.premiernuts.identity.features.user.dto.UserSearchDTO;
import com.premiernuts.identity.jpa.domain.user.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> filter(UserSearchDTO searchDTO) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchDTO == null) {
                return cb.conjunction();
            }

            if (searchDTO.getCompanyId() != null) {
                predicates.add(cb.equal(root.get("companyId"), searchDTO.getCompanyId()));
            }

            if (StringHelper.isNotBlank(searchDTO.getStatus())) {
                predicates.add(cb.equal(root.get("status"), StringHelper.toUpperCase(searchDTO.getStatus())));
            }

            if (searchDTO.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), searchDTO.getDepartmentId()));
            }

            if (StringHelper.isNotBlank(searchDTO.getKeyword())) {
                String pattern = StringHelper.toLikePattern(searchDTO.getKeyword());
                Predicate pUsername = cb.like(cb.lower(root.get("username")), pattern);
                Predicate pFullName = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate pEmail = cb.like(cb.lower(root.get("email")), pattern);
                Predicate pPhone = cb.like(cb.lower(root.get("phone")), pattern);
                predicates.add(cb.or(pUsername, pFullName, pEmail, pPhone));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
