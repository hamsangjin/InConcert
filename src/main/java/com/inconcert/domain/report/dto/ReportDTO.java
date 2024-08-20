package com.inconcert.domain.report.dto;

import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.report.entity.Report;
import com.inconcert.domain.user.entity.User;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {
    private Long id;
    private String type;
    private Post post;
    private User reporter;
    private String result;

    public static Report toEntity(ReportDTO reportDTO) {
        return Report.builder()
                .type(reportDTO.getType())
                .post(reportDTO.getPost())
                .reporter(reportDTO.getReporter())
                .build();
    }

    public ReportDTO(String type, Post post, User reporter) {
        this.type = type;
        this.post = post;
        this.reporter = reporter;
    }
}
