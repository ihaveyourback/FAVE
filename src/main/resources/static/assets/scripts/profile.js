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
            .then(response => {
                if (!response.ok) {
                    // 응답이 정상적이지 않으면, 텍스트 응답으로 서버 오류 확인
                    return response.text().then(text => {
                        console.error("서버 오류 응답:", text);  // 서버에서 반환된 텍스트 오류 페이지 확인
                        throw new Error("서버 오류 응답");
                    });
                }
                return response.json(); // 정상 응답은 JSON으로 처리
            })
            .then(data => {
                if (data && data.message) {
                    alert(data.message);  // 서버 응답 메시지 알림
                    if (data.message.includes("성공적으로 업데이트")) {
                        window.location.href = "/logout";  // 업데이트 성공 시 로그아웃 처리
                    }
                } else {
                    alert("예상치 못한 응답이 왔습니다.");
                }
            })
            .catch(error => {
                // 여기서 발생하는 오류를 좀 더 구체적으로 처리
                console.error('Error:', error);  // 실제 오류를 확인하기 위해 로깅
                if (error.message === "서버 오류 응답") {
                    alert("서버에서 오류가 발생하였습니다. 관리자에게 문의해주세요.");
                } else {
                    alert("회원 정보 업데이트 중 오류가 발생하였습니다.");
                }
            });

    });

        });
    };

    // 초기 페이지네이션 이벤트 리스너 추가
    addPaginationEventListeners();

    // 마지막으로 선택된 섹션 로드
    loadLastSelectedSection();
});