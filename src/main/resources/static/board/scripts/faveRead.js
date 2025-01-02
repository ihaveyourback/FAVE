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

    // likeButton이 null인지 확인
    if (likeButton) {
        // 페이지 로딩 시 찜 상태 확인
        checkLikeStatus();

        // 찜 상태 확인 함수
        async function checkLikeStatus() {
            const festivalId = likeButton.getAttribute('data-festival-id');
            const userEmail = likeButton.getAttribute('data-user-email');

            try {
                if (response.ok) {
                    const result = await response.json();  // 서버에서 받은 JSON 데이터
                    const isLiked = result.isLiked;

                    // 버튼 텍스트와 클래스 변경
                    if (isLiked) {
                        likeButton.classList.add('liked');
                        likeButton.textContent = '찜 취소';
                    } else {
                        likeButton.classList.remove('liked');
                        likeButton.textContent = '찜하기';
                    }

                    // 이미지 변경
                    const likeImage = document.querySelector('#likeImage'); // 이미지의 ID를 가정
                    if (isLiked) {
                        likeImage.src = "https://img.icons8.com/fluency-systems-filled/24/like.png";
                        likeImage.alt = "like-filled";
                    } else {
                        likeImage.src = "https://img.icons8.com/material-outlined/24/like--v1.png";
                        likeImage.alt = "like-outline";
                    }
                } else {
                    alert('찜 상태 확인 중 오류가 발생했습니다.');
                }
            } catch (error) {
                console.error('찜 상태 확인 중 오류:', error);
            }
        }

            // 버튼 클릭 시 찜 상태 변경 처리
        likeButton.addEventListener('click', async function (e) {
            e.preventDefault();

            const festivalId = likeButton.getAttribute('data-festival-id');
            const userEmail = likeButton.getAttribute('data-user-email');
            const isLiked = likeButton.classList.contains('liked'); // 'liked' 클래스 추가 여부로 상태 확인

            try {
                // 서버에 찜 상태 처리 요청 (POST/DELETE)
                const response = await fetch('/fave/read/', {
                    method: isLiked ? 'DELETE' : 'POST', // 상태에 따라 POST 또는 DELETE 요청
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        festivalId: festivalId,
                        userEmail: userEmail
                    }),
                });

                if (response.ok) {
                    const result = await response.text();
                    alert(result);

                    // 버튼 상태 업데이트
                    if (isLiked) {
                        likeButton.classList.remove('liked');
                        likeButton.textContent = '찜하기';
                    } else {
                        likeButton.classList.add('liked');
                        likeButton.textContent = '찜 취소';
                    }
                } else {
                    alert('서버 처리 중 오류가 발생했습니다.');
                }

            } catch (error) {
                console.error('찜 상태 처리 중 오류:', error);
            }
        });
    } else {
        console.error('likeButton 요소를 찾을 수 없습니다.');
    }
});
