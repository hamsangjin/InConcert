let stompClient = null;
let isConnected = false;

window.onload = function() {
    let chatList = document.getElementById('chat');
    chatList.scrollTop = chatList.scrollHeight;
};

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
            // 알림 구독
            subscribeToNotifications(userId);

            // 강퇴 알림 구독
            stompClient.subscribe('/topic/chat/kicked/' + userId, function (message) {
                alert('채팅방에서 강퇴되었습니다.');
                window.location.href = '/chat/list';
            });

            // 채팅방 메시지 및 알림 구독
            if (chatRoomId) {
                subscribeToTopics(chatRoomId);

                // 이미 입장 메시지를 보냈는지 확인 (로컬 스토리지나 상태 변수에서 확인)
                const hasEntered = localStorage.getItem(`entered_${chatRoomId}_${username}`);

                if (!hasEntered) {
                    sendEnterMessage(username, chatRoomId); // 처음 입장 시에만 메시지 전송
                    localStorage.setItem(`entered_${chatRoomId}_${username}`, true); // 입장 플래그 저장
                }

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
        location.assign(location.href);
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
            'message': '' // 빈 메시지로 보냄
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

// 채팅방 나가기 버튼을 눌렀을 때 실행되는 함수
function confirmLeaveChatRoom() {
    if (confirm("이 채팅방을 나가시겠습니까?")) {
        leaveChatRoom();
    }
}

// 채팅방 나가기 로직
function leaveChatRoom() {
    const chatRoomId = document.getElementById('chatRoomId').value;

    // API 요청으로 채팅방 나가기
    fetch(`/api/chat/leave/${chatRoomId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) {
                alert("채팅방을 나갔습니다.");
                window.location.href = "/chat/list"; // 나가기 후 채팅창 목록으로 이동
            } else {
                // 예외 발생 시 오류 메시지를 받아 alert로 표시
                return response.text().then(errorMessage => {
                    alert(errorMessage);
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("오류가 발생했습니다. 다시 시도해 주세요.");
        });
}

// 유저 목록을 보여주거나 숨기는 함수
function toggleUserList() {
    const userListContainer = document.getElementById('user-list-container');
    const hamburgerBtn = document.getElementById('hamburger-btn').getBoundingClientRect();

    userListContainer.style.top = (hamburgerBtn.bottom + window.scrollY) + 'px';
    userListContainer.style.left = (hamburgerBtn.left + window.scrollX) + 'px';

    if (userListContainer.style.display === 'none' || userListContainer.style.display === '') {
        loadUserList();
        userListContainer.style.display = 'block';
    } else {
        userListContainer.style.display = 'none';
    }
}

// 유저 목록을 불러오는 함수
function loadUserList() {
    const chatRoomId = document.getElementById('chatRoomId').value;
    const hostUserId = document.getElementById('hostUserId').value; // 현재 호스트의 ID
    const currentUserId = document.getElementById('userId').value; // 현재 로그인한 유저의 ID

    // API 호출로 유저 목록을 가져옴
    fetch(`/api/chat/users/${chatRoomId}`)
        .then(response => response.json())
        .then(users => {
            const userList = document.getElementById('user-list');
            userList.innerHTML = ''; // 기존 목록 초기화


            // 유저 목록을 순회하며 리스트 아이템으로 추가
            users.forEach(user => {
                const userContainer = document.createElement('div');
                const listItem = document.createElement('li');

                // 호스트인 경우 'username (호스트)'로 표시
                if (user.id == hostUserId) {
                    listItem.textContent = `${user.username} (호스트)`;
                } else {
                    listItem.textContent = user.username;
                }

                // 현재 유저가 호스트인 경우, 본인을 제외한 다른 유저에게만 강퇴 버튼 추가
                if (currentUserId == hostUserId && user.id != currentUserId) {
                    const kickButton = document.createElement('button');
                    kickButton.textContent = "강퇴";
                    kickButton.onclick = function () {
                        kickUserFromChatRoom(user.id); // 강퇴 함수 호출
                    };
                    listItem.appendChild(kickButton);
                }

                const userProfileImg = document.createElement('img');
                userProfileImg.src = user.profileImage;

                userContainer.appendChild(userProfileImg);
                userContainer.appendChild(listItem);

                userList.appendChild(userContainer);
            });
        })
        .catch(error => {
            console.error('Error:', error);
            alert("유저 목록을 불러오는 데 실패했습니다.");
        });
}

// 강퇴 함수
function kickUserFromChatRoom(kickedUserId) {
    const chatRoomId = document.getElementById('chatRoomId').value;
    console.log(kickedUserId + " ?????");

    // 강퇴 API 호출
    fetch(`/api/chat/kick/${chatRoomId}/${kickedUserId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (response.ok) {
                alert("유저가 강퇴되었습니다.");
                loadUserList(); // 유저 목록 새로고침
            } else {
                return response.text().then(errorMessage => {
                    alert(errorMessage);
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert("유저 강퇴에 실패했습니다.");
        });
}

// 페이지가 로딩될 때 유저 목록도 함께 가져오기
window.addEventListener('load', function () {
    const userId = document.getElementById('userId').value;
    const chatRoomId = document.getElementById('chatRoomId').value;

    if (userId && chatRoomId) {
        connect(userId, chatRoomId);
    } else {
        console.error("Failed to connect: userId or chatRoomId is missing.");
    }
});

window.addEventListener('beforeunload', function () {
    const username = document.getElementById('username').value;
    const chatRoomId = document.getElementById('chatRoomId').value;

    // 이미 퇴장한 경우 퇴장 메시지를 보내지 않음
    const hasLeft = localStorage.getItem(`left_${chatRoomId}_${username}`);

    if (!hasLeft) {
        sendLeaveMessage(username, chatRoomId); // 처음 퇴장 시에만 메시지 전송
        localStorage.setItem(`left_${chatRoomId}_${username}`, true); // 퇴장 플래그 설정
    }
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