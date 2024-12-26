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