let eventSource;

const popularPosts = {
    'musical': null,
    'concert': null,
    'theater': null,
    'etc': null
};

const infoPosts = [];

// sse 연결
function connectSSE() {
    eventSource = new EventSource('/api/crawling/progress');

    eventSource.onopen = function() {
        console.log("SSE connection opened");
    };

    eventSource.addEventListener('crawlingUpdate', handleCrawlingUpdate);

    eventSource.onerror = function(error) {
        console.error("SSE Error:", error);
        eventSource.close();
    };
}

// 데이터 새로고침
function refreshData() {
    Promise.all([
        fetch('/api/posts/latest').then(response => response.json()),
        fetch('/api/posts/popular').then(response => response.json())
    ]).then(([latestPosts, popularPostsData]) => {
        if (!isCrawling()) {
            infoPosts.length = 0;
            infoPosts.push(...latestPosts);

            popularPosts.musical = popularPostsData.find(post => post.postCategoryTitle === 'musical') || null;
            popularPosts.concert = popularPostsData.find(post => post.postCategoryTitle === 'concert') || null;
            popularPosts.theater = popularPostsData.find(post => post.postCategoryTitle === 'theater') || null;
            popularPosts.etc = popularPostsData.find(post => post.postCategoryTitle === 'etc') || null;

            renderUI();
        }
    }).catch(error => console.error('Error:', error));
}

// 스크래핑 판단
function isCrawling() {
    const crawlingStatusElement = document.getElementById('crawling-status');
    return crawlingStatusElement && crawlingStatusElement.style.display === 'block';
}

function updateCrawlingStatus(isCrawling) {
    const crawlingStatusElement = document.getElementById('crawling-status');
    const contentElement = document.getElementById('content');

    if (crawlingStatusElement && contentElement) {
        if (isCrawling) {
            crawlingStatusElement.style.display = 'block';
            contentElement.style.display = 'none';
        } else {
            crawlingStatusElement.style.display = 'none';
            contentElement.style.display = 'block';
        }
    }
}

function handleCrawlingUpdate(event) {
    const post = JSON.parse(event.data);
    console.log("Received post data:", post);

    if (post.title === "Crawling started") {
        updateCrawlingStatus(true);
        console.log("Crawling process started");
        return;
    } else if (post.title === "Crawling completed") {
        updateCrawlingStatus(false);
        if (!isCrawling()) {
            refreshData();
        }
        console.log("Crawling process completed:", post.content);
        return;
    }

    if (!post.id || !post.thumbnailUrl) {
        console.error("Invalid post data received:", post);
        return;
    }

    switch(post.postCategoryTitle) {
        case 'musical':
            popularPosts.musical = { ...post, id: post.id };
            break;
        case 'concert':
            popularPosts.concert = { ...post, id: post.id };
            break;
        case 'theater':
            popularPosts.theater = { ...post, id: post.id };
            break;
        case 'etc':
            popularPosts.etc = { ...post, id: post.id };
            break;
        default:
            console.error("Unknown post category:", post.postCategoryTitle);
            break;
    }

    renderPopularInfo();
}

// 렌더링
function renderUI() {
    renderPopularInfo();
    renderInfoPosts();
}

function renderPopularInfo() {
    const container = document.querySelector('.popular-info-img-container');
    if (!container) return;

    container.innerHTML = '';

    ['musical', 'concert', 'theater', 'etc'].forEach(category => {
        const post = popularPosts[category];
        if (post) {
            const element = createPopularInfoElement(post);
            container.appendChild(element);
        }
    });
}

function renderInfoPosts() {
    const container = document.querySelector('.board-container div:first-child');
    if (!container) return;

    container.innerHTML = `
        <div class="board-header">
            <h1>공연 소식</h1>
            <a href="/info"><p>+ 더보기</p></a>
        </div>
    `;

    infoPosts.forEach(post => {
        const element = createInfoPostElement(post);
        container.appendChild(element);
    });
}

function createPopularInfoElement(post) {
    console.log("Creating popular info element for category:", post.postCategoryTitle, "with post ID:", post.id);

    const element = document.createElement('div');
    element.className = 'popular-info';
    element.innerHTML = `
        <span>${getPostCategoryTitle(post.postCategoryTitle)}</span>
        <img class="popular-info-img" src="${post.thumbnailUrl}">
        <a href="/info/${post.postCategoryTitle}/${post.id}">
            <span>자세히 보기</span>
        </a>
    `;
    return element;
}

function createInfoPostElement(post) {
    console.log("Creating info post element with data:", post);
    const element = document.createElement('div');
    element.className = 'post-list';
    element.innerHTML = `
        <div class="post-left">
            <a class="post-title" href="/info/${post.postCategoryTitle}/${post.id}">
                <span>[${getPostCategoryTitle(post.postCategoryTitle)}]</span>
                <span>${post.title}</span>
            </a>
            <p class="comment-count">[${post.commentCount ?? 0}]</p>
        </div>
        <div class="post-right">
            <p>${formatDate(post.createdAt)}</p>
            <p>${post.viewCount}</p>
        </div>
    `;
    return element;
}

function getPostCategoryTitle(postCategoryTitle) {
    switch(postCategoryTitle) {
        case 'musical': return '뮤지컬';
        case 'concert': return '콘서트';
        case 'theater': return '연극';
        case 'etc': return '기타';
        default: return postCategoryTitle;
    }
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

// 새로고침 후에도 정상 렌더링
function checkCrawlingStatus() {
    fetch('/api/crawling/status')
        .then(response => response.json())
        .then(isCrawling => {
            const crawlingStatusElement = document.getElementById('crawling-status');
            const contentElement = document.getElementById('content');

            if (isCrawling) {
                crawlingStatusElement.style.display = 'block';
                contentElement.style.display = 'none';
            } else {
                crawlingStatusElement.style.display = 'none';
                contentElement.style.display = 'block';
                refreshData();  // 데이터 새로고침
            }
        });
}

function runFunctionsAtMidnight() {
    const now = new Date();
    const targetTime = new Date();
    targetTime.setHours(0, 3, 0, 0); // 오전 12시 3분으로 설정

    // 만약 현재 시간이 12시 3분 이후라면, 다음 날로 설정
    if (now > targetTime) {
        targetTime.setDate(targetTime.getDate() + 1);
    }

    const timeUntilTarget = targetTime - now;

    setTimeout(() => {
        refreshData();
        checkCrawlingStatus();
        // 다음 날 같은 시간에 다시 설정
        runFunctionsAtMidnight();
    }, timeUntilTarget);
}

document.addEventListener("DOMContentLoaded", () => {
    const isCrawling = document.getElementById('crawling-status').style.display !== 'none';
    updateCrawlingStatus(isCrawling);
    refreshData();  // 페이지 로드 시 항상 최신 데이터 가져오기
    connectSSE();
    checkCrawlingStatus();

    // 12시 3분에 주기적으로 새로고침
    runFunctionsAtMidnight();
});