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

    fetch(`/${categoryTitle}/${postCategoryTitle}/${postId}/comments?sort=${order}`)
        .then(response => response.json())
        .then(data => {
            const commentList = document.getElementById('comment-list');
            commentList.innerHTML = ''; // 기존 댓글 비우기
            data.forEach(comment => {
                const commentElement = document.createElement('div');
                commentElement.className = 'comment';
                commentElement.innerHTML = `
                    <div class="comment-content">
                        <div class="comment-header">
                            <img src="/images/logo.png" class="profile-img" alt="프로필 이미지">
                            <div>
                                <p class="author">${comment.user.nickname}</p>
                                <p class="date">${new Date(comment.createAt).toLocaleString()}</p>
                            </div>
                        </div>
                        <p>${comment.content}</p>
                    </div>
                `;
                commentList.appendChild(commentElement);
            });
        });
}
