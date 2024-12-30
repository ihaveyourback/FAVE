document.addEventListener('DOMContentLoaded', () => {
    const $button = document.querySelectorAll('.user-button')

    $button.forEach(button => {
        button.addEventListener('click', () => {
            const index = button.getAttribute('data-index');
            location.href = `/admin/modify/?index=${index}`
        })
    })
})