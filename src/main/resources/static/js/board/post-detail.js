async function toggleLike(button) {
    const postId = button.getAttribute('data-post-id');
    const categoryTitle = button.getAttribute('data-category-title');
    const user = button.getAttribute('data-user');

    if (!user) {
        window.location.href = '/loginform';
        return;
    }

    const likeIcon = document.getElementById('like-icon');
    const likeCountSpan = document.getElementById('like-count');

    let liked = likeIcon.textContent === '♡';

    liked = !liked;

    if (liked)  likeIcon.textContent = '❤️';
    else        likeIcon.textContent = '♡';

    try {
        const response = await fetch(`/api/${categoryTitle}/like/${postId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ liked })
        });

        const responseText = await response.text();
        console.log('Response:', responseText);  // 응답 내용을 먼저 출력하여 확인

        if (response.ok) {
            // const data = JSON.parse(responseText);  // JSON 파싱
            // likeCountSpan.textContent = `좋아요 ${data.likeCount}`;
        } else {
            console.error('Failed to like post', responseText);
        }
    } catch (error) {
        console.error('Error:', error);
    }
}