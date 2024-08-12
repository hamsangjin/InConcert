function toggleReplyForm(commentId) {
    const replyForm = document.getElementById('reply-form-' + commentId);
    if (replyForm) {
        replyForm.style.display = (replyForm.style.display === 'none' || replyForm.style.display === '') ? 'block' : 'none';
    } else {
        console.error('Reply form not found for commentId: ' + commentId);
    }
}

function hideReplyForm(commentId) {
    const replyForm = document.getElementById('reply-form-' + commentId);
    if (replyForm) {
        replyForm.style.display = 'none';
    } else {
        console.error('Reply form not found for commentId: ' + commentId);
    }
}

function toggleReplies(commentId) {
    const replies = document.getElementById('replies-' + commentId);
    if (replies) {
        replies.style.display = (replies.style.display === 'none' || replies.style.display === '') ? 'block' : 'none';
    } else {
        console.error('Replies not found for commentId: ' + commentId);
    }
}

function toggleEditForm(commentId) {
    const commentContent = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);
    if (commentContent && editForm) {
        if (editForm.style.display === 'none' || editForm.style.display === '') {
            commentContent.style.display = 'none';
            editForm.style.display = 'block';
        } else {
            commentContent.style.display = 'block';
            editForm.style.display = 'none';
        }
    } else {
        console.error('Edit form or comment content not found for commentId: ' + commentId);
    }
}

function cancelEdit(commentId) {
    const commentContent = document.getElementById('comment-content-' + commentId);
    const editForm = document.getElementById('comment-edit-form-' + commentId);
    if (commentContent && editForm) {
        commentContent.style.display = 'block';
        editForm.style.display = 'none';
    } else {
        console.error('Edit form or comment content not found for commentId: ' + commentId);
    }
}

function sortComments(order) {
    const commentsContainer = document.querySelector('.post-container'); // 전체 댓글 컨테이너 선택
    let comments = Array.from(commentsContainer.children);

    // 부모 댓글만 필터링 (parent가 null인 댓글)
    let rootComments = comments.filter(comment => {
        const parentElement = comment.querySelector('[data-comment-id]');
        const isRootComment = parentElement && !parentElement.hasAttribute('data-parent-id');
        return isRootComment;
    });

    // 부모 댓글을 정렬
    rootComments.sort((a, b) => {
        const dateA = new Date(a.querySelector('.date').textContent);
        const dateB = new Date(b.querySelector('.date').textContent);

        if (dateA.getTime() === dateB.getTime()) {
            // 시간이 동일하면 id 순서로 정렬
            const idA = parseInt(a.querySelector('[data-comment-id]').getAttribute('data-comment-id'));
            const idB = parseInt(b.querySelector('[data-comment-id]').getAttribute('data-comment-id'));
            return order === 'asc' ? idA - idB : idB - idA;
        }

        return order === 'asc' ? dateA - dateB : dateB - dateA;
    });

    // 정렬된 부모 댓글을 다시 DOM에 삽입
    rootComments.forEach(rootComment => {
        commentsContainer.appendChild(rootComment);

        // 자식 댓글들 (replies)을 부모 댓글 바로 아래에 삽입
        const replies = Array.from(commentsContainer.querySelectorAll(`[data-parent-id='${rootComment.getAttribute('data-comment-id')}']`));
        replies.forEach(reply => {
            commentsContainer.appendChild(reply);
        });
    });
}

function toggleReplies(commentId) {
    const repliesContainer = document.getElementById('replies-' + commentId);
    if (repliesContainer) {
        const isVisible = repliesContainer.style.display === 'block';
        repliesContainer.style.display = isVisible ? 'none' : 'block';

        if (!isVisible) {
            // 자식 댓글의 위치를 부모 댓글 아래에 유지
            const parentComment = document.querySelector(`[data-comment-id='${commentId}']`);
            if (parentComment) {
                const commentsContainer = parentComment.parentNode;
                commentsContainer.insertBefore(repliesContainer, parentComment.nextSibling);
            }
        }
    }
}
