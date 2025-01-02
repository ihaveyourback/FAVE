document.addEventListener('DOMContentLoaded', () => {
    const likeContainers = document.querySelectorAll('.like-container');

    // 페이지 로딩 시 서버에서 초기 상태를 가져옵니다.
    likeContainers.forEach(container => {
        const boardId = container.dataset.boardId;
        fetchInitialLikeStatus(boardId, container);
    });

    // 좋아요 및 취소 버튼에 이벤트 추가
    likeContainers.forEach(container => {
        const likeButton = container.querySelector('.like');
        const unlikeButton = container.querySelector('.unlike');
        const boardId = container.dataset.boardId;

        likeButton.addEventListener('click', () => {
            handleLike(boardId, container, true);
        });

        unlikeButton.addEventListener('click', () => {
            handleLike(boardId, container, false);
        });
    });

    // 초기 상태를 가져오는 함수
    function fetchInitialLikeStatus(boardId, container) {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) return;

            if (xhr.status >= 200 && xhr.status < 300) {
                const response = JSON.parse(xhr.responseText);
                const isLiked = response.isLiked;
                const likeCount = response.likeCount;

                updateLikeUI(container, isLiked, likeCount);
            } else {
                console.error(`초기 상태를 가져오는 데 실패했습니다. (게시물 ID: ${boardId})`);
            }
        };

        xhr.open('GET', `/board/status/${boardId}`, true);
        xhr.send();
    }

    // 좋아요/좋아요 취소 요청 처리 함수
    function handleLike(boardId, container, isLike) {
        const url = isLike ? `/board/like/${boardId}` : `/board/unlike/${boardId}`;
        const xhr = new XMLHttpRequest();

        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) return;

            if (xhr.status >= 200 && xhr.status < 300) {
                const response = xhr.responseText.trim();

                if (response === '"SUCCESS"') {
                    alert(isLike ? '좋아요가 반영되었습니다.' : '좋아요가 취소되었습니다.');
                    location.reload();
                    updateLikeUI(container, isLike);
                } else if (response === '"FAILURE"') {
                    alert('좋아요 반영에 실패했습니다. 다시 시도해주세요.');
                } else if (response === '"NOT_LOGGED_IN"') {
                    alert('로그인 후 좋아요를 눌러주세요.');
                } else {
                    alert('알 수 없는 오류가 발생했습니다.');
                }
            } else {
                alert('서버와의 통신에 실패했습니다.');
            }
        };

        xhr.open('POST', url, true);
        xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
        xhr.send(`isLike=${isLike}`);
    }

    // UI 업데이트 함수
    function updateLikeUI(container, isLiked, likeCount = null) {
        const likeButton = container.querySelector('.like');
        const unlikeButton = container.querySelector('.unlike');
        const likeCountSpan = container.querySelector('.like-count');

        // 좋아요 카운트 업데이트
        if (likeCount !== null) {
            likeCountSpan.innerText = likeCount;
        }

        // 버튼 상태 설정
        likeButton.style.display = isLiked ? 'none' : 'inline-block';
        unlikeButton.style.display = isLiked ? 'inline-block' : 'none';
    }
});
