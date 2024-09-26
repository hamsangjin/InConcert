package com.inconcert.common.service;

import com.inconcert.common.exception.CategoryNotFoundException;
import com.inconcert.common.repository.HomeRepository;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.repository.InfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 확장 기능을 사용
class HomeServiceTest {
    @Mock
    private InfoRepository infoRepository;
    @Mock
    private HomeRepository homeRepository;
    @InjectMocks
    private HomeService homeService;

    @Test
    public void 각_게시판의_게시글_가져오기() {
        // Given
        String categoryTitle = "info";

        PostDTO postDTO1 = PostDTO.builder()
                .id(1L)
                .title("New Performance Info - 1")
                .categoryTitle(categoryTitle)
                .postCategoryTitle("musical")
                .thumbnailUrl("url_to_thumbnail2.jpg")
                .nickname("user1")
                .viewCount(100)
                .likeCount(10)
                .commentCount(5)
                .isNew(true)
                .createdAt(LocalDateTime.now())
                .build();

        PostDTO postDTO2 = PostDTO.builder()
                .id(2L)
                .title("New Performance Info - 2")
                .categoryTitle(categoryTitle)
                .postCategoryTitle("etc")
                .thumbnailUrl("url_to_thumbnail1.jpg")
                .nickname("user2")
                .viewCount(100)
                .likeCount(10)
                .commentCount(5)
                .isNew(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<PostDTO> postDTOs = Arrays.asList(postDTO1, postDTO2);
        when(infoRepository.findPostsByCategoryTitle(any(Pageable.class))).thenReturn(postDTOs);

        // When
        List<PostDTO> result = homeService.getAllPostDTOsByCategoryTitle(categoryTitle);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(1).getTitle()).isEqualTo(postDTO2.getTitle());
        verify(infoRepository, times(1)).findPostsByCategoryTitle(any(Pageable.class));
    }

    @Test
    public void 각_게시판의_게시글_가져오기_잘못된_카테고리() {
        // Given
        String invalidCategory = "invalidCategory";

        // When
        Throwable thrown = catchThrowable(() -> homeService.getAllPostDTOsByCategoryTitle(invalidCategory));

        // Then
        assertThat(thrown).isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    public void 게시판_전체_검색() {
        // Given
        PostDTO postDTO1 = PostDTO.builder()
                .title("title 1")
                .content("keyword 1")
                .build();

        PostDTO postDTO2 = PostDTO.builder()
                .title("keyword 2")
                .content("content 2")
                .build();

        int page = 0;
        int size = 10;
        String keyword = "keyword";
        Page<PostDTO> expectedPage = new PageImpl<>(Arrays.asList(postDTO1, postDTO2));
        when(homeRepository.findByKeyword(eq(keyword), any(Pageable.class))).thenReturn(expectedPage);

        // When
        Page<PostDTO> result = homeService.getPostDTOsByKeyword(keyword, page, size);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getContent()).contains(keyword);
        assertThat(result.getContent().get(1).getTitle()).contains(keyword);
        verify(homeRepository, times(1)).findByKeyword(eq(keyword), any(Pageable.class));
    }

    @Test
    public void 공연소식_카테고리별_가장_인기있는_게시글_불러오기() {
        // Given
        String categoryTitle = "info";

        PostDTO postDTO1 = PostDTO.builder()
                .id(1L)
                .title("New Performance Info - 1")
                .categoryTitle(categoryTitle)
                .postCategoryTitle("musical")
                .thumbnailUrl("url_to_thumbnail1.jpg")
                .nickname("user1")
                .viewCount(100)
                .likeCount(10)
                .commentCount(5)
                .isNew(true)
                .createdAt(LocalDateTime.now())
                .build();

        PostDTO postDTO2 = PostDTO.builder()
                .id(2L)
                .title("New Performance Info - 2")
                .categoryTitle(categoryTitle)
                .postCategoryTitle("concert")
                .thumbnailUrl("url_to_thumbnail2.jpg")
                .nickname("user2")
                .viewCount(100)
                .likeCount(10)
                .commentCount(5)
                .isNew(true)
                .createdAt(LocalDateTime.now())
                .build();

        PostDTO postDTO3 = PostDTO.builder()
                .id(3L)
                .title("New Performance Info - 3")
                .categoryTitle(categoryTitle)
                .postCategoryTitle("theater")
                .thumbnailUrl("url_to_thumbnail3.jpg")
                .nickname("user3")
                .viewCount(100)
                .likeCount(10)
                .commentCount(5)
                .isNew(true)
                .createdAt(LocalDateTime.now())
                .build();

        PostDTO postDTO4 = PostDTO.builder()
                .id(4L)
                .title("New Performance Info - 4")
                .categoryTitle(categoryTitle)
                .postCategoryTitle("etc")
                .thumbnailUrl("url_to_thumbnail4.jpg")
                .nickname("user4")
                .viewCount(100)
                .likeCount(10)
                .commentCount(5)
                .isNew(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<PostDTO> expectedPosts = Arrays.asList(postDTO1, postDTO2, postDTO3, postDTO4);
        when(infoRepository.findPopularPostByPostCategoryTitle("musical")).thenReturn(Optional.of(postDTO1));
        when(infoRepository.findPopularPostByPostCategoryTitle("concert")).thenReturn(Optional.of(postDTO2));
        when(infoRepository.findPopularPostByPostCategoryTitle("theater")).thenReturn(Optional.of(postDTO3));
        when(infoRepository.findPopularPostByPostCategoryTitle("etc")).thenReturn(Optional.of(postDTO4));

        // When
        List<PostDTO> posts = homeService.getPopularPosts();

        // Then
        assertThat(posts).isEqualTo(expectedPosts);
        assertThat(posts.get(0).getPostCategoryTitle()).isEqualTo(postDTO1.getPostCategoryTitle());
        verify(infoRepository, times(4)).findPopularPostByPostCategoryTitle(anyString());
    }
}