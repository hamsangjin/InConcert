package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.comment.dto.CommentDTO;
import com.inconcert.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    List<CommentDTO> getCommentDTOsByPostId(String boardType, Long id, String sort);
    CommentDTO getCommentDTOByBoardTypeAndId(String boardType, Long id);
    Long saveComment(String boardType, Long id, User user, CommentCreateForm dto);
    void reSaveComment(String boardType, Long postId, Long parentId, User user, CommentCreateForm dto);
    Long updateComment(String boardType, Long id, CommentCreateForm dto);
    void deleteComment(String boardType, Long id);
}

