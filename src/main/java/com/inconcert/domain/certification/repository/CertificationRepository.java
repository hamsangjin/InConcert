package com.inconcert.domain.certification.repository;

import com.inconcert.domain.certification.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Certification findByUsername(String username);
}
