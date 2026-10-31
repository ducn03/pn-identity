package com.premiernuts.identity.jpa.domain.auth;

import com.premiernuts.identity.jpa.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "T_TOKEN")
@Getter
@Setter
public class Token extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 512)
    private String token;

    @Column(name = "token_type", nullable = false, length = 20)
    private String tokenType;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;
}
