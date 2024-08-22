package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.comment.dto.CommentDTO;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    List<CommentDTO> getCommentDTOsByPostId(String boardType, Long id, String sort);
    Long saveComment(String boardType, Long id, CommentCreationDTO dto);
    void saveReply(String boardType, Long postId, Long parentId, CommentCreationDTO dto);
    Long updateComment(String boardType, Long id, CommentCreationDTO dto);
    void deleteComment(String boardType, Long id);
    Post getPostByCategoryAndId(String categoryTitle, Long postId);
    void validateCommentDeletion(CommentDTO dto, Post post, User user);
    void validateCommentEditAuthorization(CommentDTO dto, User user);
}
