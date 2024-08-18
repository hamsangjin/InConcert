document.addEventListener("DOMContentLoaded", async function () {
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
        if (data.liked) {
            likeIcon.classList.remove('far');
            likeIcon.classList.add('fas');
        } else {
            likeIcon.classList.remove('fas');
            likeIcon.classList.add('far');
        }
    } else {
        console.error('올바르지 않은 요청입니다.');
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const errorMessageElement = document.getElementById("errorMessage");
    if (errorMessageElement && errorMessageElement.textContent.trim() !== "") {
        alert(errorMessageElement.textContent);
    }
});

function confirmDelete(button) {
    if (!confirm("이 포스트를 삭제하시겠습니까?")) {
        return false;
    }

    const hasChatRoom = button.getAttribute("data-has-chat-room") === 'true';
    if (hasChatRoom) {
        alert("연결된 채팅방이 있는 경우 포스트를 삭제할 수 없습니다.");
        return false;
    }

    return true;
}

async function toggleLike(button) {
    const postId = button.getAttribute('data-post-id');
    const categoryTitle = button.getAttribute('data-category-title');

    const likeIcon = document.getElementById('like-icon');
    const likeCountSpan = document.getElementById('like-count');
    let liked = likeIcon.classList.contains('far');

    const response = await fetch(`/api/${categoryTitle}/like/${postId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({liked})
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

    if (liked) {
        likeIcon.classList.remove('far');
        likeIcon.classList.add('fas');
        likeCountSpan.textContent = (parseInt(likeCountSpan.textContent) + 1).toString();
    } else {
        likeIcon.classList.remove('fas');
        likeIcon.classList.add('far');
        likeCountSpan.textContent = (parseInt(likeCountSpan.textContent) - 1).toString();
    }
}

function validateSearch() {
    let keyword = document.getElementById("search-input").value.trim();
    if (keyword.length < 2) {
        alert("검색어를 2자 이상 입력하세요.");
        return false;
    }
    return true;
}

// 동행 요청
function requestJoinChatRoom(button) {
    const chatRoomId = button.getAttribute("data-chat-room-id");

    fetch(`/api/chat/request-join/${chatRoomId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (response.ok) {
                return response.text(); // 성공 시 응답 처리
            } else if (response.status === 400) {
                throw new Error('이미 인원이 모집 완료된 포스트입니다.'); // 모집 완료된 경우
            } else {
                throw new Error('알 수 없는 오류가 발생했습니다. 다시 시도해주세요.'); // 기타 오류
            }
        })
        .then(data => {
            alert(data); // 요청 성공 시 메시지 출력
        })
        .catch(error => {
            alert(`${error.message}`); // 오류 메시지 출력
            console.error('There was a problem with the fetch operation:', error);
        });
}

// 1:1 채팅 요청
function requestOneToOneChat(button) {
    const receiverId = button.getAttribute("data-receiver-id");

    fetch(`/api/chat/request-one-to-one/${receiverId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (response.status === 409) {
                throw new Error('이미 해당 유저와의 채팅방이 존재합니다.');
            } else if (!response.ok) {
                throw new Error('알 수 없는 오류가 발생했습니다.');
            }
            return response.json(); // 채팅방 ID 반환
        })
        .then(chatRoomId => {
            alert("채팅방이 생성되었습니다.");
            window.location.href = `/chat/${chatRoomId}`; // 채팅방으로 이동
        })
        .catch(error => {
            alert(`${error.message}`);
            console.error('There was a problem with the fetch operation:', error);
        });
}