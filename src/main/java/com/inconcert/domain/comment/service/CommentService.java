package com.inconcert.domain.comment.service;

import com.inconcert.domain.comment.dto.CommentCreateForm;
import com.inconcert.domain.comment.dto.CommentDto;
import com.inconcert.domain.comment.entity.Comment;
import com.inconcert.domain.comment.repository.CommentRepository;
import com.inconcert.domain.post.entity.Post;
import com.inconcert.domain.user.entity.User;
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
    private final PostRepository postRepository;

    // 게시글 ID에 대한 댓글 목록
    @Transactional(readOnly = true)
    public List<CommentDto> findByPostId(Long id) {
        List<Comment> byPostId = commentRepository.findByPostId(id);
        List<CommentDto> dtoList = new ArrayList<>();

        for (Comment comment : byPostId) {
            dtoList.add(comment.toCommentDto());
        }
        return dtoList;
    }

    // 주어진 댓글 ID로 댓글 찾기
    @Transactional
    public CommentDto findComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        return comment.toCommentDto();
    }

    // 댓글 저장
    @Transactional
    public Long saveComment(Long id, User user, CommentCreateForm dto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("ID = " + id + " 의 해당 게시글이 존재하지 않습니다."));

        // DTO에서 엔티티로 변환
        Comment comment = dto.toEntity();

        // DTO에서 엔티티 변환 시 설정하지 않은 추가 필드를 설정
        comment.setPost(post);
        comment.setUser(user);

        commentRepository.save(comment);

        return comment.getId();
    }

    // 대댓글 저장
    @Transactional
    public void reSaveComment(Long postId, Long parentId, User user, CommentCreateForm dto) {
        dto.setUser(user);
        Comment comment = dto.toEntity();

        // 예외처리 변경 예정
        comment.confirmPost(postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("ID = " + postId + " 의 해당 게시글이 존재하지 않습니다.")));

        comment.confirmParent(commentRepository.findById(parentId).orElseThrow(() -> new CommentNotFoundException("ID = " + postId + " 의 해당 부모 댓글이 존재하지 않습니다.")));

        commentRepository.save(comment);
    }

    // 댓글 수정
    @Transactional
    public Long updateComment(Long id, CommentCreateForm dto) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        comment.update(dto.getContent(), dto.isSecret());
        commentRepository.save(comment);
        return comment.getId();
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("ID = " + id + " 의 해당 댓글이 존재하지 않습니다."));
        commentRepository.delete(comment);
    }
}