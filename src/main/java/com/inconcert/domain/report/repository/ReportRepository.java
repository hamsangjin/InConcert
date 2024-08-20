package com.inconcert.domain.report.repository;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.report.entity.Report;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndPost(User reporter, Post post);
}
