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
            alert('댓글이 작성되었습니다.');
        };
        xhr.open('POST', '/comment/write');
        xhr.send(formData);
    };

    const postReply = ($form, parentCommentId) => {
        if ($form['content'].value === '') {
            alert('내용을 입력해 주세요.');
            return;
        }
        const xhr = new XMLHttpRequest();
        const formData = new FormData();

        formData.append('parentCommentId', parentCommentId);
        formData.append('content', $form['content'].value);
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            if (xhr.status < 200 || xhr.status >= 300) {
                alert('답글을 작성하지 못하였습니다.');
                return;
            }
            loadComments();
            $form['content'].value = '';
            alert('답글이 작성되었습니다.');
        };
        xhr.open('POST', '/comment/reply');
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

                $commentList.innerHTML = ''; // Clear existing comments

                const commentMap = {};

                comments.forEach(comment => {
                    const $commentItem = document.createElement('li');
                    $commentItem.className = 'item';
                    if (comment.commentId) {
                        $commentItem.classList.add('reply'); // Add reply class for reply comments
                    }

                    const $topDiv = document.createElement('div');
                    $topDiv.className = 'top';

                    const $nicknameSpan = document.createElement('span');
                    $nicknameSpan.className = 'nickname';
                    $nicknameSpan.textContent = comment.userNickname;

                    const $datetimeSpan = document.createElement('span');
                    $datetimeSpan.className = 'datetime';
                    $datetimeSpan.textContent = comment.createdAt.replace('T', ' ');

                    $topDiv.appendChild($nicknameSpan);
                    $topDiv.appendChild($datetimeSpan);

                    $commentItem.appendChild($topDiv);

                    const escapeHtml = (text) => {
                        const div = document.createElement('div');
                        div.textContent = text;
                        return div.innerHTML;
                    };

                    // 댓글 내용
                    const $contentDiv = document.createElement('div');
                    $contentDiv.className = 'content';
                    $contentDiv.innerHTML = escapeHtml(comment.comment).replace(/\n/g, '<br>');
                    $commentItem.appendChild($contentDiv);

                    // 댓글 수정 폼
                    const $modifyForm = document.createElement('form');
                    $modifyForm.className = 'form modify';
                    $modifyForm.style.display = 'none';
                    $modifyForm.innerHTML = `
                         <form class="form modify">
                        <label class="label spring">
                            <span class="text">내용</span>
                            <textarea required class="field" maxlength="100" minlength="1" name="content">${comment.comment}</textarea>
                        </label>
                        <div class="button-container" style="display: flex" style="flex-direction: row">
                            <button class="--obj-button -button_color" type="submit">수정</button>
                            <button type="button" class="--obj-button -light cancel-button">취소</button>
                        </div>
                        </form>
                    `;
                    $commentItem.appendChild($modifyForm);

                    // 대댓글 작성 폼
                    const $replyForm = document.createElement('form');
                    $replyForm.className = 'form reply';
                    $replyForm.style.display = 'none';
                    $replyForm.innerHTML = `
                        <label class="label spring">
                            <span class="text">내용</span>
                            <textarea required class="field" maxlength="100" minlength="1" name="content"></textarea>
                        </label>
                        <form class="form reply">
                        <div class="button-container" style="display: flex" style="flex-direction: row">
                            <button class="--obj-button -button_color" type="submit">답글쓰기</button>
                            <button type="button" class="--obj-button -light cancel-button">취소</button>
                        </div>
                        </form>
                    `;
                    $commentItem.appendChild($replyForm);

                    // 액션 버튼들
                    const $actionContainer = document.createElement('div');
                    $actionContainer.className = 'action-container';

                    const $modifyButton = document.createElement('button');
                    $modifyButton.className = 'action';
                    $modifyButton.textContent = '수정';
                    $modifyButton.addEventListener('click', () => {
                        // Hide any visible forms
                        document.querySelectorAll('.form').forEach(form => form.style.display = 'none');
                        document.querySelectorAll('.content').forEach(content => content.style.display = 'block');
                        document.querySelectorAll('.action-container').forEach(container => container.style.display = 'flex');

                        $modifyForm.style.display = 'block';
                        $contentDiv.style.display = 'none'; // 기존 댓글 숨기기
                        $actionContainer.style.display = 'none'; // 액션 버튼 숨기기
                        $replyForm.style.display = 'none'; // Hide reply form if visible
                        $modifyForm.querySelector('textarea').focus();
                    });

                    const $deleteButton = document.createElement('button');
                    $deleteButton.className = 'action';
                    $deleteButton.textContent = '삭제';
                    $deleteButton.addEventListener('click', () => {
                        deleteComment(comment.index);
                    });

                    const $replyButton = document.createElement('button');
                    $replyButton.className = 'action';
                    $replyButton.textContent = '답글 쓰기';
                    $replyButton.addEventListener('click', () => {
                        // Hide any visible forms
                        document.querySelectorAll('.form').forEach(form => form.style.display = 'none');
                        document.querySelectorAll('.content').forEach(content => content.style.display = 'block');
                        document.querySelectorAll('.action-container').forEach(container => container.style.display = 'flex');

                        $replyForm.style.display = 'block';
                        $actionContainer.style.display = 'none'; // 액션 버튼 숨기기
                        $replyForm.querySelector('textarea').focus();
                    });

                    $actionContainer.appendChild($modifyButton);
                    $actionContainer.appendChild($deleteButton);
                    $actionContainer.appendChild($replyButton);
                    $commentItem.appendChild($actionContainer);

                    // 수정 폼 취소 버튼 이벤트
                    $modifyForm.querySelector('.cancel-button').addEventListener('click', () => {
                        $modifyForm.style.display = 'none';
                        $contentDiv.style.display = 'block'; // 기존 댓글 다시 보이기
                        $actionContainer.style.display = 'block'; // 액션 버튼 다시 보이기
                        $actionContainer.style.display = 'flex';
                        $actionContainer.style.flexDirection = 'row'
                    });

                    // 대댓글 작성 폼 취소 버튼 이벤트
                    $replyForm.querySelector('.cancel-button').addEventListener('click', () => {
                        $replyForm.style.display = 'none';
                        $actionContainer.style.display = 'block'; // 액션 버튼 다시 보이기
                        $actionContainer.style.display = 'flex';
                        $actionContainer.style.flexDirection = 'row'
                    });

                    // 수정 폼 제출 이벤트
                    $modifyForm.onsubmit = (e) => {
                        e.preventDefault();
                        const newContent = $modifyForm['content'].value;
                        if (newContent && newContent !== comment.comment) {
                            modifyComment(comment.index, newContent);
                        }
                    };

                    // 대댓글 작성 폼 제출 이벤트
                    $replyForm.onsubmit = (e) => {
                        e.preventDefault();
                        postReply($replyForm, comment.index);
                    };

                    if (comment.commentId) {
                        // Append reply to its parent comment
                        const parentComment = commentMap[comment.commentId];
                        if (parentComment) {
                            parentComment.appendChild($commentItem);
                        }
                    } else {
                        // Append main comment to the list
                        $commentList.appendChild($commentItem);
                        commentMap[comment.index] = $commentItem;
                    }
                });
            } else {
                alert('댓글을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
            }
        };

        xhr.open('GET', `/comment/?postId=${articleIndex}`);
        xhr.send();
    };

    const modifyComment = (commentIndex, newContent) => {
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('index', commentIndex);
        formData.append('content', newContent);

        xhr.onreadystatechange = () => {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    alert('댓글이 수정되었습니다.');
                    loadComments();
                } else {
                    alert('댓글을 수정하지 못했습니다. 잠시 후 다시 시도해 주세요.');
                }
            }
        };

        xhr.open('PATCH', '/comment/');
        xhr.send(formData);
    };

    const deleteComment = (commentIndex) => {
        if (!confirm('정말로 삭제하시겠습니까?')) return;

        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    alert('댓글이 삭제되었습니다.');
                    loadComments();
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

        // 삭제 버튼 클릭 시 처리
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

        // 삭제 버튼 클릭 시 처리
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