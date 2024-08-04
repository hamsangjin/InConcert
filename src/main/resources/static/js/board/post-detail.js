document.addEventListener("DOMContentLoaded", async function() {
    const likeIcon = document.getElementById('like-icon');
    const postId = likeIcon.getAttribute('data-post-id');
    const categoryTitle = likeIcon.getAttribute('data-category-title');

    // 해당 게시글을 좋아요 눌렀는지 여부에 따라 하트 다르게 출력하는 로직
    const response = await fetch(`/api/${categoryTitle}/like/status/${postId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    });

    if (response.ok) {
        const data = await response.json();
        likeIcon.textContent = data.liked ? '♥' : '♡';
    } else {
        console.error('올바르지 않은 요청입니다.');
    }
});

async function toggleLike(button) {
    const postId = button.getAttribute('data-post-id');
    const categoryTitle = button.getAttribute('data-category-title');

    const likeIcon = document.getElementById('like-icon');
    const likeCountSpan = document.getElementById('like-count');
    let liked = likeIcon.textContent === '♡';

    const response = await fetch(`/api/${categoryTitle}/like/${postId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ liked })
    });

    // 로그아웃 상태인 경우 로그인 폼으로 이동
    if (response.ok) {
        const data = await response.json();
        if (!data.liked) {
            window.location.href = '/loginform';
            return;
        }
    } else {
        console.error('올바르지 않은 요청입니다.');
    }

    if (liked){
        likeIcon.textContent = '♥';
        likeCountSpan.textContent = (parseInt(likeCountSpan.textContent) + 1).toString();
    }
    else{
        likeIcon.textContent = '♡';
        likeCountSpan.textContent = (parseInt(likeCountSpan.textContent) - 1).toString();
    }
}