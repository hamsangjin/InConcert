package com.inconcert.domain.crawling.repository;

import com.inconcert.domain.crawling.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    Performance findTopByOrderByIdDesc();
}