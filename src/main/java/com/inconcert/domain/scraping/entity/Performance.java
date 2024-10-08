package com.inconcert.domain.scraping.entity;

import com.inconcert.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "performances")
@Getter
@NoArgsConstructor
@ToString
public class Performance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "longtext")
    private String title;

    @Column(name = "image_url")
    private String imageUrl;

    private String date;

    private String place;

    private String type;

    @Builder
    public Performance(String title, String imageUrl, String date, String place, String type) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.date = date;
        this.place = place;
        this.type = type;
    }
}