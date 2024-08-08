document.addEventListener('DOMContentLoaded', function () {
    // 모든 삭제 버튼에 이벤트 리스너 추가
    document.querySelectorAll('.delete-button').forEach(button => {
        button.addEventListener('click', function (event) {
            event.stopPropagation(); // 클릭 이벤트가 상위 요소로 전파되지 않도록 합니다.
            const messageId = this.getAttribute('data-message-id');
            if (confirm('이 메시지를 삭제하시겠습니까?')) {
                fetch('/api/messages/' + messageId + '/delete', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                })
                    .then(response => {
                        if (response.ok) {
                            console.log("메시지 삭제 성공");
                            document.getElementById('message-' + messageId).remove();
                        } else {
                            throw new Error('메시지 삭제 실패');
                        }
                    })
                    .catch(error => {
                        console.error('메시지를 삭제하는 중에 오류가 발생했습니다:', error);
                    });
            }
        });
    });

    // 모든 메시지 내용에 이벤트 리스너 추가
    document.querySelectorAll('.message-content').forEach(content => {
        content.addEventListener('click', function () {
            const messageId = this.getAttribute('data-message-id');
            markAsReadAndNavigate(messageId);
        });
    });
});

async function markAsReadAndNavigate(messageId) {
    try {
        const response = await fetch('/api/messages/' + messageId + '/read', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.ok) {
            const messageElement = document.getElementById('message-' + messageId);
            messageElement.style.backgroundColor = '#ccc';
            // 클릭된 요소의 a 태그를 찾고 해당 링크로 이동
            const link = messageElement.querySelector('a');
            if (link) {
                window.location.href = link.href;
            }
        } else {
            console.error('Failed to mark as read');
        }
    } catch (error) {
        console.error('Error:', error);
    }
}