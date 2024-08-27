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
    const chatRoomId = document.getElementById("chatRoomId").value;

    let userCount = 0;

    fetch(`/api/chat/users/${chatRoomId}`)
        .then(response => response.json())
        .then(users => {
            userCount = users.length;
        })
        .catch(error => {
            console.error('Error:', error);
            alert("유저 목록을 불러오는 데 실패했습니다.");
        });

    if (hasChatRoom && userCount >= 2) {
        alert("연결된 채팅방이 있는 경우 포스트를 삭제할 수 없습니다?");
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

function matchComplete() {
    if (!confirm("현재 채팅방 인원으로 동행 정보를 저장합니다.")) {
        return false;
    } else{
        alert("동행이 완료되었습니다.");
        return true;
    }
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
            return response.text().then(resMsg => {
                if (response.status === 409) {
                    if (resMsg.startsWith("이미 채팅방에")) {
                        throw new Error('이미 채팅방에 속해 있습니다.');
                    } else if (resMsg.startsWith("이미 동행 신청을 보냈습니다")) {
                        throw new Error('이미 동행 신청을 보냈습니다.');
                    }
                } else if (response.status === 400) {
                    throw new Error('이미 인원이 모집 완료된 포스트입니다.');
                } else if (!response.ok) {
                    throw new Error('알 수 없는 오류가 발생했습니다. 다시 시도해주세요.');
                }

                return resMsg; // 성공 시 응답 처리
            });
        })
        .then(data => {
            alert(data); // 요청 성공 시 메시지 출력
        })
        .catch(error => {
            alert(`${error.message}`); // 오류 메시지 출력
            console.error('API를 호출하는 데 오류가 생겼습니다.', error);
        });
}

// 1:1 채팅
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
            } else if(response.status === 400){
                throw new Error('본인에게는 1대1 채팅을 보낼 수 없습니다.');
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
            alert(error.message); // 오류 메시지 출력
            console.error('There was a problem with the fetch operation:', error);
        });
}

document.addEventListener('DOMContentLoaded', function () {
    var shareButton = document.getElementById('kakao-share-button');

    var title = shareButton.getAttribute('data-title');
    var description = shareButton.getAttribute('data-description');
    var imageUrl = shareButton.getAttribute('data-image-url');
    var webUrl = shareButton.getAttribute('data-web-url');

    // 문자열로 가져온 값을 정수로 변환
    var likeCount = parseInt(shareButton.getAttribute('data-likeCount'), 10);
    var commentCount = parseInt(shareButton.getAttribute('data-commentCount'), 10);

    // // 정규 표현식을 사용하여 <img>, <br>, <span>, <p> 태그 제거 및 한글만 추출
    var descriptionWithoutTags = description
        .replace(/<img[^>]*>/g, '')  // <img> 태그 제거
        .replace(/<br>/g, '')        // <br> 태그 제거
        .replace(/<\/?span[^>]*>/g, '')  // <span> 태그 제거
        .replace(/<\/?p[^>]*>/g, '')  // <p> 태그 제거
        .replace(/[^ㄱ-힣a-zA-Z\s:\d~.]/g, '');  // 한글, 알파벳, 숫자, 공백, 콜론(:), ~, . 제외한 문자 제거

    // 장소와 날짜 사이에 구분자 추가
    var formattedText = descriptionWithoutTags
        .replace(/(장소:\s*[^날]*)\s*(날짜:\s*[^]*)/, '$1\n$2');  // 장소와 날짜 사이에 " / " 추가

    var koreanText = formattedText.trim();

    // imageUrl이 유효하지 않으면 기본 이미지로 설정
    if (!imageUrl || imageUrl.trim() === "") {
        imageUrl = '/images/logo.png';  // 기본 이미지 경로
    }

    Kakao.Share.createDefaultButton({
        container: '#kakaotalk-sharing-btn',
        objectType: 'feed',
        content: {
            title: title,
            description: koreanText,
            imageUrl: imageUrl,  // 기본값이 포함된 imageUrl
            link: {
                webUrl: webUrl,
            },
        },
        social: {
            likeCount: likeCount,
            commentCount: commentCount,
        },
        buttons: [
            {
                title: '웹으로 보기',
                link: {
                    webUrl: webUrl,
                },
            }
        ],
    });
});

function shareTwitter() {
    var shareButton = document.getElementById('kakao-share-button');
    var title = shareButton.getAttribute('data-title');
    var description = shareButton.getAttribute('data-description'); // 전달할 텍스트
    var webUrl = shareButton.getAttribute('data-web-url'); // 전달할 URL

    // 정규 표현식을 사용하여 <img>, <br>, <span>, <p> 태그 제거 및 한글만 추출
    var descriptionWithoutTags = description
        .replace(/<img[^>]*>/g, '')  // <img> 태그 제거
        .replace(/<br>/g, '')        // <br> 태그 제거
        .replace(/<\/?span[^>]*>/g, '')  // <span> 태그 제거
        .replace(/<\/?p[^>]*>/g, '')  // <p> 태그 제거
        .replace(/[^ㄱ-힣a-zA-Z\s:\d~.]/g, '');  // 한글, 알파벳, 숫자, 공백, 콜론(:), ~, . 제외한 문자 제거

    // 장소와 날짜 사이에 구분자 추가
    var formattedText = descriptionWithoutTags
        .replace(/(장소:\s*[^날]*)\s*(날짜:\s*[^]*)/, '$1\n$2');  // 장소와 날짜 사이에 " / " 추가

    var koreanText = formattedText.trim();

    // 텍스트와 URL을 인코딩하여 Twitter의 intent URL에 추가
    var tweetContent = title + "\n" + koreanText + "\n" + webUrl;

    var tweetText = encodeURIComponent(tweetContent);

    window.open("https://twitter.com/intent/tweet?text=" + tweetText, '_blank', 'width=600,height=400');
}

function applyReplyStyles() {
    const comments = document.querySelectorAll('.comment-wrapper');

    comments.forEach(comment => {
        let level = 0;
        let parent = comment.closest('.replies');

        while (parent) {
            level++;
            parent = parent.parentElement.closest('.replies');
        }

        // 두 번째 답글부터 마진 적용
        if (level > 1) {
            comment.style.marginLeft = `${40 * (level - 1)}px`;
        }
    });
}

document.addEventListener('DOMContentLoaded', applyReplyStyles);

