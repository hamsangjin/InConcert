document.addEventListener("DOMContentLoaded", function () {
    const userId = document.getElementById("userId").value;

    // STOMP 클라이언트 연결 설정
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);

        // 사용자별로 알림을 구독
        stompClient.subscribe(`/topic/notifications/${userId}`, function (notification) {
            const messageBody = JSON.parse(notification.body);

            // 실시간 알림이 들어오면 확인
            const isRejection = messageBody.message.includes('입장이 거절되었습니다');
            const requestItem = createNotificationItem(messageBody.message, messageBody.chatRoomId, messageBody.id, isRejection);
            const requestList = document.getElementById('requestList');
            requestList.appendChild(requestItem);
        });
    });

    // 기존 요청 목록 가져오기
    fetch(`/api/notifications/requestlist?userId=${userId}`)
        .then(response => response.json())
        .then(data => {
            const requestList = document.getElementById('requestList');
            data.forEach(request => {
                const isRejection = request.message.includes('입장이 거절되었습니다');
                const requestItem = createNotificationItem(request.message, request.chatRoomId, request.id, isRejection);
                requestList.appendChild(requestItem);
            });
        });
});

// 알림 항목을 생성하여 화면에 추가하는 함수
function createNotificationItem(message, chatRoomId, notificationId, isRejection) {
    const requestItem = document.createElement('li');
    requestItem.className = 'request-item';

    let innerHTML = `<span>${message}</span><div>`;

    // 알림이 거절되었을 경우 삭제 버튼만 표시
    if (isRejection) {
        innerHTML += `<button onclick="deleteNotification(${notificationId})">삭제</button>`;
    } else {
        // 요청일 경우 승낙/거절 버튼 표시
        innerHTML += `
            <button onclick="acceptRequest(${chatRoomId}, ${notificationId})">승낙</button>
            <button onclick="rejectRequest(${chatRoomId}, ${notificationId})">거절</button>`;
    }

    innerHTML += `</div>`;
    requestItem.innerHTML = innerHTML;

    return requestItem;
}

// 요청 승낙
function acceptRequest(chatRoomId, notificationId) {
    fetch(`/api/chat/approve-join/${chatRoomId}/${notificationId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (response.ok) {
                alert('요청이 승인되었습니다.');
                location.reload(); // 페이지 새로고침
            } else {
                alert('Failed to accept request');
            }
        });
}

// 요청 거절
function rejectRequest(chatRoomId, notificationId) {
    fetch(`/api/chat/reject-join/${chatRoomId}/${notificationId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (response.ok) {
                alert('요청이 거절되었습니다.');
                location.reload(); // 페이지 새로고침
            } else {
                alert('Failed to reject request');
            }
        });
}

// 알림 삭제
function deleteNotification(notificationId) {
    fetch(`/api/chat/notifications/${notificationId}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
        .then(response => {
            if (response.ok) {
                alert('알림이 삭제되었습니다.');
                location.reload(); // 페이지 새로고침
            } else {
                alert('Failed to delete notification');
            }
        });
}
