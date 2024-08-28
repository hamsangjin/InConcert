package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreationDTO;
import com.inconcert.domain.post.entity.Post;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    void saveComment(String categoryTitle, Long id, CommentCreationDTO dto);
    void saveReply(String categoryTitle, Long postId, Long parentId, CommentCreationDTO dto);
    void updateComment(String categoryTitle, Long id, CommentCreationDTO dto);
    void deleteComment(String categoryTitle, Long id);
    Post getPostById(Long postId);
}
