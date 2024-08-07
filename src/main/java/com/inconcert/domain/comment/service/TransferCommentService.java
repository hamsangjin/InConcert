package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.comment.repository.CommentRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.post.repository.TransferRepository;
import com.inconcert.domain.user.entity.User;
import com.inconcert.global.exception.CommentNotFoundException;
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
    public List<CommentDto> findByPostId(String boardType, Long id, String sort) {
        List<Comment> byPostId;
        if ("recent".equals(sort)) {
            byPostId = commentRepository.findByPostIdOrderByCreatedAtDesc(id);
        } else {
            byPostId = commentRepository.findByPostIdOrderByCreatedAtAsc(id);
        }
        List<CommentDto> dtoList = new ArrayList<>();
        for (Comment comment : byPostId) {
            dtoList.add(comment.toCommentDto());
        }
        return dtoList;
    }

    @Override
    @Transactional
    public CommentDto findComment(String boardType, Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        return comment.toCommentDto();
    }

    @Override
    @Transactional
    public Long saveComment(String boardType, Long id, User user, CommentCreateForm dto) {
        Post post = transferRepository.findById(id).orElseThrow(() -> new PostNotFoundException("ID = " + id + " 의 해당 게시글이 존재하지 않습니다."));

        Comment comment = dto.toEntity();
        comment.setPost(post);
        comment.setUser(user);

        commentRepository.save(comment);

        return comment.getId();
    }

    @Override
    @Transactional
    public void reSaveComment(String boardType, Long postId, Long parentId, User user, CommentCreateForm dto) {
        dto.setUser(user);
        Comment comment = dto.toEntity();

        comment.confirmPost(transferRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("ID = " + postId + " 의 해당 게시글이 존재하지 않습니다.")));

        comment.confirmParent(commentRepository.findById(parentId).orElseThrow(() -> new CommentNotFoundException("ID = " + postId + " 의 해당 부모 댓글이 존재하지 않습니다.")));

        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Long updateComment(String boardType, Long id, CommentCreateForm dto) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        comment.update(dto.getContent(), dto.getIsSecret());
        commentRepository.save(comment);
        return comment.getId();
    }

    @Override
    @Transactional
    public void deleteComment(String boardType, Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        commentRepository.delete(comment);
    }
}
