package com.inconcert.domain.certification.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certifications")
@Getter
@NoArgsConstructor
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(name = "certification_number")
    private String certificationNumber;

    private String username;

    public Certification(String email, String certificationNumber, String username) {
        this.email = email;
        this.certificationNumber = certificationNumber;
        this.username = username;
    }
}
