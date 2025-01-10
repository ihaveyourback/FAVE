document.addEventListener("DOMContentLoaded", function () {
    const alertMessageDiv = document.getElementById("alertMessageDiv");
    const notificationBell = document.getElementById("notificationBell");
    const notificationDropdown = document.getElementById("notificationDropdown");
    const unreadCount = document.getElementById("unreadCount");
    const currentUserEmail = document.getElementById("currentUserEmail");

    let _unreadCount = 0;
    const createNotificationAnchor = (notification) => {
        const $a = document.createElement('a');
        const $delete = document.createElement('button');
        $delete.innerText = '삭제';
        $delete.type = 'button';
        $delete.onclick = (e) => {
            e.preventDefault();
            if (confirm('삭제하시겠습니까?')) {
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
                    alert('삭제 완료');
                    $a.remove();
                }).catch((error) => {
                    alert(error);
                });
            }
        };
        $a.innerText = notification['message'];
        $a.setAttribute('href', notification['url']);
        $a.append($delete);
        alertMessageDiv.append($a);

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

    stompClient.connect({}, function (frame) {
        console.log('WebSocket 연결 성공:', frame);
        stompClient.subscribe('/topic/alerts', function (message) {
            const notification = JSON.parse(message.body);
            if (notification['userEmail'] !== currentUserEmail.value) {
                return;  // 본인에게 온 알림만 처리
            }
            createNotificationAnchor(notification);  // 새 알림을 실시간으로 추가
        });

    }, function (error) {
        console.error('WebSocket 연결 실패:', error);
    });

    //모든 알림 다 가져오기
    fetch('/api/notifications/unread', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ userEmail: currentUserEmail.value })  // userEmail을 요청 본문에 포함
    })
        .then(response => response.json())
        .then(notifications => {
            const unreadNs = notifications.filter((x) => x['read'] === false);
            const readNs = notifications.filter((x) => x['read'] !== false);

            // 기존에 "알림이 없습니다" 메시지가 있는지 확인하고 제거
            const noNotificationsMessage = alertMessageDiv.querySelector('.no-notifications');
            if (noNotificationsMessage) {
                noNotificationsMessage.remove();
            }

            // 알림이 없을 경우 "알림이 없습니다" 메시지 표시
            if (unreadNs.length === 0 && readNs.length === 0) {
                const noNotificationsDiv = document.createElement('div');
                noNotificationsDiv.classList.add('no-notifications');
                noNotificationsDiv.innerText = '알림이 없습니다';
                alertMessageDiv.append(noNotificationsDiv);
            }

            // 읽지 않은 알림
            if (unreadNs.length > 0) {
                const unreadCap = document.createElement('div');
                unreadCap.innerText = '읽지 않은 알림';
                alertMessageDiv.append(unreadCap);
                unreadNs.forEach(notification => createNotificationAnchor(notification));
            }

            // 읽은 알림
            if (readNs.length > 0) {
                const readCap = document.createElement('div');
                readCap.innerText = '읽은 알림';
                alertMessageDiv.append(readCap);
                readNs.forEach(notification => createNotificationAnchor(notification));
            }

        })
        .catch(error => console.error('알림을 가져오는 데 실패:', error));

    // 알림 클릭 시 읽음 처리
    alertMessageDiv.addEventListener('click', function (event) {
        if (event.target.tagName === 'P') { // 알림이 클릭되었을 때
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
            body: JSON.stringify({ notificationId: notificationId })
        })
            .then(response => response.json())
            .then(data => {
                console.log('알림 읽음 처리 완료:', data);
                alertElement.style.color = 'gray';  // 읽은 알림 스타일 변경
                alertElement.style.textDecoration = 'line-through';  // 읽은 알림에 선 긋기
            })
            .catch(error => console.error('알림 읽음 처리 실패:', error));
    }
});
