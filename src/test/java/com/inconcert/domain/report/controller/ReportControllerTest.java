package com.inconcert.domain.report.controller;

import com.inconcert.domain.report.dto.ReportDTO;
import com.inconcert.domain.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ReportControllerTest {
    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void 초기화() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 신고_삭제_성공() {
        // Given
        Long reportId = 1L;

        // When
        String result = reportController.deleteReport(reportId);

        // Then
        verify(reportService, times(1)).deleteReportId(reportId);
        assertThat(result).isEqualTo("redirect:/report");
    }

    @Test
    void 신고_상세_보기_성공() {
        // Given
        Long reportId = 1L;
        ReportDTO reportDTO = new ReportDTO();

        // Model 객체 Mocking
        Model model = mock(Model.class);

        when(reportService.getReportDTOById(reportId)).thenReturn(reportDTO);

        // When
        String result = reportController.reportDetailForm(reportId, model);

        // Then
        verify(reportService, times(1)).getReportDTOById(reportId);
        verify(model, times(1)).addAttribute(eq("reportDTO"), any(ReportDTO.class)); // 모델에 속성이 추가되는지 확인
        verify(model, times(1)).addAttribute(eq("report"), eq(reportDTO)); // 모델에 신고가 추가되는지 확인
        assertThat(result).isEqualTo("report/reportdetail");
    }

    @Test
    void 신고_결과_처리_성공() {
        // Given
        Long reportId = 1L;
        ReportDTO reportDTO = new ReportDTO();

        // When
        String result = reportController.reportDetail(reportId, reportDTO);

        // Then
        verify(reportService, times(1)).reportResult(reportId, reportDTO);
        assertThat(result).isEqualTo("redirect:/report");
    }

    @Test
    void 신고_상세_보기_실패() {
        // Given
        Long reportId = 1L;
        Model model = mock(Model.class);

        // reportService가 특정 reportId로 null을 반환하도록 설정
        when(reportService.getReportDTOById(reportId)).thenReturn(null);

        // When
        String result = reportController.reportDetailForm(reportId, model);

        // Then
        // 모델에 "reportDTO"와 "report"가 null 상태로 추가되었는지 확인
        verify(model, times(1)).addAttribute(eq("reportDTO"), any(ReportDTO.class));
        verify(model, times(1)).addAttribute("report", null);
        assertThat(result).isEqualTo("report/reportdetail");
    }

    @Test
    void 신고_결과_처리_실패() {
        // Given
        Long reportId = 1L;
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setResult("invalidResult"); // 잘못된 결과값 설정

        // reportService가 특정 reportId로 예외를 던지도록 설정
        doThrow(new IllegalArgumentException("Invalid result value"))
                .when(reportService).reportResult(reportId, reportDTO);

        // When / Then
        assertThatThrownBy(() -> reportController.reportDetail(reportId, reportDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid result value");
    }
}