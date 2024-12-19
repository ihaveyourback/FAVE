document.addEventListener("DOMContentLoaded", () => {
    const sidebarButtons = document.querySelectorAll(".sidebar__button");
    const contentSections = document.querySelectorAll(".content__section");
    const deactivateForm = document.getElementById("deactivateForm");

    // 모든 섹션을 숨기는 함수
    const hideAllSections = () => {
        contentSections.forEach((section) => {
            section.style.display = "none";
        });
    };

    // 사이드바 버튼 클릭 이벤트 처리
    sidebarButtons.forEach((button, index) => {
        button.addEventListener("click", () => {
            hideAllSections(); // 먼저 모든 섹션 숨김 처리
            contentSections[index].style.display = "block"; // 클릭한 버튼에 맞는 섹션 표시
        });
    });

    // 회원탈퇴 폼 제출 처리
    deactivateForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const formData = new FormData(deactivateForm);

        fetch("/user/secession", {
            method: "POST",
            body: JSON.stringify({
                currentPassword: formData.get("currentPassword")
            }),
            headers: {
                "Content-Type": "application/json"
            }
        }).then(response => {
            if (response.ok) {
                alert("회원탈퇴가 완료되었습니다.");
                window.location.href = "/logout";
            } else {
                response.json().then(data => {
                    alert(data.message || "회원탈퇴 처리 중 오류가 발생하였습니다. 다시 시도해 주세요.");
                });
            }
        }).catch(error => {
            console.error("Error:", error);
            alert("회원탈퇴 처리 중 오류가 발생하였습니다. 다시 시도해 주세요.");
        });
    });
});
