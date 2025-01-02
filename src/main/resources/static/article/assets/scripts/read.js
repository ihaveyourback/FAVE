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
    const $commentList = document.querySelector('.comments .list');
    const $commentCount = document.getElementById('commentCount');
    const $commentForm = document.getElementById('commentForm');
    const articleIndex = document.getElementById('articleIndex').value;
    const loggedInUserEmail = document.body.dataset.loggedInEmail;

    console.log(loggedInUserEmail)
    // 댓글 불러오기
    const loadComments = () => {
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) return;

            if (xhr.status >= 200 && xhr.status < 300) {
                const comments = JSON.parse(xhr.responseText);
                $commentCount.textContent = comments.length;

                $commentList.innerHTML = ''; // 기존 댓글 초기화
                const commentMap = {}; // 댓글 ID로 댓글을 찾을 수 있도록 맵 생성

                comments.forEach(comment => {
                    const $commentItem = createCommentItem(comment);
                    commentMap[comment.index] = $commentItem;

                    if (comment.commentId) {
                        // 대댓글의 부모 댓글을 찾아 그 아래에 추가
                        const parentComment = commentMap[comment.commentId];
                        if (parentComment) {
                            const $replyContainer = parentComment.querySelector('.reply-container');
                            $replyContainer.appendChild($commentItem);
                        }
                    } else {
                        // 최상위 댓글
                        $commentList.appendChild($commentItem);
                    }
                });
            } else {
                alert('댓글을 불러오지 못했습니다.');
            }
        };

        xhr.open('GET', `/comment/?postId=${articleIndex}`);
        xhr.send();
    };

    // 댓글 생성 함수
    const createCommentItem = (comment) => {
        const $commentItem = document.createElement('li');
        $commentItem.className = 'item';
        if (comment.commentId) {
            $commentItem.classList.add('reply'); // 대댓글 클래스 추가
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

        // 댓글 내용
        const $contentDiv = document.createElement('div');
        $contentDiv.className = 'content';
        $contentDiv.innerHTML = comment.comment.replace(/\n/g, '<br>');
        $commentItem.appendChild($contentDiv);

        // 버튼 컨테이너
        const $actionContainer = document.createElement('div');
        $actionContainer.className = 'action-container';

        const $replyButton = document.createElement('button');
        $replyButton.className = 'action';
        $replyButton.textContent = '답글 쓰기';
        $replyButton.addEventListener('click', () => {
            closeOpenForms();
            $replyForm.style.display = 'block';

        });

        const $modifyButton = document.createElement('button');
        $modifyButton.className = 'action';
        $modifyButton.textContent = '수정';
        $modifyButton.addEventListener('click', () => {
            // 작성자가 아니면 알림을 띄운다
            if (comment.userEmail !== loggedInUserEmail) {
                alert('내가 작성한 댓글이 아닙니다.');
                return;
            }


            // 수정 폼 생성 후 댓글 아래에 삽입
            const $modifyForm = createModifyForm(comment.index, comment.comment, $contentDiv, $actionContainer, $commentItem);
        });

        const $deleteButton = document.createElement('button');
        $deleteButton.className = 'action';
        $deleteButton.textContent = '삭제';
        $deleteButton.addEventListener('click', () => {
            // 작성자가 아니면 알림을 띄운다
            if (comment.userEmail !== loggedInUserEmail) {
                alert('내가 작성한 댓글이 아닙니다.');
                return;
            }
            deleteComment(comment.index);
        });

        $actionContainer.appendChild($replyButton);
        $actionContainer.appendChild($modifyButton);
        $actionContainer.appendChild($deleteButton);
        $commentItem.appendChild($actionContainer);




        // 답글 작성 폼
        const $replyForm = createReplyForm(comment.index);
        $commentItem.appendChild($replyForm);

        // 대댓글 컨테이너
        const $replyContainer = document.createElement('ul');
        $replyContainer.className = 'reply-container';
        $commentItem.appendChild($replyContainer);

        return $commentItem;
    };

    // 수정 폼 생성
    const createModifyForm = (commentIndex, currentContent, $contentDiv, $actionContainer, $commentItem) => {
        closeOpenForms();

        const existingModifyForm = $commentItem.querySelector('.form.modify');
        if (existingModifyForm) {
            existingModifyForm.remove();
        }

        const $modifyForm = document.createElement('form');
        $modifyForm.className = 'form modify';
        $modifyForm.innerHTML = `
            <label class="label spring">
                <span class="text">내용</span>
                <textarea required class="field" maxlength="100" minlength="1">${currentContent}</textarea>
            </label>
            <div class="button-container">
                <button class="--obj-button -button_color" type="submit">수정</button>
                <button type="button" class="--obj-button -light cancel-button">취소</button>
            </div>
        `;

        // 수정 폼 제출 이벤트
        $modifyForm.onsubmit = (e) => {
            e.preventDefault();
            const newContent = $modifyForm.querySelector('textarea').value;
            if (newContent && newContent !== currentContent) {
                modifyComment(commentIndex, newContent);
            } else {
                alert('내용이 변경되지 않았습니다.');
            }
        };

        // 취소 버튼 이벤트
        $modifyForm.querySelector('.cancel-button').addEventListener('click', () => {
            $modifyForm.remove(); // 수정 폼 제거
            $contentDiv.style.display = 'block'; // 기존 내용 다시 표시
            $actionContainer.style.display = 'flex'; // 버튼 다시 표시
        });

        // 부모 댓글 바로 아래에 수정 폼 추가
        const $parentComment = $commentItem.closest('.item');
        const $replyContainer = $parentComment.querySelector('.reply-container');
        $replyContainer.insertBefore($modifyForm, $replyContainer.firstChild); // 부모 댓글 아래로 수정 폼 추가

        return $modifyForm;
    };

    // 대댓글 작성 폼 생성
    const createReplyForm = (parentCommentId) => {
        const $replyForm = document.createElement('form');
        $replyForm.className = 'form reply';
        $replyForm.style.display = 'none';
        $replyForm.innerHTML = `
            <label class="label spring">
                <span class="text">내용</span>
                <textarea required class="field" maxlength="100" minlength="1" name="content"></textarea>
            </label>
            <div class="button-container">
                <button class="--obj-button -button_color" type="submit">답글 쓰기</button>
                <button type="button" class="--obj-button -light cancel-button">취소</button>
            </div>
        `;

        $replyForm.onsubmit = (e) => {
            e.preventDefault();
            const content = $replyForm['content'].value;
            if (content) {
                postReply($replyForm, parentCommentId);
            }
        };

        $replyForm.querySelector('.cancel-button').addEventListener('click', () => {
            $replyForm.style.display = 'none';
        });

        return $replyForm;
    };

    // 이미 열려 있는 폼을 닫기
    const closeOpenForms = () => {
        const openModifyForm = document.querySelector('.form.modify');
        if (openModifyForm) {
            openModifyForm.remove();
        }

        const openReplyForm = document.querySelector('.form.reply');
        if (openReplyForm) {
            openReplyForm.style.display = 'none';
        }
    };

    // 댓글 작성
    const postComment = ($form) => {
        const content = $form['comment'].value;
        if (!content) {
            alert('내용을 입력해 주세요.');
            return;
        }

        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('postId', articleIndex);
        formData.append('comment', content);

        xhr.onreadystatechange = () => {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    alert('댓글이 작성되었습니다.');
                    loadComments(); // 댓글 목록 새로 고침

                    // 댓글 작성 후 입력란 비우기
                    $form['comment'].value = ''; // 텍스트 영역 초기화
                } else {
                    alert('댓글 작성 실패.');
                }
            }
        };

        xhr.open('POST', '/comment/write');
        xhr.send(formData);
    };

    // 대댓글 작성
    const postReply = ($form, parentCommentId) => {
        const content = $form['content'].value;
        if (!content) {
            alert('내용을 입력해 주세요.');
            return;
        }

        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('parentCommentId', parentCommentId);
        formData.append('content', content);

        xhr.onreadystatechange = () => {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    alert('답글이 작성되었습니다.');
                    loadComments();
                } else {
                    alert('답글 작성 실패.');
                }
            }
        };

        xhr.open('POST', '/comment/reply');
        xhr.send(formData);
    };

    // 댓글 수정
    const modifyComment = (commentIndex, newContent) => {
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('index', commentIndex);
        formData.append('content', newContent);

        xhr.onreadystatechange = () => {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    alert('댓글이 수정되었습니다.');
                    loadComments(); // 댓글 목록 다시 불러오기
                } else {
                    alert('댓글 수정 실패. 잠시 후 다시 시도해 주세요.');
                }
            }
        };

        xhr.open('PATCH', '/comment/');
        xhr.send(formData);
    };

    // 댓글 삭제
    const deleteComment = (commentIndex) => {
        if (!confirm('정말로 삭제하시겠습니까?')) return;

        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status >= 200 && xhr.status < 300) {
                    alert('댓글이 삭제되었습니다.');
                    loadComments(); // 댓글 목록 새로 고침
                } else {
                    alert('댓글 삭제 실패.');
                }
            }
        };

        xhr.open('DELETE', `/comment/delete?commentId=${commentIndex}`);
        xhr.send();
    };

    // 댓글 작성 이벤트
    $commentForm.onsubmit = (e) => {
        e.preventDefault();
        postComment($commentForm);
    };

    // 초기 로드
    loadComments();
});

const createCommentItem = (comment) => {
    const $commentItem = document.createElement('li');
    $commentItem.className = 'item';
    if (comment.commentId) {
        $commentItem.classList.add('reply'); // 대댓글 클래스 추가
    }

    // 삭제된 댓글은 표시하지 않거나 표시 메시지 처리
    if (comment.is_deleted) {
        const $deletedMessage = document.createElement('div');
        $deletedMessage.className = 'deleted-message';
        $deletedMessage.textContent = '삭제된 댓글입니다.';
        $commentItem.appendChild($deletedMessage);
        return $commentItem;
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

    // 댓글 내용
    const $contentDiv = document.createElement('div');
    $contentDiv.className = 'content';
    $contentDiv.innerHTML = comment.comment.replace(/\n/g, '<br>');
    $commentItem.appendChild($contentDiv);

    // 버튼 컨테이너
    const $actionContainer = document.createElement('div');
    $actionContainer.className = 'action-container';

    const $replyButton = document.createElement('button');
    $replyButton.className = 'action';
    $replyButton.textContent = '답글 쓰기';
    $replyButton.addEventListener('click', () => {
        // 버튼 숨기지 않도록
        $contentDiv.style.visibility = 'hidden'; // 기존 내용 숨기기
        $actionContainer.style.visibility = 'hidden'; // 버튼 숨기기

        // 답글 작성 폼 생성 후 댓글 아래에 삽입
        const $replyForm = createReplyForm(comment.index);
        $commentItem.appendChild($replyForm);
    });

    const $modifyButton = document.createElement('button');
    $modifyButton.className = 'action';
    $modifyButton.textContent = '수정';
    $modifyButton.addEventListener('click', () => {
        // 기존 댓글 내용 숨기기
        $contentDiv.style.visibility = 'hidden'; // 기존 내용 숨기기
        $actionContainer.style.visibility = 'hidden'; // 버튼 숨기기

        // 수정 폼 생성 후 댓글 아래에 삽입
        const $modifyForm = createModifyForm(comment.index, comment.comment, $contentDiv, $actionContainer, $commentItem);
    });

    const $deleteButton = document.createElement('button');
    $deleteButton.className = 'action';
    $deleteButton.textContent = '삭제';
    $deleteButton.addEventListener('click', () => deleteComment(comment.index));

    $actionContainer.appendChild($replyButton);
    $actionContainer.appendChild($modifyButton);
    $actionContainer.appendChild($deleteButton);
    $commentItem.appendChild($actionContainer);

    return $commentItem;
};


