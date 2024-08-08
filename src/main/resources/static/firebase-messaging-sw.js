// [firebase-messaging-sw.js]
importScripts('https://www.gstatic.com/firebasejs/8.6.1/firebase-app.js');
importScripts('https://www.gstatic.com/firebasejs/8.6.1/firebase-messaging.js');

// Firebase 초기화
firebase.initializeApp({
    apiKey: "AIzaSyDuOVdHxDtX5JjCycRWz55jfN_eersgx3I",
    authDomain: "in-concert-f173f.firebaseapp.com",
    projectId: "in-concert-f173f",
    storageBucket: "in-concert-f173f.appspot.com",
    messagingSenderId: "231465264556",
    appId: "1:231465264556:web:7dea8ce97994e91c750609",
    measurementId: "G-2VR32Y6G8R"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
    console.log('[firebase-messaging-sw.js] Received background message ', payload);
    // Customize notification here
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: '/firebase-logo.png'
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});