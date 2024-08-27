let eventSource;

const popularPosts = {
    'musical': null,
    'concert': null,
    'theater': null,
    'etc': null
};

const infoPosts = [];
const MAX_INFO_POSTS = 8;

function connectSSE() {
    eventSource = new EventSource('/api/crawling/progress');

    eventSource.onopen = function(event) {
        console.log("SSE connection opened");
    };

    eventSource.addEventListener('crawlingUpdate', function(event) {
        const post = JSON.parse(event.data);
        if (post.title === "Crawling started") {
            updateCrawlingStatus(true);
        } else if (post.title === "Crawling completed") {
            updateCrawlingStatus(false);
        }
    });

    eventSource.addEventListener('crawlingUpdate', handleCrawlingUpdate);
    eventSource.addEventListener('viewCountUpdate', handleViewCountUpdate);

    eventSource.onerror = function(error) {
        console.error("SSE Error:", error);
        eventSource.close();
    };
}

// function refreshData() {
//     fetch('/api/posts/latest')
//         .then(response => response.json())
//         .then(data => {
//             if (!isCrawling()) {
//                 infoPosts.length = 0;
//                 infoPosts.push(...data);
//                 renderUI();
//             }
//         })
//         .catch(error => console.error('Error:', error));
// }
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

function isCrawling() {
    const crawlingStatusElement = document.getElementById('crawling-status');
    return crawlingStatusElement && crawlingStatusElement.style.display === 'block';
}

setInterval(refreshData, 300000);

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

function updateSpecificPostUI(post) {
    const postElement = document.querySelector(`.post-title[href="/info/${post.postCategoryTitle}/${post.id}"]`);
    if (postElement) {
        postElement.innerHTML = `
            <span>[${getCategoryTitle(post.postCategoryTitle)}]</span>
            <span>${post.title}</span>
        `;
    }
}

function handleViewCountUpdate(event) {
    const update = JSON.parse(event.data);
    const post = infoPosts.find(p => p.id === update.id);
    if (post) {
        post.viewCount = update.viewCount;
        updateSpecificPostUI(post);
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
        <span>${getCategoryTitle(post.postCategoryTitle)}</span>
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
                <span>[${getCategoryTitle(post.postCategoryTitle)}]</span>
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

function loadFromStorage() {
    const savedInfoPosts = localStorage.getItem('infoPosts');
    const savedPopularPosts = localStorage.getItem('popularPosts');

    if (savedInfoPosts && !isCrawling()) {
        infoPosts.length = 0;
        const parsedInfoPosts = JSON.parse(savedInfoPosts);
        console.log("Loaded infoPosts from storage:", parsedInfoPosts);
        infoPosts.push(...parsedInfoPosts);
    }

    if (savedPopularPosts && !isCrawling()) {
        const parsedPopularPosts = JSON.parse(savedPopularPosts);
        console.log("Loaded popularPosts from storage:", parsedPopularPosts);
        Object.assign(popularPosts, parsedPopularPosts);
    }
}

// document.addEventListener("DOMContentLoaded", () => {
//     const isCrawling = document.getElementById('crawling-status').style.display !== 'none';
//     updateCrawlingStatus(isCrawling);
//     loadFromStorage();
//     renderUI();
//     connectSSE();
//     refreshData();
// });

document.addEventListener("DOMContentLoaded", () => {
    const isCrawling = document.getElementById('crawling-status').style.display !== 'none';
    updateCrawlingStatus(isCrawling);
    refreshData();  // 페이지 로드 시 항상 최신 데이터 가져오기
    connectSSE();

    // 주기적으로 데이터 새로고침
    setInterval(refreshData, 300000);
});