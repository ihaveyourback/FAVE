document.addEventListener("DOMContentLoaded", () => {
    const sidebarButtons = document.querySelectorAll(".sidebar__button");
    const contentSections = document.querySelectorAll(".content__section");
    const deactivateForm = document.getElementById("deactivateForm");
    const updateForm = document.getElementById("updateForm");

    // 모든 섹션을 숨기는 함수, 현재 인덱스는 제외
    const hideAllSections = (currentIndex) => {
        contentSections.forEach((section, index) => {
            if (index !== currentIndex) {
                section.style.display = "none";
            }
        });
    };

    // 마지막으로 선택된 섹션을 로드하는 함수
    const loadLastSelectedSection = () => {
        const lastSelectedSection = localStorage.getItem("lastSelectedSection");
        if (lastSelectedSection) {
            const index = parseInt(lastSelectedSection, 10);
            hideAllSections(index);
            contentSections[index].style.display = "block";
        } else {
            hideAllSections(0);
            contentSections[0].style.display = "block";
        }
    };

    // 사이드바 버튼 클릭 이벤트 리스너 추가
    sidebarButtons.forEach((button, index) => {
        button.addEventListener("click", () => {
            hideAllSections(index);
            contentSections[index].style.display = "block";
            localStorage.setItem("lastSelectedSection", index);
        });
    });

    // 회원탈퇴 폼 제출 이벤트 리스너 추가
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

    // 사용자 정보 업데이트 폼 제출 이벤트 리스너 추가
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
                if (response.ok) {
                    window.location.href = "/logout";
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
    });

    // 페이지네이션 링크 클릭 이벤트 리스너 추가
    const paginationLinks = document.querySelectorAll(".pagination a");
    paginationLinks.forEach(link => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const url = link.getAttribute("href");
            fetch(url)
                .then(response => response.text())
                .then(html => {
                    const parser = new DOMParser();
                    const doc = parser.parseFromString(html, 'text/html');
                    document.querySelector("#posts").innerHTML = doc.querySelector("#posts").innerHTML;
                    document.querySelector(".pagination").innerHTML = doc.querySelector(".pagination").innerHTML;
                    history.pushState(null, '', url); // URL 업데이트
                })
                .catch(error => {
                    console.error('Error:', error);
                });
        });
    });


    // 마지막으로 선택된 섹션 로드
    loadLastSelectedSection();
});