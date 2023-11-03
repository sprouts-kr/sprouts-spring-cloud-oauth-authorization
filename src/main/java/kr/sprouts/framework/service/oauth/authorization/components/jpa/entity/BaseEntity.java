package kr.sprouts.framework.service.oauth.authorization.components.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import kr.sprouts.framework.service.oauth.authorization.components.jpa.value.SqlDateTime;
import lombok.Getter;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;
    @CreatedBy
    @Column(updatable = false, length = 36)
    @Type(type = "uuid-char")
    private UUID createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedOn;
    @LastModifiedBy
    @Column(length = 36)
    @Type(type = "uuid-char")
    private UUID lastModifiedBy;

    @Column(nullable = false)
    private boolean deleted = Boolean.FALSE;
    @Column(nullable = false)
    private LocalDateTime deletedOn = SqlDateTime.MAX;
}
