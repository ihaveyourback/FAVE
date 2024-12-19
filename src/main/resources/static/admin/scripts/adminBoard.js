document.addEventListener("DOMContentLoaded", () => {
    const warningButtons = document.querySelectorAll('.warning-button');
    const deletedButtons = document.querySelectorAll('.deleted-button');

    deletedButtons.forEach(button => {
        button.addEventListener('click', (e) => {
                e.preventDefault();
                const userEmail = button.closest('td').querySelector('input[name="userEmail"]').value;
                const index = button.closest('td').querySelector('input[name="index"]').value;
                const formData = new FormData();
                formData.append('index', index);
                const xhr = new XMLHttpRequest();
                xhr.onreadystatechange = () => {
                    if (xhr.readyState !== XMLHttpRequest.DONE) {
                        return;
                    }
                    if (xhr.status < 200 || xhr.status >= 300) {
                        return;
                    }
                    const response = JSON.parse(xhr.responseText)
                    if (response['result'] === true) {
                        alert(userEmail + '님의 게시글을 삭제 조치 하였습니다.')
                        location.reload();
                    }
                    if (response['result'] === false) {
                        alert('알 수 없는 이유로 게시글 삭제 조치를 하지 못하였습니다.')
                    }
                };
                xhr.open('DELETE', '/delete/');
                xhr.send(formData);
            }
        )
    })


    warningButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const userEmail = button.closest('td').querySelector('input[name="userEmail"]').value;
            const warning = button.closest('td').querySelector('input[name="warning"]').value;
            const formData = new FormData();
            formData.append('userEmail', userEmail);
            formData.append('warning', warning)
            const xhr = new XMLHttpRequest();
            xhr.onreadystatechange = () => {
                if (xhr.readyState !== XMLHttpRequest.DONE) {
                    return;
                }
                if (xhr.status < 200 || xhr.status >= 300) {
                    return;
                }
                const response = JSON.parse(xhr.responseText);
                if (response['result'] === true) {
                    alert(userEmail + '님께서 작성한 게시글에 경고 조치를 취하였습니다.')
                    location.reload();
                }
                if (response['result'] === false) {
                    alert('알 수 없는 이유로 경고를 추가하지 못하였습니다.')
                }
            };
            xhr.open('PATCH', '/warning/');
            xhr.send(formData);
        })
    })
})