package com.premiernuts.identity.jpa.domain.role;

import com.premiernuts.identity.jpa.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "M_ROLE")
@Getter
@Setter
public class Role extends BaseEntity {

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "level", nullable = false)
    private Integer level = 99;

    /**
     * Quy tắc phân cấp vai trò (Level Hierarchy):
     * - Cấp bậc cao hơn (level nhỏ hơn) quản lý cấp bậc thấp hơn (level lớn hơn).
     * - Không cho phép quản lý vai trò đồng cấp hoặc cao hơn mình.
     */
    public static boolean canManage(Integer actorLevel, Integer targetLevel) {
        if (actorLevel == null || targetLevel == null) {
            return false;
        }
        return targetLevel > actorLevel;
    }
}
