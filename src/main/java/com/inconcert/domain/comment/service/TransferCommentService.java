package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.comment.dto.CommentDTO;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.comment.repository.CommentRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.TransferRepository;
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
public class TransferCommentService implements CommentService {
    private final CommentRepository commentRepository;
    private final TransferRepository transferRepository;

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
    public Long saveComment(String boardType, Long id, User user, CommentCreationDTO dto) {
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
    public void saveReply(String boardType, Long postId, Long parentId, User user, CommentCreationDTO dto) {
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

    @Override
    @Transactional(readOnly = true)
    public Post getPostByCategoryAndId(String categoryTitle, Long postId) {
        return transferRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ExceptionMessage.POST_NOT_FOUND.getMessage()));
    }

    @Override
    public void validateCommentDeletion(CommentDTO dto, Post post, User user) {
        boolean isCommentAuthor = dto.getUser().getUsername().equals(user.getUsername());
        boolean isPostAuthor = post.getUser().getUsername().equals(user.getUsername());

        if (!isCommentAuthor && !isPostAuthor) {
            throw new SecurityException("이 댓글을 삭제할 권한이 없습니다.");
        }
    }

    @Override
    public void validateCommentEditAuthorization(CommentDTO dto, User user) {
        if (!dto.getUser().getUsername().equals(user.getUsername())) {
            throw new SecurityException("이 댓글을 수정할 권한이 없습니다.");
        }
    }
}
