document.addEventListener("DOMContentLoaded", function () {

    var params = new URLSearchParams(window.location.search);
    var index = params.get('index');

    if (!index) {
        console.error("인덱스 값이 없습니다.");
        return;
    }

    var mapContainer = document.getElementById('map'), // 지도를 표시할 div
        mapOption = {
            center: new kakao.maps.LatLng(33.450701, 126.570667), // 지도의 중심좌표
            level: 3 // 지도의 확대 레벨
        };

// 지도를 생성합니다
    var map = new kakao.maps.Map(mapContainer, mapOption);

// 주소-좌표 변환 객체를 생성합니다
    var geocoder = new kakao.maps.services.Geocoder();

    // DB에서 주소 가져오기 (XHR 방식)
    var xhr = new XMLHttpRequest();
    xhr.open('GET', `/fave/get-address?index=${index}`, true); // 서버의 API URL로 수정하세요
    xhr.onreadystatechange = function () {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            if (xhr.status === 200) {
                var data = JSON.parse(xhr.responseText);
                var address = data.address; // 서버에서 반환된 주소

                // 주소로 좌표를 검색합니다
                geocoder.addressSearch(address, function (result, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        var coords = new kakao.maps.LatLng(result[0].y, result[0].x);

                        var marker = new kakao.maps.Marker({
                            map: map,
                            position: coords
                        });

                        var infowindow = new kakao.maps.InfoWindow({
                            content: '<div style="width:150px;text-align:center;padding:6px 0;">위치</div>'
                        });
                        infowindow.open(map, marker);

                        map.setCenter(coords);
                    } else {
                        console.error("주소 검색 실패:", status);
                    }
                });
            } else {
                console.error("DB 요청 실패:", xhr.status, xhr.statusText);
            }
        }
    };
    xhr.send();
});
document.addEventListener('DOMContentLoaded', () => {
    const likeButton = document.getElementById('likeButton');
    const likeImage = likeButton?.querySelector('img'); // 버튼 내부 이미지 가져오기

    if (likeButton && likeImage) {
        const festivalId = likeButton.getAttribute('data-festival-id');
        const userEmail = likeButton.getAttribute('data-user-email');

        // 초기 상태 확인 함수
        async function checkLikeStatus() {
            try {
                const response = await fetch(`/fave/read/status?index=${festivalId}&userEmail=${userEmail}`);
                if (response.ok) {
                    const result = await response.json();
                    const isLiked = result.isLiked;

                    // 초기 상태에 따라 이미지 업데이트
                    if (isLiked) {
                        likeImage.src = "https://img.icons8.com/fluency-systems-filled/48/like.png";
                        likeImage.alt = "like-filled";
                    } else {
                        likeImage.src = "https://img.icons8.com/material-outlined/48/like--v1.png";
                        likeImage.alt = "like-outline";
                    }
                } else {
                    console.error('초기 찜 상태 확인 중 오류가 발생했습니다.');
                }
            } catch (error) {
                console.error('초기 찜 상태 확인 중 오류:', error);
            }
        }

        // 버튼 클릭 시 상태 변경 처리
        likeButton.addEventListener('click', async (e) => {
            e.preventDefault(); // 기본 동작 방지

            const isLiked = likeImage.src.includes('fluency-systems-filled'); // 현재 이미지로 찜 상태 확인
            const method = isLiked ? 'DELETE' : 'POST'; // 요청 타입 결정
            const endpoint = '/fave/read/';

            try {
                const response = await fetch(endpoint, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        festivalId: festivalId,
                        userEmail: userEmail,
                    }),
                });

                if (response.ok) {
                    const result = await response.json(); // JSON 형식으로 응답 받기
                    const message = result.message; // 서버에서 전송한 메시지
                    alert(message); // 메시지 알림 표시

                    // 상태 변경 성공 시 이미지 업데이트
                    if (isLiked) {
                        likeImage.src = "https://img.icons8.com/material-outlined/48/like--v1.png";
                        likeImage.alt = "like-outline";
                    } else {
                        likeImage.src = "https://img.icons8.com/fluency-systems-filled/48/like.png";
                        likeImage.alt = "like-filled";
                    }
                } else {
                    alert('찜 상태 변경 중 오류가 발생했습니다.');
                }
            } catch (error) {
                console.error('찜 상태 변경 중 오류:', error);
            }
        });

        // 초기 상태 확인 호출
        checkLikeStatus();
    } else {
        console.error('likeButton 또는 이미지 요소를 찾을 수 없습니다.');
    }
});

