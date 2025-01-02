document.addEventListener('DOMContentLoaded', () => {
    const $main = document.getElementById('main');
    const $cover = document.getElementById('cover');
    let $deleteDialog = document.getElementById('deleteDialog');
    const $buttonModify = $main.querySelector('button[name="modify"]');
    const $buttonDelete = $main.querySelector('button[name="delete"]');
    const $commentForm = document.getElementById('commentForm');

    if ($deleteDialog) { // Ensure $deleteDialog exists
        // 수정 버튼 클릭 시 처리
        $buttonModify.onclick = () => {
            const url = new URL(location.href);
            location.href = `./modify?index=${url.searchParams.get('index')}`;
        };

        // 삭제 버튼 클릭 시 처리git --version
        $buttonDelete.onclick = () => {
            $cover.classList.add('--visible');
            $deleteDialog['mode'].value = 'delete';
            $deleteDialog.classList.add('--visible');
        };

        $deleteDialog['cancel'].onclick = () => {
            $cover.classList.remove('--visible');
            $deleteDialog.classList.remove('--visible');
        };

        // 딜리트 다이얼로그 제출 시 처리
        $deleteDialog.onsubmit = (e) => {
            e.preventDefault();
            const index = $deleteDialog['index'].value;

            const xhr = new XMLHttpRequest();
            const formData = new FormData();
            formData.append('index', index);

            xhr.onreadystatechange = () => {
                if (xhr.readyState === XMLHttpRequest.DONE) {
                    if (xhr.status >= 200 && xhr.status < 300) {
                        const response = JSON.parse(xhr.responseText);
                        switch (response['result']) {
                            case 'failure':
                                alert('게시글을 삭제하지 못하였습니다. 이미 삭제된 게시글일 수도 있습니다. 잠시 후 다시 시도해 주세요.');
                                break;
                            case 'success':
                                alert('게시글이 성공적으로 삭제되었습니다.');
                                $cover.classList.remove('--visible');
                                $deleteDialog.classList.remove('--visible');
                                location.href = $main.querySelector('.button.back').href; // 목록 앵커 태그의 링크로 이동
                                break;
                            default:
                                alert('서버가 알 수 없는 응답을 반환하였습니다. 삭제 결과를 반드시 확인해 주세요.');
                                break;
                        }
                    } else {
                        alert('게시글을 삭제하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
                    }
                    // visible 클래스 제거
                    $cover.classList.remove('--visible');
                    $deleteDialog.classList.remove('--visible');
                }
            };

            xhr.open('DELETE', './read'); // URL을 수정해 주세요
            xhr.send(formData);
        };
    }
});
document.addEventListener('DOMContentLoaded', () => {
    const $commentList = document.querySelector('.comments .list');
    const $commentCount = document.getElementById('commentCount');
    const articleIndex = document.getElementById('articleIndex').value;

    const postComment = ($form) => {
        if ($form['comment'].value === '') {
            alert('내용을 입력해 주세요.');
            return;
        }
        const url = new URL(location.href);
        const xhr = new XMLHttpRequest();
        const formData = new FormData();

        formData.append('postId', url.searchParams.get('index'));
        formData.append('comment', $form['comment'].value);

        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            if (xhr.status < 200 || xhr.status >= 300) {
                alert('댓글을 작성하지 못하였습니다.');
                return;
            }
            loadComments();
            $form['comment'].value = '';
            alert('댓글이 작성되었습니다.')

        };
        xhr.open('POST', '/comment/write');
        xhr.send(formData);
    };


    const $commentForm = document.getElementById('commentForm');
    $commentForm.onsubmit = (e) => {
        e.preventDefault();
        postComment($commentForm);
    };

    const loadComments = () => {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) return;

            if (xhr.status >= 200 && xhr.status < 300) {
                const comments = JSON.parse(xhr.responseText);
                $commentCount.textContent = comments.length;

                // 기존 댓글 초기화
                $commentList.innerHTML = '';

                comments.forEach(comment => {
                    const $commentItem = document.createElement('li');
                    $commentItem.className = 'item';

                    const $topDiv = document.createElement('div');
                    $topDiv.className = 'top';

                    const $nicknameSpan = document.createElement('span');
                    $nicknameSpan.className = 'nickname';
                    $nicknameSpan.textContent = comment.userNickname;

                    const $datetimeSpan = document.createElement('span');
                    $datetimeSpan.className = 'datetime';
                    $datetimeSpan.textContent = comment.createdAt.replace('T',' ');

                    const $label = document.createElement('label');
                    $label.className = 'link';

                    const $checkbox = document.createElement('input');
                    $checkbox.className = 'checkbox';
                    $checkbox.name = 'toggle';
                    $checkbox.type = 'checkbox';

                    $label.appendChild($checkbox);

                    $topDiv.appendChild($nicknameSpan);
                    $topDiv.appendChild($datetimeSpan);
                    $topDiv.appendChild($label);

                    $commentItem.appendChild($topDiv);

                    const escapeHtml = (text) => {
                        const div = document.createElement('div');
                        div.textContent = text; // HTML 이스케이프 처리
                        return div.innerHTML;
                    };
                    // ^^ 줄바꿈 보안 고려사항 코드

                    const $contentDiv = document.createElement('div');
                    $contentDiv.className = 'content';
                    $contentDiv.innerHTML = escapeHtml(comment.comment).replace(/\n/g, '<br>');
                    // ^^ 줄바꿈 코드
                    $commentItem.appendChild($contentDiv);
                    // $contentDiv.textContent = comment.comment;

                    $commentItem.appendChild($contentDiv);

                    const $actionContainer = document.createElement('div');
                    $actionContainer.className = 'action-container';

                    const $replyButton = document.createElement('label');
                    $replyButton.className = 'action';

                    const $replyInput = document.createElement('input');
                    $replyInput.name = 'comment' + comment.index;
                    $replyInput.type = 'radio';
                    $replyInput.value = 'reply';

                    const $replySpan = document.createElement('span');
                    $replySpan.className = 'text';
                    $replySpan.textContent = '답글 쓰기';

                    $replyButton.appendChild($replyInput);
                    $replyButton.appendChild($replySpan);

                    const $modifyButton = document.createElement('label');
                    $modifyButton.className = 'action';

                    const $modifyInput = document.createElement('input');
                    $modifyInput.name = 'comment' + comment.index;
                    $modifyInput.type = 'radio';
                    $modifyInput.value = 'modify';

                    const $modifySpan = document.createElement('span');
                    $modifySpan.className = 'text';
                    $modifySpan.textContent = '수정';

                    $modifyButton.appendChild($modifyInput);
                    $modifyButton.appendChild($modifySpan);

                    const $reportButton = document.createElement('button');
                    $reportButton.className = 'action report';
                    $reportButton.name = 'report';
                    $reportButton.type = 'button';
                    $reportButton.textContent = '신고';

// 클릭 시 동작
                    $reportButton.addEventListener('click', () => {
                        if (comment) {
                            // 댓글 신고 URL로 이동, comment.index와 article.index를 사용
                            location.href = `/report/page?index=${comment.postId}&commentIndex=${comment.index}`;
                        }
                        // } else {
                        //     // 게시글 신고 URL로 이동, article.index만 사용
                        //     location.href = `/report/article?index=${article.index}`;
                        // }
                    });


// 댓글 신고 버튼 클릭 이벤트 추가
                    $reportButton.addEventListener('click', () => {
                        reportComment(comment.index);
                    });

                    $actionContainer.appendChild($reportButton);

                    const $deleteButton = document.createElement('button');
                    $deleteButton.className = 'action';
                    $deleteButton.name = 'delete';
                    $deleteButton.type = 'button';
                    $deleteButton.textContent = '삭제';

                    // 댓글 삭제 버튼 클릭 이벤트 추가
                    $deleteButton.addEventListener('click', () => {
                        deleteComment(comment.index);
                    });


                    $actionContainer.appendChild($replyButton);
                    $actionContainer.appendChild($modifyButton);
                    $actionContainer.appendChild($deleteButton);
                    $actionContainer.appendChild($reportButton)

                    $commentItem.appendChild($actionContainer);

                    $commentList.appendChild($commentItem);
                });
            } else {
                alert('댓글을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
            }
        };

        xhr.open('GET', `/comment/?postId=${articleIndex}`);
        xhr.send();
    };

    const deleteComment = (commentIndex) => {
        if (!confirm('정말로 삭제하시겠습니까?')) return;

        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    alert('댓글이 삭제되었습니다.');
                    loadComments(); // 댓글 목록 갱신
                } else {
                    alert('댓글을 삭제하지 못했습니다. 잠시 후 다시 시도해 주세요.');
                }
            }
        };

        xhr.open('DELETE', `/comment/delete?commentId=${commentIndex}`);
        xhr.send();
    };

    // 페이지 로드 시 댓글을 불러옴
    loadComments();
});