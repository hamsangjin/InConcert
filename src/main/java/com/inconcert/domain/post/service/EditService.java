package com.inconcert.domain.post.service;

import com.inconcert.domain.category.entity.Category;
import com.inconcert.domain.category.entity.PostCategory;
import com.inconcert.domain.category.repository.CategoryRepository;
import com.inconcert.domain.category.repository.PostCategoryRepository;
import com.inconcert.domain.chat.dto.ChatRoomDTO;
import com.inconcert.domain.chat.entity.ChatRoom;
import com.inconcert.domain.chat.repository.ChatRoomRepository;
import com.inconcert.domain.chat.service.ChatService;
import com.inconcert.domain.post.dto.PostDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.InfoRepository;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.common.exception.*;
import com.inconcert.domain.post.util.HtmlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditService {
    private final InfoRepository infoRepository;
    private final MatchRepository matchRepository;
    private final ReviewRepository reviewRepository;
    private final TransferRepository transferRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    @Transactional
    public Long updatePost(Long postId, PostDTO postDto, String currentCategoryTitle, String newCategoryTitle, String newPostCategoryTitle) {
        // 현재 카테고리에서 게시글 찾기
        Post currentPost = getPostByIdAndCategory(postId, currentCategoryTitle);

        Category category = categoryRepository.findByTitle(newCategoryTitle)
                .orElseThrow(() -> new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage()));

        List<PostCategory> postCategories = postCategoryRepository.findByTitle(newPostCategoryTitle);

        // 적절한 PostCategory 찾기
        PostCategory postCategory = postCategories.stream()
                .filter(pc -> pc.getCategory().equals(category))
                .findFirst()
                .orElseThrow(() -> new PostCategoryNotFoundException(ExceptionMessage.POST_CATEGORY_COMBINATION_NOT_FOUND.getMessage()));

        postDto.setThumbnailUrl(extractURL(postDto.getContent()));
        postDto.setContent(HtmlUtils.escapeHtml(postDto.getContent()));

        // 동행을 제외한 카테고리들은 모집인원, 마감 날짜 제거
        if(!category.getTitle().equals("match")){
            postDto.setMatchCount(0);
            postDto.setEndDate(null);
        }

        // 채팅방 찾기 (match 카테고리인 경우에만 처리)
        ChatRoom chatRoom = currentPost.getChatRoom();

        // 다른 게시판에서 동행 게시판으로 수정하는 경우 채팅방 생성
        if(!currentCategoryTitle.equals("match") && newCategoryTitle.equals("match")){
            log.info("얘는?");
            ChatRoomDTO chatRoomDto = chatService.createChatRoom(postDto.getTitle());
            chatRoom = chatRoomRepository.findById(chatRoomDto.getId())
                    .orElseThrow(() -> new ChatNotFoundException(ExceptionMessage.CHAT_NOT_FOUND.getMessage()));

            // Post와 채팅방 연결
            chatRoom.assignPost(currentPost);
            currentPost.assignChatRoom(chatRoom);
        }

        // 새로운 레포지토리에 저장
        Post updatedPost = Post.builder()
                .id(currentPost.getId())
                .title(postDto.getTitle())
                .thumbnailUrl(postDto.getThumbnailUrl())
                .content(postDto.getContent())
                .endDate(postDto.getEndDate())
                .chatRoom(chatRoom)
                .matchCount(postDto.getMatchCount())
                .user(currentPost.getUser())
                .comments(new ArrayList<>(currentPost.getComments()))
                .likes(new ArrayList<>(currentPost.getLikes()))
                .notifications(new ArrayList<>(currentPost.getNotifications()))
                .viewCount(currentPost.getViewCount())
                .postCategory(postCategory)
                .reports(currentPost.getReports())
                .feedbacks(currentPost.getFeedbacks())
                .build();

        savePostToRepository(updatedPost, newCategoryTitle);

        return updatedPost.getId();
    }

    private Post getPostByIdAndCategory(Long postId, String categoryTitle) {
        return getRepositoryByCategoryTitle(categoryTitle).findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    private void savePostToRepository(Post post, String categoryTitle) {
        getRepositoryByCategoryTitle(categoryTitle).save(post);
    }

    private JpaRepository<Post, Long> getRepositoryByCategoryTitle(String categoryTitle) {
        return switch (categoryTitle.toLowerCase()) {
            case "info" -> infoRepository;
            case "match" -> matchRepository;
            case "review" -> reviewRepository;
            case "transfer" -> transferRepository;
            default -> throw new CategoryNotFoundException(ExceptionMessage.CATEGORY_NOT_FOUND.getMessage());
        };
    }

    private static String extractURL(String input) {
        // img의 src속성만 추출하는 패턴 생성
        String regex = "http[s]?://[^\\s\"']+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        String url = null;
        // 패턴에 맞는거 하나라도 찾을 경우 url에 저장 후 리턴
        while (matcher.find()) {
            url = matcher.group();
            break;
        }
        return url;
    }
}