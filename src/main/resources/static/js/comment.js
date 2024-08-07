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
    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('sort', order);
    window.location.search = urlParams.toString();
}