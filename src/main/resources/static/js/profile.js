document.addEventListener("DOMContentLoaded", function() {
    // 기존 프로필 팝업 관련 요소
    const profilePopup = document.getElementById("profilePopup");
    const profileImage = document.getElementById("profileImage");
    const closePopup = document.getElementById("closePopup");
    const profileCard = document.getElementById("profileCard");

    // 댓글 작성 유저 프로필 팝업 관련 요소
    const profilePopupComment = document.getElementById("profilePopup-comment");
    const closePopupComment = document.getElementById("closePopup-comment");
    const profileCardComment = document.getElementById("profileCard-comment");

    // 기존 프로필 팝업 이벤트
    profileImage.onclick = function() {
        profilePopup.style.display = "block";
        setTimeout(() => {
            profileCard.classList.add("show");
            const birthdate = document.getElementById('birthdate-post').value;
            const age = calculateAge(birthdate);
            document.getElementById('age-value-post').innerText = age;
        }, 0); // 약간의 딜레이를 주어 애니메이션이 적용되도록 함
    }

    closePopup.onclick = function() {
        profileCard.classList.remove("show");
        setTimeout(() => {
            profilePopup.style.display = "none";
        }, 0); // 카드 애니메이션이 끝난 후 팝업을 숨김 (0ms for smooth transition)
    }

    // 댓글 작성 유저 프로필 팝업 이벤트
    document.querySelectorAll(".profile-img-comment").forEach(profileImageComment => {
        profileImageComment.onclick = function() {
            const nickname = this.getAttribute("data-nickname");
            const gender = this.getAttribute("data-gender") === 'FEMALE' ? '여성' : '남성';
            const birthdate = this.getAttribute("data-birth");
            const email = this.getAttribute("data-email");
            const mbti = this.getAttribute("data-mbti");
            const manner = this.getAttribute("data-manner");
            const intro = this.getAttribute("data-intro");
            const profileImage = this.src;

            profilePopupComment.querySelector(".nickname").innerText = nickname;
            profilePopupComment.querySelector(".profile-img3").src = profileImage;
            profilePopupComment.querySelector(".popup-span.gender").innerText = gender;
            profilePopupComment.querySelector(".user-age").innerText = calculateAge(birthdate)+'세';
            profilePopupComment.querySelector(".popup-span.email").innerText = email;
            profilePopupComment.querySelector(".popup-mbti").innerText = mbti;
            profilePopupComment.querySelector(".popup-score").innerText = manner;
            profilePopupComment.querySelector(".intro-p").innerText = intro;

            profilePopupComment.style.display = "block";
            setTimeout(() => {
                profileCardComment.classList.add("show");
            }, 0); // 약간의 딜레이를 주어 애니메이션이 적용되도록 함
        }
    });

    closePopupComment.onclick = function() {
        profileCardComment.classList.remove("show");
        setTimeout(() => {
            profilePopupComment.style.display = "none";
        }, 0); // 카드 애니메이션이 끝난 후 팝업을 숨김 (0ms for smooth transition)
    }

    window.onclick = function(event) {
        if (event.target == profilePopup) {
            profileCard.classList.remove("show");
            setTimeout(() => {
                profilePopup.style.display = "none";
            }, 0); // 카드 애니메이션이 끝난 후 팝업을 숨김 (0ms for smooth transition)
        }

        if (event.target == profilePopupComment) {
            profileCardComment.classList.remove("show");
            setTimeout(() => {
                profilePopupComment.style.display = "none";
            }, 0); // 카드 애니메이션이 끝난 후 팝업을 숨김 (0ms for smooth transition)
        }
    };
});

function calculateAge(birthdate) {
    const birthDateObj = new Date(birthdate);
    const today = new Date();
    let age = today.getFullYear() - birthDateObj.getFullYear();
    const monthDiff = today.getMonth() - birthDateObj.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDateObj.getDate())) {
        age--;
    }

    return age;
}