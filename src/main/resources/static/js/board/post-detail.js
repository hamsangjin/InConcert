document.addEventListener("DOMContentLoaded", async function() {
    const likeIcon = document.getElementById('like-icon');
    const postId = likeIcon.getAttribute('data-post-id');
    const categoryTitle = likeIcon.getAttribute('data-category-title');
    const user = likeIcon.getAttribute('data-user');

    if(user != null) {
        try {
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
                console.error('Failed to get like status');
            }
        } catch (error) {
            console.error('Error:', error);
        }
    }
});

async function toggleLike(button) {
    const postId = button.getAttribute('data-post-id');
    const categoryTitle = button.getAttribute('data-category-title');
    const user = button.getAttribute('data-user');

    if(user == null) {
        window.location.href = '/loginform';
        return;
    }

    const likeIcon = document.getElementById('like-icon');
    const likeCountSpan = document.getElementById('like-count');
    let liked = likeIcon.textContent === '♡';

    if (liked){
        likeIcon.textContent = '♥';
        likeCountSpan.textContent = (parseInt(likeCountSpan.textContent) + 1).toString();
    }
    else{
        likeIcon.textContent = '♡';
        likeCountSpan.textContent = (parseInt(likeCountSpan.textContent) - 1).toString();
    }

    const response = await fetch(`/api/${categoryTitle}/like/${postId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ liked })
    });
}