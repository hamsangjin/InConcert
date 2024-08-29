package com.inconcert.domain.certification.entity;

import com.inconcert.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certifications")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(name = "certification_number")
    private String certificationNumber;

    private String username;
}
