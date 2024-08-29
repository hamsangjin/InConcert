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
import com.inconcert.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final InfoRepository infoRepository;
    private final MatchRepository matchRepository;
    private final ReviewRepository reviewRepository;
    private final TransferRepository transferRepository;
    private final UserService userService;

    // 신고 목록 불러오기
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportDTOAll() {
        return reportRepository.getReportDTOs();
    }

    // 신고하기
    @Transactional
    public void report(Long postId, String categoryTitle, String type){
        Post reportPost = getPostByIdAndCategoryTitle(postId, categoryTitle);
        User reporter = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));

        // 한 유저가 같은 게시글을 또 신고했을 경우에는 무시
        boolean isDuplicateReport = reportRepository.existsByReporterAndPost(reporter, reportPost);
        if(!isDuplicateReport){
            Report report = ReportDTO.toEntity(new ReportDTO(type, reportPost, reporter));
            reportRepository.save(report);
        }
    }

    // 신고 반려하기
    @Transactional
    public void deleteReportId(Long reportId){
        reportRepository.deleteById(reportId);
    }

    // 관리자가 신고 처리할 때 신고 내용 보기
    @Transactional(readOnly = true)
    public ReportDTO getReportDTOById(Long reportId){
        return reportRepository.getReportDTOByReportId(reportId);
    }

    // 관리자가 신고 처리
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

    // 반환받은 Repository로 post 조회
    private Post getPostByIdAndCategoryTitle(Long postId, String categoryTitle) {
        return getRepositoryByCategoryTitle(categoryTitle).findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    // 카테고리 제목에 따라 맞는 Repository 반환
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