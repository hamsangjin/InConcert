package com.inconcert.domain.user.service;



import com.inconcert.domain.chat.repository.ChatRoomRepository;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.feedback.repository.FeedbackRepository;
import com.inconcert.domain.like.entity.Like;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.user.dto.response.FeedbackRspDTO;
import com.inconcert.domain.user.dto.response.MatchRspDTO;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.repository.MyPageRepostory;
import com.inconcert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;


import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {
    @Mock
    private MyPageRepostory myPageRepostory;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;


    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private MyPageService myPageService;

    @Test
    void 마이페이지_게시물_불러오기() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        PostDTO postDTO = new PostDTO();
        List<PostDTO> postDTOList = Arrays.asList(postDTO); //1개의 요소 리스트로 저장
        Page<PostDTO> postPage = new PageImpl<>(postDTOList, pageable, postDTOList.size());

        when(myPageRepostory.getPostDTOsByUserId(userId, pageable)).thenReturn(postPage);

        // when
        Page<PostDTO> result = myPageService.getMyPosts(userId, page, size);

        // then
        assertEquals(1, result.getTotalElements()); //개수가 1개 맞는가 ?
        assertEquals(postDTO, result.getContent().get(0)); // postDTO 와 myPageService 에서 불러온 게시물의 정보가 일치한가 ?
    }

    @Test
    void 내가_댓글_단_게시물_조회() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);


        User user = new User();
        user.setId(userId);
        user.setNickname("User1");

        Post post1 = new Post();
        post1.setId(1L);
        post1.setTitle("Post 1");

        Post post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Post 2");

        Comment comment1 = new Comment();
        comment1.setUser(user);
        comment1.setPost(post1);
        comment1.setContent("댓글을 단 1번 포스트");

        Comment comment2 = new Comment();
        comment2.setUser(user);
        comment2.setPost(post2);
        comment2.setContent("댓글을 단 2번 포스트");


        PostDTO postDTO1 = new PostDTO(post1.getId(), post1.getTitle(), "info", "musical", "http://image1.png", user.getNickname(), 10, 5, 3, false, post1.getCreatedAt());
        PostDTO postDTO2 = new PostDTO(post2.getId(), post2.getTitle(), "match", "musical", "http://image2.png", user.getNickname(), 15, 8, 4, true, post2.getCreatedAt());

        List<PostDTO> postDTOList = Arrays.asList(postDTO1, postDTO2);
        Page<PostDTO> postPage = new PageImpl<>(postDTOList, pageable, postDTOList.size());

        when(myPageRepostory.getPostDTOsWithMyComments(userId, pageable)).thenReturn(postPage);

        // when
        Page<PostDTO> result = myPageService.getMyCommentPosts(userId, page, size);

        // then
        assertThat(2).isEqualTo(result.getTotalElements()); // 2개가 맞는가 ?
        assertThat(postDTO1.getId()).isEqualTo(result.getContent().get(0).getId());
        assertThat(postDTO2.getTitle()).isEqualTo(result.getContent().get(1).getTitle());

    }


    @Test
    void 내가_좋아요한_게시물_불러오기(){


        Long userId = 1L;
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);


        User user = new User();
        user.setId(userId);
        user.setNickname("User1");

        Post post1 = new Post();
        post1.setId(1L);
        post1.setTitle("Post 1");

        Post post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Post 2");

        Like like1 = new Like();
        like1.setUser(user);
        like1.setPost(post1);


        Comment comment2 = new Comment();
        comment2.setUser(user);
        comment2.setPost(post2);



        PostDTO postDTO1 = new PostDTO(post1.getId(), post1.getTitle(), "info", "musical", "http://image1.png", user.getNickname(), 10, 5, 3, false, post1.getCreatedAt());
        PostDTO postDTO2 = new PostDTO(post2.getId(), post2.getTitle(), "match", "musical", "http://image2.png", user.getNickname(), 15, 8, 4, true, post2.getCreatedAt());

        List<PostDTO> postDTOList = Arrays.asList(postDTO1, postDTO2);
        Page<PostDTO> postPage = new PageImpl<>(postDTOList, pageable, postDTOList.size());

        when(myPageRepostory.getPostDTOsMyLiked(userId, pageable)).thenReturn(postPage);

        // when
        Page<PostDTO> result = myPageService.getMyLikePosts(userId, page, size);
        System.out.println(result.getContent().get(0).getTitle());
        // then
        assertThat(2).isEqualTo(result.getTotalElements()); // 2개
        assertThat(postDTO1.getId()).isEqualTo(result.getContent().get(0).getId());
        assertThat(postDTO2.getTitle()).isEqualTo(result.getContent().get(1).getTitle());

    }


    @Test
    void 기본_프사로_변경() {
        // given
        User user = new User();
        user.setId(1L);
        user.setNickname("User1");
        user.setProfileImage("example.png");

        when(userService.getAuthenticatedUser()).thenReturn(Optional.of(user));

        // when
        ResponseEntity<String> response = myPageService.resetToDefaultProfileImage();

        // then
        verify(userRepository, times(1)).save(user);  // 사용자 저장 호출 검증
        assertThat("/images/profile.png").isEqualTo(user.getProfileImage()); //유저의 프로필 사진이 바뀌었는지
        assertEquals("이미지가 정상적으로 변경되었습니다.", response.getBody());  // 응답 메시지 검증
    }


    @Test
    void 동행중인_유저_목록() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        MatchRspDTO matchDTO1 = new MatchRspDTO();
        MatchRspDTO matchDTO2 = new MatchRspDTO();
        List<MatchRspDTO> matchDTOList = Arrays.asList(matchDTO1, matchDTO2);
        Page<MatchRspDTO> matchPage = new PageImpl<>(matchDTOList, pageable, matchDTOList.size());

        when(chatRoomRepository.getChatRoomDTOsByUserId(userId, pageable)).thenReturn(matchPage);

        // when
        Page<MatchRspDTO> result = myPageService.presentMatch(userId, page, size);

        // then
        assertEquals(2, result.getTotalElements());  // 총 요소 개수
        assertEquals(matchDTO1, result.getContent().get(0));
        verify(chatRoomRepository, times(1)).getChatRoomDTOsByUserId(userId, pageable);
    }

    @Test
    void 동행_완료된_유저_목록() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        MatchRspDTO matchDTO1 = new MatchRspDTO();  // Mock MatchRspDTO 객체 생성
        MatchRspDTO matchDTO2 = new MatchRspDTO();
        List<MatchRspDTO> matchDTOList = Arrays.asList(matchDTO1, matchDTO2);
        Page<MatchRspDTO> matchPage = new PageImpl<>(matchDTOList, pageable, matchDTOList.size());

        when(matchRepository.getMatchRspDTOsByUserIdInMatchUserIdsAndEndMatch(userId, pageable)).thenReturn(matchPage);

        // when
        Page<MatchRspDTO> result = myPageService.completeMatch(userId, page, size);

        // then
        assertEquals(2, result.getTotalElements());  // 총 요소 개수 확인
        assertEquals(matchDTO1, result.getContent().get(0));  // 첫 번째 요소 확인
        assertEquals(matchDTO2, result.getContent().get(1));  // 두 번째 요소 확인
        verify(matchRepository, times(1)).getMatchRspDTOsByUserIdInMatchUserIdsAndEndMatch(userId, pageable);  // 메서드 호출 검증
    }

    @Test
    void 동행_완료됐을시() {
        // given
        Long userId = 1L;
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);

        MatchRspDTO matchDTO1 = new MatchRspDTO();
        MatchRspDTO matchDTO2 = new MatchRspDTO();
        List<MatchRspDTO> matchDTOList = Arrays.asList(matchDTO1, matchDTO2);
        Page<MatchRspDTO> matchPage = new PageImpl<>(matchDTOList, pageable, matchDTOList.size());

        when(matchRepository.getMatchRspDTOsByUserIdInMatchUserIdsAndEndMatch(userId, pageable)).thenReturn(matchPage);

        // when
        Page<MatchRspDTO> result = myPageService.completeMatch(userId, page, size);

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals(matchDTO1, result.getContent().get(0));
        verify(matchRepository, times(1)).getMatchRspDTOsByUserIdInMatchUserIdsAndEndMatch(userId, pageable);  // 메서드 호출 검증
    }


    @Test
    void 나를_평가하는_리뷰어_목록(){
        // given
        Long userId = 1L;
        Long postId = 1L;
        List<Long> matchUserIds = Arrays.asList(2L, 3L);
        List<FeedbackRspDTO> feedbackList = Arrays.asList(new FeedbackRspDTO(), new FeedbackRspDTO());

        when(matchRepository.findMatchUsersByPostId(postId, userId)).thenReturn(matchUserIds);
        when(userRepository.getFeedbackRspDTOByMatchUserIds(userId, postId, matchUserIds)).thenReturn(feedbackList);

        // when
        List<FeedbackRspDTO> result = myPageService.getMyReviewee(userId, postId);

        // then
        assertEquals(2, result.size());
        assertEquals(feedbackList, result);
        verify(matchRepository, times(1)).findMatchUsersByPostId(postId, userId);
        verify(userRepository, times(1)).getFeedbackRspDTOByMatchUserIds(userId, postId, matchUserIds);
    }

    @Test
    void 이미_리뷰를_남겼을_경우() {
        // given
        Long userId = 1L;
        Long postId = 1L;
        List<Long> matchUserIds = Arrays.asList(2L, 3L, 4L);
        List<Long> revieweeIds = Arrays.asList(2L, 4L);  // 유저가 이미 리뷰를 남긴 사용자 ID

        when(matchRepository.findMatchUsersByPostId(postId, userId)).thenReturn(matchUserIds);
        when(feedbackRepository.getExistingRevieweeIdsByReviewerAndPost(userId, matchUserIds, postId)).thenReturn(revieweeIds);

        // when
        List<Boolean> result = myPageService.getUsersReviewStatuses(userId, postId);

        // then
        assertEquals(3, result.size());  // 예상 결과와 비교하여 크기 검증
        assertEquals(Arrays.asList(true, false, true), result);  // 예상된 결과 확인
        verify(matchRepository, times(1)).findMatchUsersByPostId(postId, userId);
        verify(feedbackRepository, times(1)).getExistingRevieweeIdsByReviewerAndPost(userId, matchUserIds, postId);
    }

    @Test
    void 모든_게시글에_대해_모든_리뷰가_완료() {
        // given
        Long userId = 1L;
        List<Long> postIds = Arrays.asList(1L, 2L);

        when(myPageService.getUsersReviewStatuses(userId, 1L)).thenReturn(Arrays.asList(true, true));
        when(myPageService.getUsersReviewStatuses(userId, 2L)).thenReturn(Arrays.asList(true, true, true));

        // when
        List<Boolean> result = myPageService.isEndFeedback(userId, postIds);

        // then
        assertEquals(2, result.size());
        assertEquals(Arrays.asList(true, true), result);  // 모든 게시글에 대해 모든 리뷰가 완료된 경우 확인
    }

    @Test
    void 모든_리뷰가_완료되지_않았을_경우() {
        // given
        Long userId = 1L;
        List<Long> postIds = Arrays.asList(1L, 2L);

        when(myPageService.getUsersReviewStatuses(userId, 1L)).thenReturn(Arrays.asList(true, false));  // 하나의 리뷰가 완료되지 않은 경우
        when(myPageService.getUsersReviewStatuses(userId, 2L)).thenReturn(Arrays.asList(true, true, true));

        // when
        List<Boolean> result = myPageService.isEndFeedback(userId, postIds);

        // then
        assertEquals(2, result.size());
    }


}