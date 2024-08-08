document.addEventListener('DOMContentLoaded', function () {
    const keywordForm = document.getElementById('keywordForm');
    const keywordInput = document.getElementById('keywordInput');
    const keywordsList = document.getElementById('keywords');
    const keywordCount = document.getElementById('keywordCount');

    keywordForm.addEventListener('submit', function (event) {
        event.preventDefault();

        const keyword = keywordInput.value.trim();
        if (keyword) {
            addKeyword(keyword);
        }
    });

    function addKeyword(keyword) {
        console.log("키워드 추가 요청: " + keyword);
        fetch('/api/user/keywords', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(keyword)
        })
            .then(response => {
                if (response.ok) {
                    console.log("서버 응답 성공");
                    return response.json();
                }
                throw new Error('네트워크 응답이 좋지 않습니다.');
            })
            .then(data => {
                updateKeywordsList(data);
                keywordInput.value = ''; // 입력 필드 초기화
            })
            .catch(error => {
                console.error('키워드를 추가하는 중에 오류가 발생했습니다:', error);
            });
    }

    function deleteKeyword(keyword) {
        console.log("키워드 삭제 요청: " + keyword);
        fetch('/api/user/keywords/delete', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(keyword)
        })
            .then(response => {
                if (response.ok) {
                    console.log("서버 응답 성공");
                    return response.json();
                }
                throw new Error('네트워크 응답이 좋지 않습니다.');
            })
            .then(data => {
                updateKeywordsList(data);
            })
            .catch(error => {
                console.error('키워드를 삭제하는 중에 오류가 발생했습니다:', error);
            });
    }

    function updateKeywordsList(keywords) {
        keywordsList.innerHTML = '';
        keywords.forEach(keyword => {
            const div = document.createElement('div');
            div.className = 'keyword-tag';
            div.textContent = keyword;
            div.addEventListener('click', function () {
                if (confirm(`키워드 "${keyword}"을(를) 삭제하시겠습니까?`)) {
                    deleteKeyword(keyword);
                }
            });
            keywordsList.appendChild(div);
        });
        keywordCount.textContent = keywords.length;
    }

    // 초기 키워드 목록 불러오기
    fetch('/api/user/keywords')
        .then(response => response.json())
        .then(data => updateKeywordsList(data))
        .catch(error => console.error('키워드 목록을 불러오는 중에 오류가 발생했습니다:', error));
});