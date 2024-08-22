package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.comment.dto.CommentDTO;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.comment.repository.CommentRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.MatchRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.domain.user.service.UserService;
import com.inconcert.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchCommentService implements CommentService {
    private final CommentRepository commentRepository;
    private final MatchRepository matchRepository;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentDTOsByPostId(String boardType, Long id, String sort) {
        List<Comment> byPostId;
        if ("desc".equals(sort)) {
            byPostId = commentRepository.findByPostIdOrderByCreatedAtDesc(id);
        } else {
            byPostId = commentRepository.findByPostIdOrderByCreatedAtAsc(id);
        }
        List<CommentDTO> dtoList = new ArrayList<>();
        for (Comment comment : byPostId) {
            dtoList.add(comment.toCommentDto());
        }
        return dtoList;
    }

    @Override
    @Transactional
    public CommentDTO getCommentDTOByBoardTypeAndId(String boardType, Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
        return comment.toCommentDto();
    }

    @Override
    @Transactional
    public Long saveComment(String boardType, Long id, CommentCreationDTO dto) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        Post post = getPostByCategoryAndId(boardType, id);

        Comment comment = dto.toEntity();
        comment.setPost(post);
        comment.setUser(user);

        if (dto.getParent() != null) {
            Comment parentComment = commentRepository.findById(dto.getParent())
                    .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
            comment.confirmParent(parentComment);
        }

        commentRepository.save(comment);

        return comment.getId();
    }

    @Override
    @Transactional
    public void saveReply(String boardType, Long postId, Long parentId, CommentCreationDTO dto) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        Post post = getPostByCategoryAndId(boardType, postId);
        dto.setUser(user);
        Comment comment = dto.toEntity();

        comment.confirmPost(post);

        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
            comment.confirmParent(parentComment);
        }

        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Long updateComment(String boardType, Long id, CommentCreationDTO dto) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));

        validateCommentEditAuthorization(comment.toCommentDto(), user);

        comment.update(dto.getContent(), dto.getIsSecret());
        commentRepository.save(comment);
        return comment.getId();
    }

    @Override
    @Transactional
    public void deleteComment(String boardType, Long id) {
        User user = userService.getAuthenticatedUser()
                .orElseThrow(() -> new UserNotFoundException(ExceptionMessage.USER_NOT_FOUND.getMessage()));
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
        Post post = getPostByCategoryAndId(boardType, comment.getPost().getId());

        validateCommentDeletion(comment.toCommentDto(), post, user);

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPostByCategoryAndId(String categoryTitle, Long postId) {
        return matchRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    @Override
    public void validateCommentDeletion(CommentDTO dto, Post post, User user) {
        boolean isCommentAuthor = dto.getUser().getUsername().equals(user.getUsername());
        boolean isPostAuthor = post.getUser().getUsername().equals(user.getUsername());

        if (!isCommentAuthor && !isPostAuthor) {
            throw new CommentDeleteUnauthorizedException(ExceptionMessage.COMMENT_DELETE_UNAUTHORIZED.getMessage());
        }
    }

    @Override
    public void validateCommentEditAuthorization(CommentDTO dto, User user) {
        if (!dto.getUser().getUsername().equals(user.getUsername())) {
            throw new CommentEditUnauthorizedException(ExceptionMessage.COMMENT_EDIT_UNAUTHORIZED.getMessage());
        }
    }
}
