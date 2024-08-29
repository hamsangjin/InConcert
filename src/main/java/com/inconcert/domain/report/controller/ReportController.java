package com.inconcert.domain.report.controller;

import com.inconcert.domain.report.dto.ReportDTO;
import com.inconcert.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/report")
    public String reportList(Model model) {
        model.addAttribute("reports", reportService.getReportDTOAll());
        return "report/reportlist";
    }

    @PostMapping("/report/{id}/delete")
    public String deleteReport(@PathVariable("id") Long id) {
        reportService.deleteReportId(id);

        return "redirect:/report";
    }

    @GetMapping("/report/{id}")
    public String reportDetailForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("reportDTO", new ReportDTO());
        model.addAttribute("report", reportService.getReportDTOById(id));

        return "report/reportdetail";
    }

    @PostMapping("/report/{id}")
    public String reportDetail(@PathVariable("id") Long id,
                               @ModelAttribute ReportDTO reportDTO){
        reportService.reportResult(id, reportDTO);

        return "redirect:/report";
    }
}