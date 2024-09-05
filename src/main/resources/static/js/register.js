// 이메일 및 인증번호 확인 상태 변수
let isEmailVerified = false;
let isCertificationVerified = false;

// 원래의 아이디를 추적
let originalUsername = document.getElementById('username').value;

// 실시간 유효성 검사 및 중복 확인 함수
function validateInput(inputElement, checkUrl, errorElement, errorMessage, validationRegex, validationMessage) {
    let timeoutId;
    inputElement.addEventListener('input', function () {
        clearTimeout(timeoutId);
        const value = this.value;
        this.setCustomValidity('');

        if (validationRegex && !validationRegex.test(value)) {
            errorElement.textContent = validationMessage;
            return;
        }

        errorElement.textContent = '';
        if (checkUrl) {
            timeoutId = setTimeout(() => {
                fetch(checkUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ [inputElement.name]: value })
                })
                    .then(response => response.json())
                    .then(data => {
                        errorElement.textContent = data.code !== "SU" ? errorMessage : '';
                    })
                    .catch(error => console.error('Error:', error));
            }, 500);
        }
    });
}

// 각 필드에 대한 유효성 검사 적용
validateInput(
    document.getElementById('username'),
    '/user/id-check',
    document.getElementById('username-error'),
    '이미 존재하는 아이디입니다.',
    /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{5,}$/,
    '아이디는 5자 이상으로 알파벳, 숫자를 조합해서 사용하세요.'
);

validateInput(
    document.getElementById('password'),
    null,
    document.getElementById('password-error'),
    '',
    /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/,
    '비밀번호는 8자 이상으로 알파벳, 숫자, 특수기호를 조합해서 사용하세요.'
);

validateInput(
    document.getElementById('password-confirm'),
    null,
    document.getElementById('password-confirm-error'),
    '비밀번호가 일치하지 않습니다.'
);

validateInput(
    document.getElementById('email'),
    null,
    document.getElementById('email-error'),
    '이메일 형식이 아닙니다.',
    /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    '유효한 이메일 형식을 입력해주세요.'
);

validateInput(
    document.getElementById('name'),
    null,
    document.getElementById('name-error'),
    '',
    /^[가-힣a-zA-Z]+$/,
    '이름에는 한글 또는 영어만 사용 가능합니다.'
);

validateInput(
    document.getElementById('nickname'),
    '/user/nickname-check',
    document.getElementById('nickname-error'),
    '이미 존재하는 닉네임입니다.',
    /^.{1,8}$/,
    '닉네임은 8자 이내로 입력해주세요.'
);

validateInput(
    document.getElementById('phoneNumber'),
    '/user/phone-number-check',
    document.getElementById('phoneNumber-error'),
    '',
    /^010\d{7,8}$/,
    '010으로 시작하는 번호를 숫자만 입력해주세요.'
);

// 아이디 필드의 변화 감지
document.getElementById('username').addEventListener('input', function () {
    const currentUsername = this.value;

    // 이메일 인증이 완료된 경우에만 이메일 인증 상태를 리셋
    if (currentUsername !== originalUsername && isEmailVerified) {
        isEmailVerified = false;
        isCertificationVerified = false;
        document.getElementById('email-error').textContent = '이메일 인증을 다시 진행해주세요.';
        originalUsername = currentUsername;
    }
});

// 비밀번호 확인
document.getElementById('password-confirm').addEventListener('input', function () {
    const password = document.getElementById('password').value;
    const errorElement = document.getElementById('password-confirm-error');
    errorElement.textContent = this.value !== password ? '비밀번호가 일치하지 않습니다.' : '';
});

// 이메일 인증 버튼 클릭 이벤트
document.getElementById('checkEmailBtn').addEventListener('click', function () {
    const email = document.getElementById('email').value;
    const username = document.getElementById('username').value;

    fetch('/user/email-check', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, username }),
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === "SU") {
                alert('사용 가능한 이메일입니다. 인증 번호를 전송합니다.');
                return fetch('/user/email-certification', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, username }),
                });
            } else {
                alert('이미 존재하는 이메일입니다.');
                throw new Error('이메일 중복');
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.code === "SU") {
                alert('인증 번호가 전송되었습니다.');
                isEmailVerified = true; // 이메일 인증 완료 상태로 변경
            } else {
                alert('인증 번호 전송에 실패했습니다.');
            }
        })
        .catch(error => {
            if (error.message !== '이메일 중복') {
                console.error('Error:', error);
                alert('처리 중 오류가 발생했습니다.');
            }
        });
});

// 인증번호 확인 버튼 클릭 이벤트
document.getElementById('checkCertificationBtn').addEventListener('click', function () {
    const certificationNumber = document.getElementById('certificationNumber').value;
    const email = document.getElementById('email').value;
    const username = document.getElementById('username').value;

    fetch('/user/check-certification', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, username, certificationNumber }),
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === "SU") {
                alert('인증되었습니다.');
                isCertificationVerified = true; // 인증 번호 확인 완료 상태로 변경
                // 인증 완료 시 메시지 삭제
                document.getElementById('email-error').textContent = ''; // 인증 완료 시 메시지 삭제
            } else {
                alert('인증 번호가 일치하지 않습니다.');
                isCertificationVerified = false;
            }
        })
        .catch(error => console.error('Error:', error));
});

// 회원가입 버튼 클릭 이벤트
document.getElementById('registerBtn').addEventListener('click', function () {
    const form = document.getElementById('registerForm');

    // 이메일과 인증번호가 확인되지 않았을 때
    if (!isEmailVerified) {
        alert('이메일 인증을 완료해주세요.');
        return;
    }

    if (!isCertificationVerified) {
        alert('인증번호 확인을 완료해주세요.');
        return;
    }

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    const formData = new FormData(form);
    const jsonData = Object.fromEntries(
        Array.from(formData.entries()).map(([key, value]) =>
            key === 'agreeTerms' ? [key, value === 'on'] : [key, value]
        )
    );

    fetch('/register', {
        method: 'POST',
        body: JSON.stringify(jsonData),
        headers: { 'Content-Type': 'application/json' },
    })
        .then(response => response.json())
        .then(data => {
            if (data.code === "SU") {
                alert('회원가입이 완료되었습니다.');
                window.location.href = '/loginform';
            } else {
                alert('회원가입에 실패했습니다. 다시 시도해주세요.');
                console.error('Error details:', data);
            }
        })
        .catch(error => {
            alert('회원가입 처리 중 오류가 발생했습니다.');
            console.error('Error:', error);
        });
});

// 생년월일 입력 제한
document.getElementById('birth').max = new Date().toISOString().split("T")[0];