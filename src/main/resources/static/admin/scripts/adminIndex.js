const $modalContainer = document.querySelector('.Modal-container');
const $modal = $modalContainer.querySelector('.Modal');
const $modalEmail = document.getElementById('modalEmail');
const $modalNickName = document.getElementById('modalNickname');
const $modalContact = document.getElementById('modalContact');
const $modalCreateAt = document.getElementById('modalCreateAt');
const $modalIsAdmin = document.getElementById('modalIsAdmin');
const $modalVerified = document.getElementById('modalVerified');
const $modalWarning = document.getElementById('modalWarning');

const $grid = document.querySelector('.grid-container > .gird');
const $users = document.querySelectorAll('.tbody > .user');

function updateModalPosition() {
    const gridRect = $grid.getBoundingClientRect(); // 첫 번째 grid 위치
    const containerRect = $modalContainer.getBoundingClientRect(); // 부모 컨테이너 위치

    // 부모 기준으로 위치 보정
    const top = gridRect.top - containerRect.top;
    const left = gridRect.right - containerRect.left; // 오른쪽 + 20px

    // 모달 위치 설정
    $modal.style.top = `${top}px`;
    $modal.style.left = `${left}px`;
}

$users.forEach(($user) => {
    $user.addEventListener('mouseover', () => {
        const $email = $user.dataset.email;
        const $nickname = $user.dataset.nickname;
        const $contact = $user.dataset.contact;
        const $createAt = $user.dataset.createat;
        const $isAdmin = $user.dataset.isadmin;
        const $verified = $user.dataset.verified;
        const $warning = $user.dataset.warning
        $modalEmail.textContent = '이메일 : ' + $email;
        $modalNickName.textContent = '닉네임 : ' + $nickname;
        $modalContact.textContent = '전화번호 : ' + $contact;
        $modalCreateAt.textContent = '생성일 : ' + $createAt;
        $modalIsAdmin.textContent = '회원등급 : ' + $isAdmin;
        $modalVerified.textContent = '인증상태 : ' + $verified;
        $modalWarning.textContent = '누적경고 : ' + $warning;
        updateModalPosition();
        $modal.style.display = 'block';
    });

    $user.addEventListener('mouseout', () => {
        $modal.style.display = 'none';
    })
})

