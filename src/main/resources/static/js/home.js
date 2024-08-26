let eventSource;

let totalPostsCount = 0;  // 현재까지 받아온 데이터 수를 카운트하는 변수
const MAX_TOTAL_POSTS = 500;  // 최대 500개로 제한


const popularPosts = {
    'musical': null,
    'concert': null,
    'theater': null,
    'etc': null
};
const infoPosts = [];
const MAX_INFO_POSTS = 8;
let isInitialLoadComplete = false;

function connectSSE() {
    eventSource = new EventSource('/api/crawling/progress');

    eventSource.onopen = function(event) {
        console.log("SSE connection opened");
    };

    eventSource.addEventListener('crawlingUpdate', handleCrawlingUpdate);
    eventSource.addEventListener('crawlingBatchUpdate', handleCrawlingBatchUpdate);

    eventSource.onerror = function(error) {
        console.error("SSE Error:", error);
        eventSource.close();
    };
}

function handleCrawlingUpdate(event) {
    if (isInitialLoadComplete || totalPostsCount >= MAX_TOTAL_POSTS) return;
    try {
        const post = JSON.parse(event.data);
        totalPostsCount++;  // 카운트 증가
        updateUI(post);
    } catch (e) {
        console.error("Error parsing crawlingUpdate data", e);
    }
}

function handleCrawlingBatchUpdate(event) {
    if (isInitialLoadComplete || totalPostsCount >= MAX_TOTAL_POSTS) return;
    try {
        const posts = JSON.parse(event.data);
        posts.forEach(post => {
            if (totalPostsCount < MAX_TOTAL_POSTS) {
                totalPostsCount++;  // 각 포스트마다 카운트 증가
                updateUI(post);
            }
        });
    } catch (e) {
        console.error("Error parsing crawlingBatchUpdate data", e);
    }
}

function updateUI(post) {
    if (!isPostAlreadyAdded(post)) {
        updatePopularInfo(post);
        updateInfoPosts(post);
        saveToStorage();
        renderUI();
        checkInitialLoadComplete();
    }
}

function updatePopularInfo(post) {
    if (!popularPosts[post.postCategoryTitle]) {
        popularPosts[post.postCategoryTitle] = post;
    }
}

function updateInfoPosts(post) {
    if (infoPosts.length < MAX_INFO_POSTS) {
        infoPosts.push(post);
    }
}

function renderUI() {
    renderPopularInfo();
    renderInfoPosts();
}

function renderPopularInfo() {
    const container = document.querySelector('.popular-info-img-container');
    if (!container) return;

    container.innerHTML = '';
    ['musical', 'concert', 'theater', 'etc'].forEach(category => {
        if (popularPosts[category]) {
            const element = createPopularInfoElement(popularPosts[category]);
            container.appendChild(element);
        }
    });
}

function renderInfoPosts() {
    const container = document.querySelector('.board-container div:first-child');

    container.innerHTML = '';
    container.innerHTML = `
        <div class="board-header">
            <h1> 공연 소식 </h1>
            <a href="/info">
                <p> + 더보기</p>
            </a>
        </div>
    `;

    infoPosts.forEach(post => {
        const element = createInfoPostElement(post);
        container.appendChild(element);
    });
}

function createPopularInfoElement(post) {
    const element = document.createElement('div');
    element.className = 'popular-info';
    element.innerHTML = `
        <span>${getCategoryTitle(post.postCategoryTitle)}</span>
        <img class="popular-info-img" src="${post.thumbnailUrl}">
        <a href="/info/${post.postCategoryTitle}/${post.id}">
            <span>자세히 보기</span>
        </a>
    `;
    return element;
}


function createInfoPostElement(post) {
    const element = document.createElement('div');
    element.className = 'post-list';
    element.innerHTML = `
        <div class="post-left">
            <a class="post-title" href="/info/${post.postCategoryTitle}/${post.id}">
                <span>[${getCategoryTitle(post.postCategoryTitle)}]</span>
                <span>${post.title}</span>
            </a>
            <p class="comment-count">[${post.commentCount || 0}]</p>
        </div>
        <div class="post-right">
            <p>${formatDate(post.createdAt)}</p>
            <p>${post.viewCount}</p>
        </div>
    `;
    return element;
}

function getCategoryTitle(category) {
    switch(category) {
        case 'musical': return '뮤지컬';
        case 'concert': return '콘서트';
        case 'theater': return '연극';
        case 'etc': return '기타';
        default: return category;
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

function checkInitialLoadComplete() {
    if (Object.values(popularPosts).filter(Boolean).length === 4 && infoPosts.length === MAX_INFO_POSTS) {
        isInitialLoadComplete = true;
        if (eventSource) {
            eventSource.close();
            console.log("SSE connection closed after initial load");
        }
    }
}

function saveToStorage() {
    localStorage.setItem('infoPosts', JSON.stringify(infoPosts));
    localStorage.setItem('popularPosts', JSON.stringify(popularPosts));
}

function loadFromStorage() {
    const savedInfoPosts = localStorage.getItem('infoPosts');
    const savedPopularPosts = localStorage.getItem('popularPosts');

    if (savedInfoPosts) {
        infoPosts.length = 0;  // 기존 배열을 비웁니다.
        infoPosts.push(...JSON.parse(savedInfoPosts));
    }
    if (savedPopularPosts) {
        Object.assign(popularPosts, JSON.parse(savedPopularPosts));
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadFromStorage();  // 저장된 데이터를 불러옵니다.
    renderUI();  // UI를 렌더링합니다.
    connectSSE();  // SSE 연결을 시작합니다.
});
