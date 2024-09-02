package com.inconcert.domain.report.repository;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.report.dto.ReportDTO;
import com.inconcert.domain.report.entity.Report;
import com.inconcert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT new com.inconcert.domain.report.dto.ReportDTO(r.id, r.type, r.post, r.reporter) " +
            "FROM Report r")
    List<ReportDTO> getReportDTOs();

    @Query("SELECT new com.inconcert.domain.report.dto.ReportDTO(r.id, r.type, r.post, r.reporter) " +
            "FROM Report r " +
            "WHERE r.id = :reportId")
    ReportDTO getReportDTOByReportId(@Param("reportId") Long reportId);

    boolean existsByReporterAndPost(User reporter, Post post);
}