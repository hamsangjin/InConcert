package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.comment.dto.CommentDTO;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.comment.repository.CommentRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.ReviewRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.exception.CommentNotFoundException;
import com.inconcert.global.exception.ExceptionMessage;
import com.inconcert.global.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCommentService implements CommentService {
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;

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
    public Long saveComment(String boardType, Long id, User user, CommentCreateForm dto) {
        Post post = reviewRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));

        Comment comment = dto.toEntity();
        comment.setPost(post);
        comment.setUser(user);

        // parentId가 있는 경우 부모 댓글 설정
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
    public void reSaveComment(String boardType, Long postId, Long parentId, User user, CommentCreateForm dto) {
        dto.setUser(user);
        Comment comment = dto.toEntity();

        // Post를 설정합니다.
        comment.confirmPost(reviewRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage())));

        // parentId가 null이 아닌 경우에만 부모 댓글을 설정합니다.
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
            comment.confirmParent(parentComment);
        }

        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Long updateComment(String boardType, Long id, CommentCreateForm dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
        comment.update(dto.getContent(), dto.getIsSecret());
        commentRepository.save(comment);
        return comment.getId();
    }

    @Override
    @Transactional
    public void deleteComment(String boardType, Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(ExceptionMessage.COMMENT_NOT_FOUND.getMessage()));
        commentRepository.delete(comment);
    }
}