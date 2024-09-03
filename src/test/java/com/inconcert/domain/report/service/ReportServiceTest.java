package com.inconcert.domain.report.service;

import com.inconcert.common.auth.jwt.token.service.TokenService;
import com.inconcert.common.exception.PostNotFoundException;
import com.inconcert.common.exception.ReportNotFoundException;
import com.inconcert.common.service.ImageService;
import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.report.dto.ReportDTO;
import com.inconcert.domain.report.entity.Report;
import com.inconcert.domain.report.repository.ReportRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReportServiceTest {
    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserService userService;

    @Mock
    private ImageService imageService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private ReportService reportService;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 신고_성공() {
        // Given
        Long postId = 1L;
        String categoryTitle = "match";
        String type = "spam";

        Post post = mock(Post.class);
        User reporter = mock(User.class);

        when(matchRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(reporter));
        when(reportRepository.existsByReporterAndPost(reporter, post)).thenReturn(false);

        // When
        reportService.report(postId, categoryTitle, type);

        // Then
        verify(reportRepository, times(1)).save(any(Report.class));
    }

    @Test
    void 신고_실패_이미_신고한_경우() {
        // Given
        Long postId = 1L;
        String categoryTitle = "match";
        String type = "injustice";

        Post post = mock(Post.class);
        User reporter = mock(User.class);

        when(matchRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(reporter));
        when(reportRepository.existsByReporterAndPost(reporter, post)).thenReturn(true);

        // When
        reportService.report(postId, categoryTitle, type);

        // Then
        verify(reportRepository, never()).save(any(Report.class));
    }
    @Test
    void 신고_실패_포스트_없음() {
        // Given
        Long postId = 1L;
        String categoryTitle = "match";

        when(matchRepository.findById(postId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> reportService.report(postId, categoryTitle, "pornography"))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("찾으려는 게시글이 존재하지 않습니다.");
    }

    @Test
    void 신고_결과_처리() {
        // Given
        Long reportId = 1L;
        Report report = mock(Report.class);
        Post post = mock(Post.class);
        User postUser = mock(User.class);
        ReportDTO reportDTO = ReportDTO.builder()
                .result("7day")
                .build();

        // PostCategory와 Category 설정
        PostCategory postCategory = mock(PostCategory.class);
        Category category = mock(Category.class);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(report.getPost()).thenReturn(post);
        when(post.getUser()).thenReturn(postUser);
        when(post.getPostCategory()).thenReturn(postCategory);
        when(postCategory.getCategory()).thenReturn(category);
        when(category.getTitle()).thenReturn("match");

        // When
        reportService.reportResult(reportId, reportDTO);

        // Then
        verify(postUser, times(1)).updateBanDate(any());
        verify(reportRepository, times(1)).deleteById(reportId);
        verify(matchRepository, times(1)).delete(post);     // 포스트 삭제되었는지 확인
    }

    @Test
    void 신고_결과_처리_실패_신고_없음() {
        // Given
        Long reportId = 1L;

        // 신고가 존재하지 않는 경우를 설정
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> reportService.reportResult(reportId, new ReportDTO()))
                .isInstanceOf(ReportNotFoundException.class)
                .hasMessage("찾으려는 신고가 존재하지 않습니다.");
    }

    @Test
    void 신고_결과_처리_실패_게시글_없음() {
        // Given
        Long reportId = 1L;
        Report report = mock(Report.class);

        // 신고는 있지만 게시글이 없는 경우를 설정
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(report.getPost()).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> reportService.reportResult(reportId, new ReportDTO()))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("찾으려는 게시글이 존재하지 않습니다.");
    }

    @Test
    void 신고_목록_조회() {
        // Given
        ReportDTO reportDTO = new ReportDTO(1L, "youthHarmful", new Post(), new User());
        when(reportRepository.getReportDTOs()).thenReturn(Arrays.asList(reportDTO));

        // When
        List<ReportDTO> result = reportService.getReportDTOAll();

        // Then
        assertThat(result.get(0).getType()).isEqualTo(reportDTO.getType());
        verify(reportRepository, times(1)).getReportDTOs();
    }

    @Test
    void 신고_내용_보기() {
        // Given
        Long reportId = 1L;
        ReportDTO reportDTO = new ReportDTO(reportId, "personalInformation", new Post(), new User());
        when(reportRepository.getReportDTOByReportId(reportId)).thenReturn(reportDTO);

        // When
        ReportDTO result = reportService.getReportDTOById(reportId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reportId);
        assertThat(result.getType()).isEqualTo(reportDTO.getType());
        verify(reportRepository, times(1)).getReportDTOByReportId(reportId);
    }
}