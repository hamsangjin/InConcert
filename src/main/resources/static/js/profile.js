document.addEventListener("DOMContentLoaded", function() {
    // 공통 팝업 관련 요소
    const popups = [
        {
            popup: document.getElementById("profilePopup"),
            card: document.getElementById("profileCard"),
            closeBtn: document.getElementById("closePopup"),
            imageSelector: "#profileImage",
            ageField: "age-value-post",
            birthdateField: "birthdate-post",
        },
        {
            popup: document.getElementById("profilePopup-comment"),
            card: document.getElementById("profileCard-comment"),
            closeBtn: document.getElementById("closePopup-comment"),
            imageSelector: ".profile-img-comment",
        }
    ];

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

    function showPopup(popupObj, imageElement) {
        popupObj.popup.style.display = "block";
        setTimeout(() => {
            popupObj.card.classList.add("show");

            if (popupObj.birthdateField) {
                const birthdate = document.getElementById(popupObj.birthdateField).value;
                const age = calculateAge(birthdate);
                document.getElementById(popupObj.ageField).innerText = age;
            } else {
                const nickname = imageElement.getAttribute("data-nickname");
                const gender = imageElement.getAttribute("data-gender") === 'FEMALE' ? '여성' : '남성';
                const birthdate = imageElement.getAttribute("data-birth");
                const email = imageElement.getAttribute("data-email");
                const mbti = imageElement.getAttribute("data-mbti");
                const manner = imageElement.getAttribute("data-manner");
                const intro = imageElement.getAttribute("data-intro");
                const profileImage = imageElement.src;

                popupObj.popup.querySelector(".nickname").innerText = nickname;
                popupObj.popup.querySelector(".profile-img3").src = profileImage;
                popupObj.popup.querySelector(".popup-span.gender").innerText = gender;
                popupObj.popup.querySelector(".user-age").innerText = calculateAge(birthdate) + '세';
                popupObj.popup.querySelector(".popup-span.email").innerText = email;
                popupObj.popup.querySelector(".popup-mbti").innerText = mbti;
                popupObj.popup.querySelector(".popup-score").innerText = manner;
                popupObj.popup.querySelector(".intro-p").innerText = intro;

                const chatButton = popupObj.popup.querySelector(".chat-btn");
                const receiverId = imageElement.getAttribute("data-receiver-id");
                chatButton.setAttribute("data-receiver-id", receiverId);

                chatButton.onclick = function () {
                    requestOneToOneChat(chatButton);
                };
            }
        }, 0);
    }

    function closePopup(popupObj) {
        popupObj.card.classList.remove("show");
        setTimeout(() => {
            popupObj.popup.style.display = "none";
        }, 0);
    }

    popups.forEach(popupObj => {
        if (popupObj.closeBtn) {
            popupObj.closeBtn.onclick = function() {
                closePopup(popupObj);
            };
        }

        document.querySelectorAll(popupObj.imageSelector).forEach(imageElement => {
            imageElement.onclick = function() {
                showPopup(popupObj, imageElement);
            };
        });
    });

    window.addEventListener("click", function(event) {
        popups.forEach(popupObj => {
            if (popupObj.popup && event.target === popupObj.popup) {
                closePopup(popupObj);
            }
        });
    });
});