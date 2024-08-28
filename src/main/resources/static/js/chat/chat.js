let stompClient = null;
let isConnected = false;

// 메시지 추가 후 스크롤을 아래로 내리는 함수
function scrollToBottom() {
    let chatList = document.getElementById('chat');
    chatList.scrollTop = chatList.scrollHeight;
}

window.onload = function() {
    scrollToBottom()
};

// STOMP 연결
function connect(userId, chatRoomId) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // 사용자 정보 로드
    const username = document.getElementById('username').value;
    const nickname = document.getElementById('nickname').value;

    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        isConnected = true;

        // 사용자 정보가 있는지 확인하고 연결 처리
        if (userId && username && nickname) {
            // 알림 구독
            subscribeToNotifications(userId);

            // 강퇴 알림 구독(message 매개변수 사용 여부 확인 필요)
            stompClient.subscribe('/topic/chat/kicked/' + userId, function (message) {
                alert('채팅방에서 강퇴되었습니다.');
                window.location.href = '/chat/list';
            });

            // 채팅방 메시지 및 알림 구독
            if (chatRoomId) {
                subscribeToTopics(chatRoomId);

                let isOneToOne = document.getElementById('isOneToOne').value;

                if(!isOneToOne){
                    // 이미 입장 메시지를 보냈는지 확인 (로컬 스토리지나 상태 변수에서 확인)
                    const hasEntered = localStorage.getItem(`entered_${chatRoomId}_${username}`);

                    if (!hasEntered) {
                        sendEnterMessage(username, nickname, chatRoomId); // 처음 입장 시에만 메시지 전송
                        localStorage.setItem(`entered_${chatRoomId}_${username}`, true); // 입장 플래그 저장
                    }
                }
            }
        } else {
            console.error("User information is missing.");
        }
    }, function (error) {
        console.error('Connection error:', error);
        isConnected = false;
    });
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
        scrollToBottom()
    });
}

// 알림 구독
function subscribeToNotifications(userId) {
    stompClient.subscribe('/topic/notifications/' + userId);
}

// 입장 메시지
function sendEnterMessage(username, nickname, chatRoomId) {
    if (!isConnected) {
        console.error('Cannot send message: Not connected');
        return;
    }

    stompClient.send("/app/chat/enterUser", {},
        JSON.stringify({
            'username': username,
            'nickname': nickname,
            'chatRoomId': chatRoomId,
            'message': '' // 빈 메시지 (입장 메시지는 공백)
        })
    );
}

// 메시지 전송
function sendMessage() {
    const username = document.getElementById('username').value;
    const nickname = document.getElementById('nickname').value;
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
                'nickname': nickname, // nickname 전송
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
function sendLeaveMessage(username, nickname, chatRoomId) {
    if (!isConnected) {
        console.error('Cannot send message: Not connected');
        return;
    }

    let isOneToOne = document.getElementById('isOneToOne').value;

    if(!isOneToOne) {
        // 퇴장 메시지를 전송할 때 username을 포함하여 전송
        stompClient.send("/app/chat/leaveUser", {},
            JSON.stringify({
                'username': username, // 퇴장하는 사용자의 username
                'nickname': nickname, // 퇴장하는 사용자의 username
                'chatRoomId': chatRoomId,
                'message': '' // 빈 메시지로 보냄
            })
        );
    }
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
    const chatRoomTitle = document.getElementById('chatRoomTitle').textContent;

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

                // 채팅방 제목이 '1:1 채팅'으로 시작하는 경우 호스트 표시와 강퇴 버튼을 생략
                if (chatRoomTitle.startsWith('1:1 채팅')) {
                    listItem.textContent = user.nickname;
                }
                else {
                    // 호스트인 경우 'nickname (호스트)'로 표시
                    if (user.id == hostUserId) {
                        listItem.textContent = `${user.nickname} (호스트)`;
                    } else {
                        listItem.textContent = user.nickname;
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
    const nickname = document.getElementById('nickname').value;
    const chatRoomId = document.getElementById('chatRoomId').value;
    const isOneToOne = document.getElementById('isOneToOne').value;
    if(!isOneToOne){
        // 이미 퇴장한 경우 퇴장 메시지를 보내지 않음
        const hasLeft = localStorage.getItem(`left_${chatRoomId}_${username}`);

        if (!hasLeft) {
            sendLeaveMessage(username, nickname, chatRoomId); // 처음 퇴장 시에만 메시지 전송
            localStorage.setItem(`left_${chatRoomId}_${username}`, true); // 퇴장 플래그 설정
        }
        disconnect();
    }
});

// 메시지 표시
function showMessage(message) {
    const chatContainer = document.getElementById('chat');

    if(!message.notice){
        const notMyMessage = document.createElement('div');
        notMyMessage.className = 'not-my-message';

        const messageHeader = document.createElement('div');
        messageHeader.className = 'message-header';

        const profileImg = document.createElement('img');
        profileImg.src = document.getElementById('profileImg').value;

        const nickname = document.createElement('p');
        nickname.innerText = message.nickname;

        const messageContent = document.createElement('div');
        messageContent.className = 'message-content';

        const messageValue = document.createElement('p');
        messageValue.innerText = message.message;

        const createdAt = document.createElement('p');
        const now = new Date();
        createdAt.innerText = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;

        messageHeader.append(profileImg, nickname);
        messageContent.append(messageValue, createdAt);
        notMyMessage.append(messageHeader, messageContent);
        chatContainer.appendChild(notMyMessage);
    } else {
        const noticeMessage = document.createElement('div');
        noticeMessage.className = 'notice'

        const messageValue = document.createElement('p');
        messageValue.innerText = message.message;

        noticeMessage.appendChild(messageValue);
        chatContainer.appendChild(noticeMessage);
    }
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}