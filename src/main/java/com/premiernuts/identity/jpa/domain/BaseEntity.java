package com.premiernuts.identity.jpa.domain;

import com.premiernuts.identity.core.enums.EStatus;
import com.premiernuts.identity.core.utils.DateTimeHelper;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = DateTimeHelper.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = DateTimeHelper.now();
        }
        if (this.status == null) {
            this.status = EStatus.ACTIVE.name();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = DateTimeHelper.now();
    }
}
