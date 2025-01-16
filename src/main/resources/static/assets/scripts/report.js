document.addEventListener("DOMContentLoaded", function () {
    const reasonRadios = document.querySelectorAll('input[name="reason"]');
    const otherReasonTextarea = document.getElementById("reasonTextarea");
    const form = document.getElementById("form");

    // 초기 설정
    otherReasonTextarea.disabled = true;

    // 라디오 버튼 변경 시 처리
    reasonRadios.forEach((radio) => {
        radio.addEventListener("change", function () {
            if (this.value === "기타") {
                otherReasonTextarea.disabled = false; // 기타 선택 시 활성화
                otherReasonTextarea.focus();
            } else {
                otherReasonTextarea.disabled = true; // 기타 외 선택 시 비활성화
                otherReasonTextarea.value = ""; // 내용 초기화
            }
        });
    });

    function get_reported_post_index(){

        const searchParams = new URL(location.href).searchParams;
        if(searchParams.has('commentIndex')){
            return { "reported_comment_id": searchParams.get('commentIndex'), "status": "댓글" }
        }
        else if(searchParams.has('index')){
            return { "reported_post_id": searchParams.get('index'), "status": "게시글" }
        }
    }

    // 폼 제출 시 처리
    form.onsubmit = function (e) {
        e.preventDefault();

        const selectedReason = document.querySelector('input[name="reason"]:checked');



        if (selectedReason.value === "기타" && otherReasonTextarea.value.trim() === "") {
            alert("기타 사유를 입력해주세요.");
            return;
        }

        const requestBodyData = get_reported_post_index();
        if(requestBodyData == null){
            alert('url 이상해ㅛ');
            return;
        }
        // AJAX 요청 처리
        const xhr = new XMLHttpRequest();
        const formData = new FormData(form);
        for(const key in requestBodyData){
            formData.append(key, requestBodyData[key]);
        }

        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE)
                return;

            const response = JSON.parse(xhr.responseText);
            if (xhr.status < 200 || xhr.status >= 300) {
                alert(response.message || "오류 발생.");
                return;
            }

            if (response.result === "success") {
                location.href = "./result";
            } else {
                alert(response.message || "오류 발생.");
            }
        };

        xhr.open("POST", location.href);
        xhr.send(formData);
    };
});