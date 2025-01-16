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



    deactivateForm.addEventListener("submit", (e) => {
        e.preventDefault();

        const formData = new FormData(deactivateForm);
        const currentPassword = formData.get("currentPassword");

        // 소셜 로그인 여부 확인: oauth2Provider가 null이면 일반 로그인, 아니면 소셜 로그인
        const isSocialLogin = document.body.dataset.oauth2Provider != null;

        // 소셜 로그인인 경우 currentPassword를 제외
        const data = isSocialLogin ? {} : { currentPassword: currentPassword };

        // 비밀번호가 필요하지 않은 경우에는 currentPassword를 비워서 전송
        if (isSocialLogin) {
            data.currentPassword = "";  // 소셜 로그인 시 비밀번호 제외
        }

        console.log("서버로 전송할 데이터:", data);

        // 서버로 폼 데이터 전송
        fetch(deactivateForm.action, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        })
            .then(response => {
                console.log("응답 상태:", response.status);
                return response.json().then(data => {
                    console.log("서버 응답 데이터:", data);
                    if (response.status === 200) {
                        // 회원 탈퇴가 성공적으로 완료되었을 때
                        alert("회원 탈퇴가 완료되었습니다.");
                        window.location.href = "/logout";  // 메인 페이지로 리디렉션
                    } else if (response.status === 401) {
                        // 비밀번호가 일치하지 않을 때
                        alert("현재 비밀번호가 일치하지 않습니다.");
                    } else {
                        // 기타 실패 시 메시지 표시
                        alert("회원 탈퇴에 실패하였습니다. 다시 시도해 주세요.");
                    }
                });
            })
            .catch(error => {
                console.error("오류:", error);
                alert("회원 탈퇴 중 오류가 발생했습니다.");
            });
    });


// 폼 요소를 선택
    const socialDeactivateForm = document.getElementById("socialDeactivateForm");

    if (socialDeactivateForm) {
        socialDeactivateForm.addEventListener("submit", (e) => {
            e.preventDefault();

            // 소셜 로그인 사용자는 비밀번호 없이 회원탈퇴를 요청할 수 있으므로 빈 객체를 전달
            fetch("/user/secession", {
                method: "POST",
                body: JSON.stringify({}), // 비밀번호 확인 없이 빈 객체 전달
                headers: {
                    "Content-Type": "application/json"
                }
            }).then(response => {
                if (response.ok) {
                    alert("회원탈퇴가 완료되었습니다.");
                    window.location.href = "/logout"; // 탈퇴 후 로그아웃 페이지로 이동
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
    }


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
    const addPaginationEventListeners = () => {
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
                        document.querySelector("#reports").innerHTML = doc.querySelector("#reports").innerHTML;
                        document.querySelector("#favorites").innerHTML = doc.querySelector("#favorites").innerHTML;
                        addPaginationEventListeners(); // 새로운 페이지네이션 링크에 이벤트 리스너 추가
                        history.pushState(null, '', url); // URL 업데이트
                    })
                    .catch(error => {
                        console.error('Error:', error);
                    });
            });
        });
    };
    // 초기 페이지네이션 이벤트 리스너 추가
    addPaginationEventListeners();

    // 마지막으로 선택된 섹션 로드
    loadLastSelectedSection();
});