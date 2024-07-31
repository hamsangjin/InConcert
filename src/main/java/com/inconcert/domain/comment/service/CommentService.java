package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.comment.repository.CommentRepository;
import com.inconcert.global.exception.CommentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<CommentDto> findByPostId(Long Id) {
        List<Comment> byPostId = commentRepository.findByPostId(Id);
        List<CommentDto> dtoList = new ArrayList<>();

        for (Comment comment : byPostId) {
            dtoList.add(comment.toCommentDto());
        }
        return dtoList;
    }

    @Transactional
    public CommentDto findComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        return comment.toCommentDto();
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        commentRepository.delete(comment);
    }
}