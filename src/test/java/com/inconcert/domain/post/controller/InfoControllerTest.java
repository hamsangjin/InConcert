//package com.inconcert.domain.post.controller;
//
//import com.inconcert.domain.post.dto.PostDTO;
//import com.inconcert.domain.post.entity.Post;
//import com.inconcert.domain.category.entity.PostCategory;
//import com.inconcert.domain.post.service.InfoService;
//import com.inconcert.domain.post.service.WriteService;
//import com.inconcert.domain.user.service.UserService;
//import com.inconcert.common.exception.CategoryNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.ui.Model;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class InfoControllerTest {
//
//    @Mock
//    private WriteService writeService;
//
//    @Mock
//    private InfoService infoService;
//
//    @Mock
//    private UserService userService;
//
//    // 테스트할 InfoController에 Mock된 서비스들을 주입
//    @InjectMocks
//    private InfoController infoController;
//
//    @Mock
//    private Model model;
//
//    // 각 테스트가 실행되기 전에 Mock 객체들을 초기화
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void 게시글_작성_테스트() {
//        // Given
//        // 게시글 작성에 필요한 DTO 객체 생성 및 설정
//        PostDTO postDto = new PostDTO();
//        postDto.setTitle("Test Post");
//        postDto.setContent("Test Content");
//        postDto.setPostCategoryTitle("musical");
//
//        // 카테고리 Mock 객체 생성 및 설정
//        PostCategory postCategory = mock(PostCategory.class);
//        when(postCategory.getTitle()).thenReturn("musical");
//
//        // 작성된 게시글의 Mock 객체 생성 및 설정
//        Post mockPost = mock(Post.class);
//        when(mockPost.getId()).thenReturn(1L);
//        when(mockPost.getPostCategory()).thenReturn(postCategory);
//
//        // WriteService의 save 메소드가 호출되면 mockPost를 반환하도록 설정
//        when(writeService.save(any(PostDTO.class))).thenReturn(mockPost);
//
//        // When
//        // InfoController의 write 메소드를 호출하여 게시글 작성
//        String result = infoController.write(postDto);
//
//        // Then
//        // WriteService의 save 메소드가 정확히 한 번 호출되었는지 검증
//        verify(writeService, times(1)).save(any(PostDTO.class));
//        // 리다이렉트 결과가 예상과 일치하는지 검증
//        assertEquals("redirect:/info/musical/1", result);
//    }
//
//    @Test
//    void 유효하지_않은_카테고리에_게시글_작성_테스트() {
//        // Given
//        // 유효하지 않은 카테고리로 게시글 작성 DTO 설정
//        PostDTO postDto = new PostDTO();
//        postDto.setTitle("Test Invalid Post");
//        postDto.setContent("Test Invalid Content");
//        postDto.setPostCategoryTitle("invalid");
//
//        // WriteService의 save 메소드가 호출되면 CategoryNotFoundException을 던지도록 설정
//        when(writeService.save(any(PostDTO.class)))
//                .thenThrow(new CategoryNotFoundException("Invalid category"));
//
//        // When / Then
//        // 예외가 발생하는지 검증
//        assertThrows(CategoryNotFoundException.class, () -> {
//            infoController.write(postDto);
//        });
//    }
//
//    @Test
//    void 게시판_정보_조회_테스트() {
//        // Given
//        // 각 카테고리에 대한 게시글 목록 반환 설정
//        when(infoService.getAllInfoPostsByPostCategory("musical")).thenReturn(List.of(new PostDTO()));
//        when(infoService.getAllInfoPostsByPostCategory("concert")).thenReturn(List.of(new PostDTO()));
//        when(infoService.getAllInfoPostsByPostCategory("theater")).thenReturn(List.of(new PostDTO()));
//        when(infoService.getAllInfoPostsByPostCategory("etc")).thenReturn(List.of(new PostDTO()));
//
//        // When
//        // InfoController의 info 메소드를 호출하여 정보 조회
//        String result = infoController.info(model);
//
//        // Then
//        // 각 카테고리에 대한 서비스 메소드가 정확히 한 번씩 호출되었는지 검증
//        verify(infoService, times(1)).getAllInfoPostsByPostCategory("musical");
//        verify(infoService, times(1)).getAllInfoPostsByPostCategory("concert");
//        verify(infoService, times(1)).getAllInfoPostsByPostCategory("theater");
//        verify(infoService, times(1)).getAllInfoPostsByPostCategory("etc");
//
//        // 반환된 뷰 이름이 예상과 일치하는지 검증
//        assertEquals("board/board", result);
//    }
//
//    @Test
//    void 게시글_상세보기_테스트() {
//        // Given
//        Long postId = 1L;
//        String postCategoryTitle = "musical";
//        PostDTO postDTO = new PostDTO();
//
//        // 해당 게시글 DTO를 반환하도록 설정
//        when(infoService.getPostDtoByPostId(postId)).thenReturn(postDTO);
//
//        // 인증된 사용자가 없는 상태로 설정
//        when(userService.getAuthenticatedUser()).thenReturn(null);
//
//        // When
//        // InfoController의 getPostDetail 메소드를 호출하여 게시글 상세보기 수행
//        String result = infoController.getPostDetail(postCategoryTitle, postId, model);
//
//        // Then
//        // 서비스 계층 메소드가 정확히 호출되었는지 검증
//        verify(infoService, times(1)).getPostDtoByPostId(postId);
//        verify(userService, times(1)).getAuthenticatedUser();
//        verify(model, times(1)).addAttribute("post", postDTO);
//
//        // 반환된 뷰 이름이 예상과 일치하는지 검증
//        assertEquals("board/post-detail", result);
//    }
//
//    @Test
//    void 게시글_삭제_테스트() {
//        // Given
//        Long postId = 1L;
//        String postCategoryTitle = "musical";
//
//        // When
//        // InfoController의 deletePost 메소드를 호출하여 게시글 삭제 수행
//        String result = infoController.deletePost(postCategoryTitle, postId);
//
//        // Then
//        // 서비스 계층의 deletePost 메소드가 정확히 한 번 호출되었는지 검증
//        verify(infoService, times(1)).deletePost(postId);
//        // 리다이렉트 결과가 예상과 일치하는지 검증
//        assertEquals("redirect:/info/musical", result);
//    }
//
//    @Test
//    void 게시글_수정_폼_테스트() {
//        // Given
//        Long postId = 1L;
//        String postCategoryTitle = "musical";
//        PostDTO postDTO = new PostDTO();
//
//        // 수정할 게시글 DTO 반환 설정
//        when(infoService.getPostDtoByPostId(postId)).thenReturn(postDTO);
//
//        // When
//        // InfoController의 editPostForm 메소드를 호출하여 수정 폼 조회
//        String result = infoController.editPostForm(postCategoryTitle, postId, model);
//
//        // Then
//        // 서비스 계층 메소드 호출 및 모델에 데이터 추가 검증
//        verify(infoService, times(1)).getPostDtoByPostId(postId);
//        verify(model, times(1)).addAttribute("post", postDTO);
//
//        // 반환된 뷰 이름이 예상과 일치하는지 검증
//        assertEquals("board/editform", result);
//    }
//
//    @Test
//    void 게시글_검색_테스트() {
//        // Given
//        String postCategoryTitle = "musical";
//        String keyword = "test";
//        String period = "all";
//        String type = "title+content";
//        int page = 0;
//        int size = 10;
//
//        // 검색 결과를 담은 페이지 객체 반환 설정
//        Page<PostDTO> postsPage = new PageImpl<>(List.of(new PostDTO()));
//        when(infoService.getByKeywordAndFilters(postCategoryTitle, keyword, period, type, page, size)).thenReturn(postsPage);
//
//        // When
//        // InfoController의 search 메소드를 호출하여 검색 수행
//        String result = infoController.search(postCategoryTitle, keyword, period, type, page, size, model);
//
//        // Then
//        // 서비스 계층 메소드 호출 및 검색 결과가 예상대로 처리되었는지 검증
//        verify(infoService, times(1)).getByKeywordAndFilters(postCategoryTitle, keyword, period, type, page, size);
//        // 반환된 뷰 이름이 예상과 일치하는지 검증
//        assertEquals("board/board-detail", result);
//    }
//
//    @Test
//    void 게시글_신고_폼_테스트() {
//        // Given
//        Long postId = 1L;
//        PostDTO postDTO = new PostDTO();
//
//        // 신고할 게시글 DTO 반환 설정
//        when(infoService.getPostDtoByPostId(postId)).thenReturn(postDTO);
//
//        // When
//        // InfoController의 reportForm 메소드를 호출하여 신고 폼 조회
//        String result = infoController.reportForm(postId, model);
//
//        // Then
//        // 서비스 계층 메소드 호출 및 모델에 데이터 추가 검증
//        verify(infoService, times(1)).getPostDtoByPostId(postId);
//        // 반환된 뷰 이름이 예상과 일치하는지 검증
//        assertEquals("report/reportform", result);
//    }
//}