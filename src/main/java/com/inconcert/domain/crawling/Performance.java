package com.inconcert.domain.crawling;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "performances")
@Getter
@Setter
public class Performance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "longtext")
    private String title;

    @Column(name = "image_url")
    private String imageUrl;

    private String date;

    private String place;
}