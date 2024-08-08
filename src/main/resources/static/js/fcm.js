// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
    apiKey: "AIzaSyDuOVdHxDtX5JjCycRWz55jfN_eersgx3I",
    authDomain: "in-concert-f173f.firebaseapp.com",
    projectId: "in-concert-f173f",
    storageBucket: "in-concert-f173f.appspot.com",
    messagingSenderId: "231465264556",
    appId: "1:231465264556:web:7dea8ce97994e91c750609",
    measurementId: "G-2VR32Y6G8R"
};

// Firebase 초기화
firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// 현재 사용자 ID를 가져오는 함수
async function getCurrentUserId() {

    const response = await fetch('/api/user/current', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error('현재 사용자 정보를 가져오는 데 실패했습니다.');
    }
    const data = await response.json();
    return data.userId;
}

// FCM 권한 요청 및 토큰 등록
async function requestPermissionAndRegisterToken() {
    try {
        await messaging.requestPermission();
        const token = await messaging.getToken();
        console.log("FCM Token: ", token);

        const storedToken = localStorage.getItem('fcmToken');

        if (storedToken !== token) {
            const userId = await getCurrentUserId();
            console.log("User ID: ", userId);
            // 서버에 토큰 전달
            const response = await fetch('/api/token', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ userId: userId, token: token })
            });

            if (!response.ok) {
                throw new Error('토큰 저장 실패');
            }

            console.log("토큰이 성공적으로 저장되었습니다.");

            localStorage.setItem('fcmToken', token);
        } else {
            console.log("이미 저장된 토큰이 있습니다.");
        }
    } catch (err) {
        if (err.code === 'messaging/permission-blocked') {
            alert('알림 권한이 차단되었습니다. 알림을 사용하려면 브라우저 설정에서 알림 권한을 허용해주세요.');
        } else {
            console.log('FCM Token Error: ', err);
        }
    }
}

// 페이지 로드 시 토큰 요청 함수 호출
window.onload = function() {
    requestPermissionAndRegisterToken();
};

// 포그라운드 메시지 수신 및 처리
messaging.onMessage((payload) => {
    console.log('Message received. ', payload);
    // Customize notification here
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: payload.notification.icon
    };

    if (Notification.permission === 'granted') {
        new Notification(notificationTitle, notificationOptions);
    }
});