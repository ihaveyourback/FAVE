const $cover = document.getElementById('cover');
const $main = document.getElementById('main');
const $registerForm = document.getElementById('registerForm');
const $recoverForm = document.getElementById('recoverForm');
const $loginBtn = document.getElementById('loginBtn');


document.addEventListener("DOMContentLoaded", () => {
    const images = document.querySelectorAll(".main-image"); // 이미지 선택
    let currentIndex = 0; // 현재 표시 중인 이미지 인덱스

    // 슬라이더 자동 전환 함수
    function slideImages() {
        images[currentIndex].classList.remove("active"); // 현재 이미지 비활성화
        currentIndex = (currentIndex + 1) % images.length; // 다음 이미지
        images[currentIndex].classList.add("active"); // 다음 이미지 활성화
    }

    // 일정 시간마다 자동 실행 (3초 간격)
    setInterval(slideImages, 3000);
});


{
    const $content = $main.querySelector(':scope > .content');
    const $loginForm = $content.querySelector(':scope > .login-form');
    const $menu = $loginForm.querySelector(':scope > .menu');


    function formUnhide(form, focusElement) {
        $cover.show();
        form.show();
        form.reset();
        focusElement.focus();
    }

    // 이미 저장된 이메일을 설정하고 포커스
    if (typeof localStorage.getItem('rememberedEmail') === 'string') {
        $loginForm['email'].value = localStorage.getItem('rememberedEmail');
        $loginForm['rememberEmail'].checked = true;
        $loginForm['password'].focus();
    }


    $loginBtn.onclick = (e) => {
        e.preventDefault();
        $cover.onclick = () => {
            $cover.hide();
            $loginForm.hide();
        };
        formUnhide($loginForm, $loginForm['email']);
    };

    $loginForm['cancel'].onclick = () => {
        $cover.hide();
        $loginForm.hide();
    };


    // 로그인 폼 제출 처리
    $loginForm.onsubmit = (e) => {
        e.preventDefault();
        const $emailLabel = $loginForm.findLabel('email');
        const $passwordLabel = $loginForm.findLabel('password');
        $emailLabel.setValid($loginForm['email'].value.length >= 8 && $loginForm['email'].value.length <= 50);
        $passwordLabel.setValid($loginForm['password'].value.length >= 6 && $loginForm['password'].value.length <= 50, '올바른 비밀번호를 입력해 주세요.');

        if (!$emailLabel.isValid() || !$passwordLabel.isValid()) {
            return;
        }
        const xhr = new XMLHttpRequest();
        const url = new URL(location.href);

        // url.pathname = '/';
        url.pathname = '/api/login'; // JSON 응답을 반환하는 경로
        url.searchParams.set('email', $loginForm['email'].value);
        url.searchParams.set('password', $loginForm['password'].value);
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            Loading.hide()
            if (xhr.status < 200 || xhr.status >= 300) {
                Dialog.show({
                    title: '오류',
                    content: '요청을 전송하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요',
                    buttons: [{
                        text: '확인',
                        onclick: ($dialog) => Dialog.hide($dialog)
                    }]
                });
                return;
            }
            const response = JSON.parse(xhr.responseText);
            if (response['result'] === 'success') {
                if ($loginForm['rememberEmail'].checked) {
                    localStorage.setItem('rememberedEmail', $loginForm['email'].value);
                } else {
                    localStorage.removeItem('rememberedEmail');
                }
                location.reload();
                return;
            }
            // 예외 처리
            if (!response['result']) {
                console.error('서버 응답에 result 값이 없습니다.');
                Dialog.show({
                    title: '오류',
                    content: '서버로부터 유효하지 않은 응답이 반환되었습니다.',
                    buttons: [{text: '확인', onclick: Dialog.hide}]
                });
                return;
            }
            // result에 따른 메시지 매핑
            const failureCases = {
                failure: ['로그인', '이메일 혹은 비밀번호가 올바르지 않습니다. 다시 확인해 주세요.', ($dialog) => {
                    Dialog.hide($dialog);
                    $loginForm['email'].focus();
                    $loginForm['email'].select();
                }],
                failure_not_verified: ['로그인', `해당 계정의 이메일 인증이 완료되지 않았습니다. 이메일을 확인해 주세요.<br><br>혹시 이메일이 오지 않았다면 인증 링크가  포함된 이메일을 <a href="/user/resend-register-email-token?email=${$loginForm['email'].value}" target="_blank">다시전송</a> 할 수 있습니다.`, ($dialog) => Dialog.hide($dialog)],
                failure_suspended: ['로그인', '해당 계정은 이용이 정지된 상태입니다. 관리자에게 문의해 주세요.', ($dialog) => Dialog.hide($dialog)],
            }
            // 응답 처리
            const [title, content, onclick] = failureCases[response['result']] ||
            ['오류', '서버가 알 수 없는 응답을 반환하였습니다.', ($dialog) => Dialog.hide($dialog)];
            Dialog.show({
                title: title, content: content, buttons: [{
                    text: '확인', onclick: onclick
                }]
            });
        };
        xhr.open('POST', url.toString());
        xhr.setRequestHeader('Content-Type', 'application/json'); // 요청 헤더가 JSON임을 명시
        xhr.send(JSON.stringify({
            email: $loginForm['email'].value,
            password: $loginForm['password'].value
        }));
        Loading.show(0);
    };

    $menu.querySelector(':scope > .item > [rel="register"]').onclick = (e) => {
        e.preventDefault();
        $cover.onclick = () => { //커버(바깥쪽) 누르면 빠져나와야 하니까
            $cover.hide();
            $registerForm.hide();
        };
        $cover.show();
        $registerForm.reset(); // 필드 초기화
        $registerForm.show();
        $registerForm[`email`].focus();
    };
    $menu.querySelector(':scope > .item > [rel="recover"]').onclick = (e) => {
        e.preventDefault();
        $cover.onclick = () => { //커버(바깥쪽) 누르면 빠져나와야 하니까
            $cover.hide();
            $recoverForm.hide();
        };
        $cover.show();
        $recoverForm.reset(); // 필드 초기화
        $recoverForm.show();
    };
}


$registerForm[`cancel`].onclick = () => {
    $cover.hide();
    $registerForm.hide();
};

$registerForm.onsubmit = (e) => {
    e.preventDefault();
    const $emailLabel = $registerForm.findLabel('email');
    const $passwordLabel = $registerForm.findLabel('password');
    const $nicknameLabel = $registerForm.findLabel('nickname');
    const $contactLabel = $registerForm.findLabel('contact');
    $emailLabel.setValid($registerForm['email'].value.length >= 8 && $registerForm['email'].value.length <= 50);
    $passwordLabel.setValid($registerForm['password'].value.length >= 6 && $registerForm['password'].value.length <= 50, '올바른 비밀번호를 입력해 주세요');
    if ($passwordLabel.isValid()) {
        $passwordLabel.setValid($registerForm['password'].value === $registerForm['passwordCheck'].value, '비밀번호가 서로 일치하지 않습니다.');
    }
    $nicknameLabel.setValid($registerForm['nickname'].value.length >= 2 && $registerForm['nickname'].value.length <= 10);
    $contactLabel.setValid($registerForm['contact'].value.length >= 10 && $registerForm['contact'].value.length <= 12);
    if (!$emailLabel.isValid() || !$passwordLabel.isValid() || !$nicknameLabel.isValid() || !$contactLabel.isValid()) {
        return;
    }
    if (!$registerForm['agree'].checked) {
        Dialog.show({
            title: '회원가입',
            content: '서비스 이용약관 및 개인정보 처리방침에 동의하지 않으면 회원가입을 하실 수 없습니다.',
            buttons: [{
                text: '확인', onclick: ($dialog) => Dialog.hide($dialog)
            }]
        });
        return;
    }
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    formData.append('email', $registerForm['email'].value);
    formData.append('password', $registerForm['password'].value);
    formData.append('nickname', $registerForm['nickname'].value);
    formData.append('contact', $registerForm['contact'].value);
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        Loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            Dialog.show({
                title: '오류',
                content: '요청을 전송하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요',
                buttons: [{
                    text: '확인',
                    onclick: ($dialog) => Dialog.hide($dialog)
                }]
            });
            return;
        }
        const response = JSON.parse(xhr.responseText);
        const [title, content, onclick] = {
            failure: ['회원가입', `알 수 없는 이유로 회원가입에 실패아였습니다. 잠시 후 다시 시도해 주세요.`, ($dialog) => Dialog.hide($dialog)],
            failure_duplicate_email: ['회원가입', `입력하신 이메일(${$registerForm['email'].value})은 이미 사용 중입니다. 다른 이메일을 사용해 주세요.`, ($dialog) => Dialog.hide($dialog)],
            failure_duplicate_contact: ['회원가입', `입력하신 연락처(${$registerForm['contact'].value})은 이미 사용 중입니다. 다른 연락처를 사용해 주세요.`, ($dialog) => Dialog.hide($dialog)],
            failure_duplicate_nickname: ['회원가입', `입력하신 닉네임(${$registerForm['nickname'].value})은 이미 사용 중입니다. 다른 닉네임을 사용해 주세요.`, ($dialog) => Dialog.hide($dialog)],
            success: ['회원가입', `회원가입해 주셔서 감사합니다. 입력하신 이메일로 계정을 인증할 수 있는 링크를 전송하였습니다. 계정 인증 후 로그인할 수 있으며, 해당 링크는 24시간 동안만 유효하니 유의해 주세요.`, ($dialog) => {
                Dialog.hide($dialog);
                $registerForm.hide();
                $cover.hide();
            }],
        }[response['result']] || ['오류', '서버가 알 수 없는 응답을 반환하였습니다. 잠시 후 다시 시도해 주세요', ($dialog) => Dialog.hide($dialog)];
        Dialog.show({
            title: title,
            content: content,
            buttons: [{
                text: '확인',
                onclick: onclick
            }]
        });
    };
    xhr.open('POST', './user/');
    xhr.send(formData);
    Loading.show(0);
};

$recoverForm['cancel'].onclick = () => {
    $cover.hide();
    $recoverForm.hide();
};

$recoverForm.onsubmit = (e) => {
    e.preventDefault();
    if ($recoverForm['mode'].value === '') {
        Dialog.show({
            title: '계정 찾기',
            content: '"이메일 찾기" 혹은 "비밀번호 재설정" 중 하나를 선택해 주세요',
            buttons: [{
                text: '확인', onclick: ($dialog) => Dialog.hide($dialog)
            }]
        });
        return;
    }
    if ($recoverForm['mode'].value === 'email') {
        const $contactLabel = $recoverForm.findLabel('contact');
        $contactLabel.setValid($recoverForm['contact'].value.length >= 10 && $recoverForm['contact'].value.length <= 12);
        if (!$contactLabel.isValid()) {
            return;
        }
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            Loading.hide()
            if (xhr.status < 200 || xhr.status >= 300) {
                Dialog.show({
                    title: '오류',
                    content: '요청을 전송하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요',
                    buttons: [{
                        text: '확인',
                        onclick: ($dialog) => Dialog.hide($dialog)
                    }]
                })
                return;
            }
            const response = JSON.parse(xhr.responseText);
            const [title, content, onclick] = {
                failure: ['이메일 찾기', '입력하신 연락처와 일치하는 계정 정보를 찾을 수 없습니다.', ($dialog) => Dialog.hide($dialog)],
                success: ['이메일 찾기', `입력하신 연락처로 찾은 계정의 이메일은. <b>${response['email']}</b>입니다.<br><br>확인을 클릭하면 로그인 화면으로 돌아갑니다`, ($dialog) => {
                    Dialog.hide($dialog);
                    $cover.hide();
                    $recoverForm.hide();
                }],
            }[response['result']] || ['오류', '서버가 알 수 없는 응답을 반환하였습니다. 잠시 후 다시 시도해 주세요.', ($dialog) => Dialog.hide($dialog)];
            Dialog.show({
                title: title,
                content: content,
                buttons: [{
                    text: '확인', onclick: onclick
                }]
            });
        };
        xhr.open('GET', `/user/recover-email?contact=${$recoverForm['contact'].value}`);
        xhr.send();
        Loading.show(0);
    }

    if ($recoverForm['mode'].value === 'password') {
        const $emailLabel = $registerForm.findLabel('email')
        $emailLabel.setValid($recoverForm['email'].value.length >= 8 && $recoverForm['email'].value.length <= 50);
        if (!$emailLabel.isValid()) {
            return;
        }
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            Loading.hide()
            if (xhr.status < 200 || xhr.status >= 300) {
                Dialog.show({
                    title: '오류',
                    content: '요청을 전송하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요',
                    buttons: [{
                        text: '확인',
                        onclick: ($dialog) => Dialog.hide($dialog)
                    }]
                })
                return;
            }
            const response = JSON.parse(xhr.responseText);
            const [title, content, onclick] = {
                failure: ['비밀번호 재설정', '입력하신 이메일과 일치하는 계정 정보를 찾을 수 없습니다.', ($dialog) => Dialog.hide($dialog)],
                success: ['비밀번호 재설정', `입력하신 이메일로 비밀번호를 재설정할 수 있는 링크를 포함한 메일을 전송하였습니다.<br><br>확인을 클릭하면 로그인 화면으로 돌아갑니다`, ($dialog) => {
                    Dialog.hide($dialog);
                    $cover.hide();
                    $recoverForm.hide();
                }],
            }[response['result']] || ['오류', '서버가 알 수 없는 응답을 반환하였습니다. 잠시 후 다시 시도해 주세요.', ($dialog) => Dialog.hide($dialog)];
            Dialog.show({
                title: title,
                content: content,
                buttons: [{
                    text: '확인', onclick: onclick
                }]
            });
        };
        xhr.open('POST', `/user/recover-password?email=${$recoverForm['email'].value}`);
        xhr.send();
        Loading.show(0);
    }
};

