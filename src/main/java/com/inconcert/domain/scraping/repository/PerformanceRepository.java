package com.inconcert.domain.scraping.repository;

import com.inconcert.domain.scraping.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Boolean existsByTitle(String title);
}