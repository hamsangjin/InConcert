let stompClient = null;
let isConnected = false;

// STOMP 연결
// function connect(userId, chatRoomId) {
//     const socket = new SockJS('/ws');
//     stompClient = Stomp.over(socket);
//
//     stompClient.connect({}, function (frame) {
//         console.log('Connected: ' + frame);
//         isConnected = true;
//
//         subscribeToNotifications(userId); // 사용자 알림 구독
//
//         // 채팅방 메시지 구독
//         if (chatRoomId) {
//             subscribeToTopics(chatRoomId);
//             sendEnterMessage(userId, chatRoomId);
//
//             // 로컬 스토리지에 저장된 알림 확인 및 표시
//             loadNotificationsFromLocalStorage(chatRoomId);
//         }
//     }, function (error) {
//         console.error('Connection error:', error);
//         isConnected = false;
//     });
// }
// STOMP 연결
function connect(userId, chatRoomId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // 사용자 정보 로드
    const username = document.getElementById('username').value;

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        isConnected = true;

        // 사용자 정보가 있는지 확인하고 연결 처리
        if (userId && username) {
            subscribeToNotifications(userId);

            if (chatRoomId) {
                subscribeToTopics(chatRoomId);
                sendEnterMessage(username, chatRoomId); // 사용자 정보 포함하여 메시지 전송
                fetchNotificationsFromServer(chatRoomId); // 서버에서 알림 가져오기
            }
        } else {
            console.error("User information is missing.");
        }
    }, function (error) {
        console.error('Connection error:', error);
        isConnected = false;
    });
}

// 서버에서 알림 목록 가져오기
function fetchNotificationsFromServer(chatRoomId) {
    fetch(`/api/notifications/${chatRoomId}`)
        .then(response => response.json())
        .then(notifications => {
            notifications.forEach(notification => {
                showNotificationConfirm(notification);
            });
        })
        .catch(error => console.error('Error fetching notifications:', error));
}

function subscribeToTopics(chatRoomId) {
    if (!isConnected) {
        console.error('Cannot subscribe: Not connected');
        return;
    }

    // 채팅 메시지 구독
    stompClient.subscribe('/topic/chat/room/' + chatRoomId, function (message) {
        console.log('Message received:', message.body);
        showMessage(JSON.parse(message.body));
    });

    // 참가 요청 승인 알림 구독
    const userId = document.getElementById('userId').value;
    stompClient.subscribe('/topic/notifications/' + userId, function (message) {
        console.log('Approval notification received:', message.body);
        const notification = JSON.parse(message.body);

        if (notification.chatRoomId === chatRoomId) {
            // 알림 메시지를 사용자에게 확인 창으로 보여줌
            const userResponse = confirm(notification.message + "\n승인하려면 확인을 누르세요.");

            if (userResponse) {
                // 사용자가 "확인"을 눌렀을 때 입장 승인 API 호출
                approveJoinRequest(chatRoomId, userId);
            } else {
                console.log('사용자가 요청을 거부했습니다.');
            }
        }
    });
}

// 알림 구독
function subscribeToNotifications(userId) {
    stompClient.subscribe('/topic/notifications/' + userId, function (message) {
        console.log('Approval notification received:', message.body);
        const notification = JSON.parse(message.body);

        // 로컬 스토리지에 메시지 저장
        saveNotificationToLocalStorage(notification);

        // 사용자가 채팅방에 있으면 바로 confirm 창을 띄움
        if (isUserInChatRoom(notification.chatRoomId)) {
            showNotificationConfirm(notification);
        }
    });
}

// 로컬 스토리지에 알림 저장
function saveNotificationToLocalStorage(notification) {
    let notifications = JSON.parse(localStorage.getItem('notifications')) || [];
    notifications.push(notification);
    localStorage.setItem('notifications', JSON.stringify(notifications));
}

// 로컬 스토리지에서 알림 불러오기
// function loadNotificationsFromLocalStorage(chatRoomId) {
//     let notifications = JSON.parse(localStorage.getItem('notifications')) || [];
//     notifications.forEach(notification => {
//         if (notification.chatRoomId === chatRoomId) {
//             showNotificationConfirm(notification);
//         }
//     });
//
//     // 표시된 알림은 로컬 스토리지에서 제거
//     notifications = notifications.filter(notification => notification.chatRoomId !== chatRoomId);
//     localStorage.setItem('notifications', JSON.stringify(notifications));
// }

// 알림창 표시 (승인 요청 시)
function showNotificationConfirm(notification) {
    const userResponse = confirm(notification.message + "\n이 사용자의 입장을 승인하시겠습니까?");
    if (userResponse) {
        approveJoinRequest(notification.chatRoomId, notification.userId);
    } else {
        console.log('사용자가 요청을 거부했습니다.');
    }
}

// 유저가 채팅방에 있는지 확인
function isUserInChatRoom(chatRoomId) {
    const currentChatRoomId = document.getElementById('chatRoomId')?.value;
    return currentChatRoomId && currentChatRoomId === String(chatRoomId);
}

// 입장 메시지
function sendEnterMessage(username, chatRoomId) {
    if (!isConnected) {
        console.error('Cannot send message: Not connected');
        return;
    }

    stompClient.send("/app/chat/enterUser", {},
        JSON.stringify({
            'username': username,
            'chatRoomId': chatRoomId,
            'message': '' // 빈 메시지 (입장 메시지는 공백)
        })
    );
}

// 메시지 전송
function sendMessage() {
    const username = document.getElementById('username').value;
    const chatRoomId = document.getElementById('chatRoomId').value;
    const message = document.getElementById('message').value;

    if (!isConnected) {
        console.error('Cannot send message: Not connected');
        return;
    }

    // 사용자 정보가 제대로 로드되었는지 확인
    if (username) {
        stompClient.send("/app/chat/sendMessage", {},
            JSON.stringify({
                'chatRoomId': chatRoomId,
                'username': username, // username 전송
                'message': message,
                'type': 'CHAT' // 메시지 타입은 CHAT
            })
        );

        document.getElementById('message').value = ''; // 메시지 입력 필드 비우기
    } else {
        console.error("Username is missing. Cannot send message.");
    }
}

// 퇴장 메시지
function sendLeaveMessage(username, chatRoomId) {
    if (!isConnected) {
        console.error('Cannot send message: Not connected');
        return;
    }

    // 퇴장 메시지를 전송할 때 username을 포함하여 전송
    stompClient.send("/app/chat/leaveUser", {},
        JSON.stringify({
            'username': username, // 퇴장하는 사용자의 username
            'chatRoomId': chatRoomId,
            'message': username + '님이 퇴장하셨습니다.' // 퇴장 메시지
        })
    );
}

// 승인 요청 전송
function approveJoinRequest(chatRoomId, userId) {
    fetch(`/api/chat/approve-join/${chatRoomId}/${userId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        mode: 'no-cors'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('승인 요청 실패: ' + response.statusText);
            }
            return response.text();
        })
        .then(data => {
            alert("사용자의 입장 요청을 승인했습니다.");
        })
        .catch(error => {
            console.error('승인 요청 중 문제가 발생했습니다:', error);
        });
}

// 페이지가 로딩될 때 유저와 채팅방 정보 가져오기
window.addEventListener('load', function () {
    const userId = document.getElementById('userId').value;
    const chatRoomId = document.getElementById('chatRoomId').value;

    // 사용자 정보가 존재할 때만 STOMP 연결을 시도
    if (userId && chatRoomId) {
        connect(userId, chatRoomId);
    } else {
        console.error("Failed to connect: userId or chatRoomId is missing.");
    }
});

window.addEventListener('beforeunload', function () {
    const username = document.getElementById('username').value;
    const chatRoomId = document.getElementById('chatRoomId').value;
    sendLeaveMessage(username, chatRoomId);
    disconnect();
});

// 메시지 표시
function showMessage(message) {
    const messageElement = document.createElement('li');
    messageElement.className = 'chat-message';

    // 입퇴장 메시지인 경우
    if (message.type === 'ENTER' || message.type === 'LEAVE') {
        messageElement.innerText = `${message.message}`;
    }
    // 일반 채팅 메시지
    else {
        messageElement.innerText = `${message.username}: ${message.message}`;
    }
    document.getElementById('chat').appendChild(messageElement);
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}