document.addEventListener("DOMContentLoaded", () => {
    const sidebarButtons = document.querySelectorAll(".sidebar__button");
    const contentSections = document.querySelectorAll(".content__section");
    const deactivateForm = document.getElementById("deactivateForm");
    const updateForm = document.getElementById("updateForm");

    // 현재 표시된 섹션을 제외하고 모든 섹션을 숨기는 함수
    const hideAllSections = (currentIndex) => {
        contentSections.forEach((section, index) => {
            if (index !== currentIndex) {
                section.style.display = "none";
            }
        });
    };

    // 사이드바 버튼 클릭 이벤트 처리
    sidebarButtons.forEach((button, index) => {
        button.addEventListener("click", () => {
            hideAllSections(index); // 현재 인덱스를 제외하고 모든 섹션 숨김 처리
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

    // 내 정보 수정 폼 제출 처리
    updateForm.addEventListener("submit", (e) => {
        e.preventDefault();
        const formData = {
            nickname: document.getElementById('nickname').value,
            currentPassword: document.getElementById('currentPassword').value,
            newPassword: document.getElementById('newPassword').value
        };

        fetch('/user/update-profile', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
            .then(response => response.json())
            .then(data => {
                alert(data.message);
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });

    // 페이징 버튼 클릭 이벤트 처리
    const paginationLinks = document.querySelectorAll(".pagination a");
    paginationLinks.forEach(link => {
        link.addEventListener("click", (event) => {
            event.preventDefault(); // 새로고침 방지
            const url = link.getAttribute("href");
            fetch(url)
                .then(response => response.text())
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    document.querySelector("#posts").innerHTML = doc.querySelector("#posts").innerHTML;
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        });
    });
});