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
     * Quy tắc cấp bậc:
     * - SuperAdmin (level 1) và Admin (level 2) toàn quyền quản lý.
     * - Các cấp khác: chỉ được quản lý cấp bậc thấp hơn mình (level số lớn hơn).
     */
    public static boolean canManage(Integer actorLevel, Integer targetLevel) {
        if (actorLevel == null) {
            return false;
        }
        if (actorLevel <= 2) {
            return true;
        }
        if (targetLevel == null) {
            return true;
        }
        return targetLevel > actorLevel;
    }
}
