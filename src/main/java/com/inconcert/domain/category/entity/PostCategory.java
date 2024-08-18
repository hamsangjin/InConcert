package com.inconcert.domain.category.entity;

import com.inconcert.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "post_categories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "postCategory")
    private List<Post> posts;

    public PostCategory(String title, Category category) {
        this.title = title;
        this.category = category;
    }
}