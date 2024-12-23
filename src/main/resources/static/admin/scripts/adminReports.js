document.addEventListener("DOMContentLoaded", () => {
    const warningButtons = document.querySelectorAll('.warning-button');
    const deletedButtons = document.querySelectorAll('.deleted-button')

    warningButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const getReportUserEmail = button.closest('td').querySelector('input[name="getReportUserEmail"]').value;
            const warning = button.closest('td').querySelector('input[name="warning"]').value;
            const index = button.closest('td').querySelector('input[name="index"]').value;
            const xhr = new XMLHttpRequest();
            const formData = new FormData();
            formData.append('userEmail', getReportUserEmail);
            formData.append('warning', warning);
            xhr.onreadystatechange = () => {
                if (xhr.readyState !== XMLHttpRequest.DONE) {
                    return;
                }
                if (xhr.status < 200 || xhr.status >= 300) {
                    return;
                }
                const response = JSON.parse(xhr.responseText);
                if (response['result'] === true) {
                    alert(getReportUserEmail + '님에게 경고 조치를 취하였습니다.');
                    const xhr = new XMLHttpRequest();
                    const formData = new FormData();
                    formData.append('index', index);
                    xhr.onreadystatechange = () => {
                        if (xhr.readyState !== XMLHttpRequest.DONE) {
                            return;
                        }
                        if (xhr.status < 200 || xhr.status >= 300) {
                            return;
                        }
                        const response = JSON.parse(xhr.responseText)
                        if (response['result'] === true) {
                            alert('신고 처리 완료 했습니다.')
                            location.reload();
                        }
                    };
                    xhr.open('POST', '/admin/reports/index');
                    xhr.send(formData);
                } else {
                    alert('알 수 없는 이유로 경고를 추가하지 못하였습니다.');
                }
            };
            xhr.open('PATCH', '/admin/warning/');
            xhr.send(formData);
        });
    });

    deletedButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const getReportUserEmail = button.closest('td').querySelector('input[name="getReportUserEmail"]').value;
            const index = button.closest('td').querySelector('input[name="index"]').value;
            const xhr = new XMLHttpRequest();
            const formData = new FormData();
            formData.append('userEmail', getReportUserEmail);
            xhr.onreadystatechange = () => {
                if (xhr.readyState !== XMLHttpRequest.DONE) {
                    return;
                }
                if (xhr.status < 200 || xhr.status >= 300) {
                    return;
                }
                const response = JSON.parse(xhr.responseText)
                if (response['result'] === true) {
                    alert(getReportUserEmail + '님의 계정을 이용조치 처리하였습니다.')
                    const xhr = new XMLHttpRequest();
                    const formData = new FormData();
                    formData.append('index', index);
                    xhr.onreadystatechange = () => {
                        if (xhr.readyState !== XMLHttpRequest.DONE) {
                            return;
                        }
                        if (xhr.status < 200 || xhr.status >= 300) {
                            return;
                        }
                        const response = JSON.parse(xhr.responseText)
                        if (response['result'] === true) {
                            alert('신고 처리 완료 했습니다.')
                            location.reload();
                        }
                    };
                    xhr.open('POST', '/admin/reports/index');
                    xhr.send(formData);
                } else {
                    alert('알 수 없는 이유로 이용조치 처리를 실패하였습니다.')
                }
            };
            xhr.open('DELETE', '/admin/delete/');
            xhr.send(formData);
        })
    })
})