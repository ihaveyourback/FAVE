const $modalContainer = document.querySelector('.Modal-container');
const $modal = $modalContainer.querySelector('.Modal');

const $modalEmail = document.getElementById('modalEmail');
const $modalNickName = document.getElementById('modalNickname');
const $modalContact = document.getElementById('modalContact');
const $modalCreateAt = document.getElementById('modalCreateAt');
const $modalIsAdmin = document.getElementById('modalIsAdmin');
const $modalVerified = document.getElementById('modalVerified');
const $modalWarning = document.getElementById('modalWarning');

const $modalIndex = document.getElementById('modalIndex');
const $modalTitle = document.getElementById('modalTitle');
const $modalContent = document.getElementById('modalContent');
const $modalUserEmail = document.getElementById('modalUserEmail');
const $modalUserNickName = document.getElementById('modalUserNickName');
const $modalCreateAt2 = document.getElementById('modalCreateAt2')
const $modalView = document.getElementById('modalView');

const $modalIndex2 = document.getElementById('modalIndex2')
const $modalUserEmail2 = document.getElementById('modalUserEmail2')
const $modalReportedUserEmail = document.getElementById('modalReportedUserEmail')
const $modalReportedPostId = document.getElementById('modalReportedPostId')
const $modalReportedCommentId = document.getElementById('modalReportedCommentId')
const $modalStatus = document.getElementById('modalStatus')
const $modalCurrentStatus = document.getElementById('modalCurrentStatus')
const $modalReason = document.getElementById('modalReason')
const $modalReasonDetail = document.getElementById('modalReasonDetail')
const $modalReportedAt = document.getElementById('modalReportedAt')

const $users = document.querySelectorAll('.grid-container > .gird:first-of-type .main');
const $boards = document.querySelectorAll('.grid-container > .gird:nth-of-type(2) .main');
const $reports = document.querySelectorAll('.grid-container > .gird:nth-of-type(4) .main');

function updateModalPosition(grid) {
    const gridRect = grid.getBoundingClientRect(); // 해당 grid 위치
    const containerRect = $modalContainer.getBoundingClientRect(); // 부모 컨테이너 위치

    const top = gridRect.top - containerRect.top;
    const left = gridRect.right - containerRect.left;

    $modal.style.top = `${top}px`;
    $modal.style.left = `${left}px`;
}

function updateModalPositionReports(grid) {
    const gridRect = grid.getBoundingClientRect();
    const containerRect = $modalContainer.getBoundingClientRect();

    const top = gridRect.top - containerRect.top;
    const left = gridRect.left - 300;

    $modal.style.top = `${top}px`;
    $modal.style.left = `${left}px`;
}

function resetModal() {
    // 회원 데이터 초기화
    $modalEmail.textContent = '';
    $modalNickName.textContent = '';
    $modalContact.textContent = '';
    $modalCreateAt.textContent = '';
    $modalIsAdmin.textContent = '';
    $modalVerified.textContent = '';
    $modalWarning.textContent = '';

    // 게시글 데이터 초기화
    $modalIndex.textContent = '';
    $modalTitle.textContent = '';
    $modalContent.textContent = '';
    $modalUserEmail.textContent = '';
    $modalUserNickName.textContent = '';
    $modalCreateAt2.textContent = '';
    $modalView.textContent = '';

    // 신고 데이터 초기화
    $modalIndex2.textContent = '';
    $modalUserEmail2.textContent = '';
    $modalReportedUserEmail.textContent = '';
    $modalReportedPostId.textContent = '';
    $modalReportedCommentId.textContent = '';
    $modalStatus.textContent = '';
    $modalCurrentStatus.textContent = '';
    $modalReason.textContent = '';
    $modalReasonDetail.textContent = '';
    $modalReportedAt.textContent = '';
}

$users.forEach(($user) => {
    $user.addEventListener('mouseover', () => {
        resetModal();
        const $email = $user.dataset.email;
        const $nickname = $user.dataset.nickname;
        const $contact = $user.dataset.contact;
        const $createAt = $user.dataset.createat;
        const $isAdmin = $user.dataset.isadmin;
        const $verified = $user.dataset.verified;
        const $warning = $user.dataset.warning;

        $modalEmail.textContent = '이메일 : ' + $email;
        $modalNickName.textContent = '닉네임 : ' + $nickname;
        $modalContact.textContent = '전화번호 : ' + $contact;
        $modalCreateAt.textContent = '생성일 : ' + $createAt;
        $modalIsAdmin.textContent = '회원등급 : ' + $isAdmin;
        $modalVerified.textContent = '인증상태 : ' + $verified;
        $modalWarning.textContent = '누적경고 : ' + $warning;

        updateModalPosition(document.querySelector('.grid-container > .gird:first-of-type'));
        $modal.style.display = 'block';
    });

    $user.addEventListener('mouseout', () => {
        $modal.style.display = 'none';
    });
});

$boards.forEach(($board) => {
    $board.addEventListener('mouseover', () => {
        resetModal();
        const $index = $board.dataset.index;
        const $title = $board.dataset.title;
        const $content = $board.dataset.content;
        const $userEmail = $board.dataset.useremail;
        const $userNickName = $board.dataset.usernickname;
        const $createAt2 = $board.dataset.createat
        const $view = $board.dataset.view;

        $modalIndex.textContent = '번호 : ' + $index;
        $modalTitle.textContent = '제목 : ' + $title;
        $modalContent.textContent = '내용 : ' + $content;
        $modalUserEmail.textContent = '작성자 이메일 : ' + $userEmail;
        $modalUserNickName.textContent = '작성자 닉네임 : ' + $userNickName;
        $modalCreateAt2.textContent = "작성일 : " + $createAt2;
        $modalView.textContent = '조회수 : ' + $view;

        updateModalPosition(document.querySelector('.grid-container > .gird:nth-of-type(2)'));
        $modal.style.display = 'block';
    });

    $board.addEventListener('mouseout', () => {
        $modal.style.display = 'none';
    });
});

$reports.forEach(($report) => {
    $report.addEventListener('mouseover', () => {
        resetModal();
        const $Index2 = $report.dataset.index;
        const $UserEmail2 = $report.dataset.useremail;
        const $ReportedUserEmail = $report.dataset.reporteduseremail;
        const $ReportedPostId = $report.dataset.reportedpostid;
        const $ReportedCommentId = $report.dataset.reportedcommentid;
        const $Status = $report.dataset.status;
        const $CurrentStatus = $report.dataset.currentstatus;
        const $Reason = $report.dataset.reason;
        const $ReasonDetail = $report.dataset.reasondetail;
        const $ReportedAt = $report.dataset.reportedat;

        $modalIndex2.textContent = '신고 번호 : ' + $Index2;
        $modalUserEmail2.textContent = '신고자 : ' + $UserEmail2;
        $modalReportedUserEmail.textContent = '피신고자 : ' + $ReportedUserEmail;
        $modalReportedPostId.textContent = '게시글 번호 : ' + $ReportedPostId;
        $modalReportedCommentId.textContent = '댓글 번호 : ' + $ReportedCommentId;
        $modalStatus.textContent = '신고유형 : ' + $Status;
        $modalCurrentStatus.textContent = '상태 : ' + $CurrentStatus;
        $modalReason.textContent = '신고이유 : ' + $Reason;
        $modalReasonDetail.textContent = '신고내용 : ' + $ReasonDetail;
        $modalReportedAt.textContent = '신고일 : ' + $ReportedAt;


        updateModalPositionReports(document.querySelector('.grid-container > .gird:nth-of-type(4)'));
        $modal.style.display = 'block';
    })

    $report.addEventListener('mouseout', () => {
        $modal.style.display = 'none';
    })
})