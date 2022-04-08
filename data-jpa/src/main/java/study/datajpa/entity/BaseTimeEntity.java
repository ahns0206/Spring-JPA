package study.datajpa.entity;

import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

// Auditing Spring Data JPA로 해결
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class BaseTimeEntity {

    @CreatedBy // auditorProvied 값
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy // auditorProvied 값
    private String lastModifiedBy;
}
