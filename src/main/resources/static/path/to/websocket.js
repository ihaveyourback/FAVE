document.addEventListener("DOMContentLoaded", function () {
    const alertMessageDiv = document.getElementById("alertMessageDiv");
    const alertDiv = document.getElementById("alertDiv");
    const currentUserNickname = document.getElementById("currentUserNickname").value;
    const notificationBell = document.getElementById("notificationBell");
    const notificationDropdown = document.getElementById("notificationDropdown");
    const unreadCount = document.getElementById("unreadCount");
    const currentUserEmail = document.getElementById("currentUserEmail");


    let unreadNotificationCount = 0;

    const createNotificationAnchor = (notification) => {
        const $a = document.createElement('a');
        const $delete = document.createElement('button');
        $delete.innerText = '삭제';
        $delete.type = 'button';
        $delete.onclick = (e) => {
            e.preventDefault();
            if (confirm('ㄹㅇ?')) {
                const url = new URL(location.href);
                url.pathname = `/api/notifications/`;
                url.searchParams.set('index', notification.index);
                fetch(url.toString(), {
                    method: 'DELETE',
                    signal: AbortSignal.timeout(10000)
                }).then((response) => {
                    if (!response.ok) {
                        throw new Error(response.status.toString());
                    }
                    alert('굿');
                    $a.remove();
                }).catch((error) => {
                    alert(error);
                });
            }
        }
        $a.innerText = notification['message'];
        $a.setAttribute('href', notification['url']);
        $a.append($delete);
        alertMessageDiv.append($a);
        alertMessageDiv.querySelector('p').style.display = 'none';
        if (notification['read'] === false) {
            _unreadCount++;
            unreadCount.innerText = _unreadCount.toLocaleString();
        }
        unreadCount.style.display = "inline";
        return $a;
    };

    // 종 클릭 시 드롭다운 토글
    notificationBell.addEventListener("click", function () {
        const isDropdownVisible = notificationDropdown.style.display === "block";
        notificationDropdown.style.display = isDropdownVisible ? "none" : "block";

        // 드롭다운 열면 읽음 처리
        if (!isDropdownVisible) {
            unreadNotificationCount = 0;
            unreadCount.style.display = "none";
        }

        const url = new URL(location.href);
        url.pathname = '/api/notifications/all';
        fetch(url.toString(), {
            method: 'PATCH',
            signal: AbortSignal.timeout(10000)
        }).then((response) => {
            if (!response.ok) {
                throw new Error(response.status.toString());
            }
        }).catch((error) => {
            alert(error);
        })
    });

    const socket = new SockJS('/push');
    const stompClient = Stomp.over(socket);
    let _unreadCount = 0;

    stompClient.connect({}, function (frame) {
        console.log('WebSocket 연결 성공:', frame);
        stompClient.subscribe('/topic/alerts', function (message) {
            const notification = JSON.parse(message.body);
            if (notification['userEmail'] !== currentUserEmail.value) {
                console.log('유저 불일치')
                return;
            }
            console.log('유저 일치')
            createNotificationAnchor(notification);
        });

        // stompClient.subscribe('/topic/alerts', function (message) {
        //     const data = JSON.parse(message.body);
        //     console.log("받은 데이터:", data);
        //
        //     let shouldDisplayAlert = false; // 알림 표시 여부
        //     let alertMessage = ""; // 알림 메시지
        //     const titleMaxLength = 8; // 제목 최대 길이
        //
        //     // 데이터 값이 null이거나 undefined일 경우 처리
        //     const truncatedTitle = data.postTitle && data.postTitle.length > titleMaxLength
        //         ? `${data.postTitle.substring(0, titleMaxLength)}...`
        //         : data.postTitle || "제목 없음";
        //
        //     const commentContent = data.commentContent && data.commentContent.length > titleMaxLength
        //         ? `${data.commentContent.substring(0, titleMaxLength)}...`
        //         : data.commentContent || "내용 없음";
        //
        //     const postLink = `/article/read?index=${data.articleIndex}`;
        //
        //     if (data.notificationType === '좋아요' || data.notificationType === '댓글') {
        //         if (data.postAuthor === currentUserNickname) {
        //             shouldDisplayAlert = true;
        //             alertMessage = (data.notificationType === '좋아요')
        //                 ? `${data.nickname}님이 <a href="${postLink}">내 ${truncatedTitle}</a> 게시글에 좋아요를 눌렀습니다`
        //                 : `${data.nickname}님이 <a href="${postLink}">내 ${truncatedTitle}</a> 게시글에 댓글을 달았습니다`;
        //         }
        //     } else if (data.notificationType === '대댓글') {
        //         if (data.commentAuthor === currentUserNickname) {
        //             shouldDisplayAlert = true;
        //             alertMessage = `${data.nickname}님이 <a href="${postLink}">내 ${commentContent}</a> 댓글을 달았습니다`;
        //         }
        //     }
        //
        //     if (shouldDisplayAlert) {
        //         // 새로운 알림 메시지 추가
        //         const newAlertMessage = document.createElement("p");
        //         newAlertMessage.innerHTML = alertMessage; // HTML을 삽입
        //         newAlertMessage.style.padding = "5px 0";
        //         newAlertMessage.dataset.notificationId = data.notificationId; // 알림 ID 추가
        //         alertMessageDiv.appendChild(newAlertMessage);
        //
        //         // 읽지 않은 알림 카운트 증가
        //         unreadNotificationCount++;
        //         unreadCount.textContent = unreadNotificationCount;
        //         unreadCount.style.display = "inline";
        //     } else {
        //         console.log("알림 표시 조건에 맞지 않음. 데이터:", data);
        //     }
        // }, function (error) {
        //     console.error('알림 수신 중 오류 발생:', error);
        // });
    }, function (error) {
        console.error('WebSocket 연결 실패:', error);
    });
    console.log("------------"+ currentUserEmail)

    //모든 알림 다 가져오기
    fetch('/api/notifications/unread', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },

        body: JSON.stringify({userEmail: currentUserEmail.value})  // userNickname을 요청 본문에 포함

    })
        .then(response => response.json())
        .then(notifications => {
            const unreadNs = notifications.filter((x) => x['read'] === false);
            const readNs = notifications.filter((x) => x['read'] !== false);
            const $unreadCap = Object.assign(document.createElement('div'), {
                innerText: '읽지 않은 알림'
            });
            alertMessageDiv.append($unreadCap);
            for (const notification of unreadNs) {
                createNotificationAnchor(notification);
            }
            const $readCap = Object.assign(document.createElement('div'), {
                innerText: '읽은 알림'
            });
            alertMessageDiv.append($readCap);
            for (const notification of readNs) {
                createNotificationAnchor(notification);
            }
        })
        .catch(error => console.error('알림을 가져오는 데 실패:', error));

    // fetch('/api/notifications/unread', {
    //     method: 'POST',
    //     headers: {
    //         'Content-Type': 'application/json',
    //     },
    //     body: JSON.stringify({ userNickname: currentUserNickname })  // userNickname을 요청 본문에 포함
    // })
    //     .then(response => response.json())
    //     .then(notifications => {
    //         if (notifications.length > 0) {
    //             let shouldDisplayAlert = false; // 알림 표시 여부
    //             let alertMessage = ""; // 알림 메시지
    //             const titleMaxLength = 8; // 제목 최대 길이
    //
    //             // 알림을 화면에 추가하는 코드
    //             notifications.forEach(notification => {
    //                 const truncatedTitle = notification.postTitle && notification.postTitle.length > titleMaxLength
    //                     ? `${notification.postTitle.substring(0, titleMaxLength)}...`
    //                     : notification.postTitle || "제목 없음";
    //
    //                 const commentContent = notification.commentContent && notification.commentContent.length > titleMaxLength
    //                     ? `${notification.commentContent.substring(0, titleMaxLength)}...`
    //                     : notification.commentContent || "내용 없음";
    //
    //                 const postLink = `/article/read?index=${notification.articleIndex}`;
    //
    //                 if (notification.type === '좋아요' || notification.type === '댓글') {
    //                         shouldDisplayAlert = true;
    //                         alertMessage = (notification.type === '좋아요')
    //                             ? `${notification.nickname}님이 <a href="${postLink}">내 ${truncatedTitle}</a> 게시글에 좋아요를 눌렀습니다`
    //                             : `${notification.nickname}님이 <a href="${postLink}">내 ${truncatedTitle}</a> 게시글에 댓글을 달았습니다`;
    //                 } else if (notification.type === '대댓글') {
    //                         shouldDisplayAlert = true;
    //                         alertMessage = `${notification.nickname}님이 <a href="${postLink}">내 ${commentContent}</a> 댓글을 달았습니다`;
    //                 }
    //
    //                 if (shouldDisplayAlert) {
    //                     // 새로운 알림 메시지 추가
    //                     const newAlertMessage = document.createElement("p");
    //                     newAlertMessage.innerHTML = alertMessage; // HTML을 삽입
    //                     newAlertMessage.style.padding = "5px 0";
    //                     newAlertMessage.dataset.notificationId = notification.notificationId; // 알림 ID 추가
    //                     alertMessageDiv.appendChild(newAlertMessage);
    //
    //                     // 읽지 않은 알림 카운트 증가
    //                     unreadNotificationCount++;
    //                     unreadCount.textContent = unreadNotificationCount;
    //                     unreadCount.style.display = "inline";
    //                 } else {
    //                     console.log("알림 표시 조건에 맞지 않음. 데이터:", notification);
    //                 }
    //             });
    //         }
    //     })
    //     .catch(error => console.error('알림을 가져오는 데 실패:', error));

    // 알림 클릭 시 읽음 처리
    alertMessageDiv.addEventListener('click', function (event) {
        if (event.target.tagName === 'P') { // 알림이 클릭되었을 때
            // 해당 알림을 읽음 처리하고, 서버로 상태 업데이트 요청
            markNotificationAsRead(event.target);
        }
    });

    function markNotificationAsRead(alertElement) {
        const notificationId = alertElement.dataset.notificationId; // 알림 ID 가져오기

        // 서버로 알림 읽음 처리 요청
        fetch('/push', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({notificationId: notificationId})
        })
            .then(response => response.json())
            .then(data => {
                console.log('알림 읽음 처리 완료:', data);
                alertElement.style.color = 'gray'; // 읽은 알림 스타일 변경
                alertElement.style.textDecoration = 'line-through'; // 읽은 알림에 선 긋기
            })
            .catch(error => console.error('알림 읽음 처리 실패:', error));
    }
});
