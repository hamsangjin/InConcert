package com.inconcert.domain.report.service;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.domain.report.dto.ReportDTO;
import com.inconcert.domain.report.entity.Report;
import com.inconcert.domain.report.repository.ReportRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {
    private final ReportRepository reportRepository;
    private final InfoRepository infoRepository;
    private final MatchRepository matchRepository;
    private final ReviewRepository reviewRepository;
    private final TransferRepository transferRepository;
    private final UserService userService;

    public List<ReportDTO> findAll() {
        List<Report> reportList = reportRepository.findAll();
        return reportList.stream()
                .map(report -> ReportDTO.builder()
                        .id(report.getId())
                        .type(report.getType())
                        .post(report.getPost())
                        .reporter(report.getReporter())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void report(Long postId, String categoryTitle, String type){
        Post reportPost = findPostByIdAndCategory(postId, categoryTitle);
        User reporter = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        Report report = ReportDTO.toEntity(new ReportDTO(type, reportPost, reporter));

        boolean isDuplicateReport = reportRepository.existsByReporterAndPost(reporter, reportPost);
        if(!isDuplicateReport){
            reportRepository.save(report);
        }
    }

    @Transactional
    public void deleteReportId(Long reportId){
        reportRepository.deleteById(reportId);
    }

    public ReportDTO findById(Long reportId){
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(ExceptionMessage.REPORT_NOT_FOUND.getMessage()));

        return ReportDTO.builder()
                .id(report.getId())
                .type(report.getType())
                .reporter(report.getReporter())
                .post(report.getPost())
                .build();
    }

    @Transactional
    public void reportResult(Long reportId, ReportDTO reportDTO){
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(ExceptionMessage.REPORT_NOT_FOUND.getMessage()));

        Post post = report.getPost();
        User postUser = post.getUser();

        switch (reportDTO.getResult()){
            case "7day":
                postUser.updateBanDate(LocalDate.now().plusDays(7));
                break;
            case "30day":
                postUser.updateBanDate(LocalDate.now().plusDays(30));
                break;
            case "90day":
                postUser.updateBanDate(LocalDate.now().plusDays(90));
                break;
            case "1year":
                postUser.updateBanDate(LocalDate.now().plusYears(1));
                break;
            case "permanentBan":
                postUser.updateBanDate(LocalDate.now().plusYears(1000));
                break;
        }

        // 신고 삭제 처리
        deleteReportId(reportId);
        // 게시글 삭제 처리
        getRepositoryByCategoryTitle(post.getPostCategory().getCategory().getTitle()).delete(post);
        // 신고 대상 유저 ban_date 저장
        userService.updateUser(postUser);
    }

    private Post findPostByIdAndCategory(Long postId, String categoryTitle) {
        return getRepositoryByCategoryTitle(categoryTitle).findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    private JpaRepository<Post, Long> getRepositoryByCategoryTitle(String categoryTitle) {
        return switch (categoryTitle.toLowerCase()) {
            case "info" -> infoRepository;
            case "match" -> matchRepository;
            case "review" -> reviewRepository;
            case "transfer" -> transferRepository;
            default -> throw new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage());
        };
    }
}