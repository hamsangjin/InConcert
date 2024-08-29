package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.comment.repository.CommentRepository;
import com.inconcert.domain.notification.service.NotificationService;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchCommentService implements CommentService {
    private final CommentRepository commentRepository;
    private final MatchRepository matchRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void saveComment(String categoryTitle, Long postId, CommentCreationDTO dto) {
        // 인증 여부를 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            // 인증되지 않은 경우 예외 발생
            throw new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage());
        }

        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        Post post = getPostById(postId);

        dto.setPost(post);
        dto.setUser(user);
        Comment comment = dto.toEntity();
        Comment saveComment = commentRepository.save(comment);

        if(!saveComment.getUser().getId().equals(post.getUser().getId())) notificationService.createCommentsNotification(post, saveComment.getContent());
    }

    @Override
    @Transactional
    public void saveReply(String categoryTitle, Long postId, Long parentId, CommentCreationDTO dto) {
        // 인증 여부를 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            // 인증되지 않은 경우 예외 발생
            throw new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage());
        }

        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        Post post = getPostById(postId);

        dto.setUser(user);
        dto.setPost(post);

        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
        dto.setParent(parentComment);

        Comment comment = dto.toEntity();
        Comment saveComment = commentRepository.save(comment);

        if(!saveComment.getUser().getId().equals(post.getUser().getId())) notificationService.createCommentsNotification(post, saveComment.getContent());
    }

    @Override
    @Transactional
    public void updateComment(String categoryTitle, Long commentId, CommentCreationDTO dto) {
        commentRepository.updateComment(commentId, dto.getContent(), dto.getIsSecret());
    }

    @Override
    @Transactional
    public void deleteComment(String categoryTitle, Long commentId) {
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPostById(Long postId) {
        return matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }
}