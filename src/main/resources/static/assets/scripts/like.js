// 좋아요
document.addEventListener('DOMContentLoaded', () => {
    const likeButton = document.querySelector('.like');
    const unlikeButton = document.querySelector('.unlike');
    const parentElement = likeButton.closest('div'); // 버튼이 포함된 부모 요소 선택

    likeButton.addEventListener('click', () => {
        parentElement.classList.add('liked');
    });

    unlikeButton.addEventListener('click', () => {
        parentElement.classList.remove('liked');
    });
});