async function toggleLike(postId, categoryTitle) {
    const likeIcon = document.getElementById('like-icon');
    const likeCountSpan = document.getElementById('like-count');
    let liked = likeIcon.textContent === '♡';

    liked = !liked;

    if (liked) {
        likeIcon.textContent = '❤️';
    } else {
        likeIcon.textContent = '♡';
    }

    try {
        const response = await fetch(`/api/post/like/${categoryTitle}/${postId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ liked })
        });

        if (response.ok) {
            const data = await response.json();
            likeCountSpan.textContent = `좋아요 ${data.likeCount}`;
        } else {
            console.error('Failed to like post');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}
