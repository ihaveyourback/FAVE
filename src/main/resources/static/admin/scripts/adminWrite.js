const $mainForm = document.getElementById('mainForm')

const {
    ClassicEditor,
    Alignment,
    Autoformat,
    AutoImage,
    Autosave,
    Base64UploadAdapter,
    BlockQuote,
    Bold,
    CloudServices,
    Essentials,
    Heading,
    ImageBlock,
    ImageCaption,
    ImageInline,
    ImageInsert,
    ImageInsertViaUrl,
    ImageResize,
    ImageStyle,
    ImageTextAlternative,
    ImageToolbar,
    ImageUpload,
    Indent,
    IndentBlock,
    Italic,
    Link,
    LinkImage,
    List,
    ListProperties,
    MediaEmbed,
    Paragraph,
    PasteFromOffice,
    Table,
    TableCaption,
    TableCellProperties,
    TableColumnResize,
    TableProperties,
    TableToolbar,
    TextTransformation,
    TodoList,
    Underline
} = window.CKEDITOR;

/**
 * This is a 24-hour evaluation key. Create a free account to use CDN: https://portal.ckeditor.com/checkout?plan=free
 */
const LICENSE_KEY =
    'eyJhbGciOiJFUzI1NiJ9.eyJleHAiOjE3MzM2MTU5OTksImp0aSI6ImVhYmY0ODJmLTRkOWUtNDg3NS1iNDg2LTBkNTJhNzE4ZjlhZCIsImxpY2Vuc2VkSG9zdHMiOlsiKi53ZWJjb250YWluZXIuaW8iLCIqLmpzaGVsbC5uZXQiLCIqLmNzcC5hcHAiLCJjZHBuLmlvIiwiMTI3LjAuMC4xIiwibG9jYWxob3N0IiwiMTkyLjE2OC4qLioiLCIxMC4qLiouKiIsIjE3Mi4qLiouKiIsIioudGVzdCIsIioubG9jYWxob3N0IiwiKi5sb2NhbCJdLCJkaXN0cmlidXRpb25DaGFubmVsIjpbImNsb3VkIiwiZHJ1cGFsIiwic2giXSwibGljZW5zZVR5cGUiOiJldmFsdWF0aW9uIiwidmMiOiIwNTRlZjFkYiJ9.TbMI-UxH9DHt8kzi2IR9uHo12IpL6LfQM1S8esRPZk4tKIW0Q7SyLyBWKouKGbX7zsMRZSAnwb821MroKPhXoA';

const editorConfig = {
    toolbar: {
        items: [
            'heading',
            '|',
            'bold',
            'italic',
            'underline',
            '|',
            'link',
            'insertImage',
            'mediaEmbed',
            'insertTable',
            'blockQuote',
            '|',
            'alignment',
            '|',
            'bulletedList',
            'numberedList',
            'todoList',
            'outdent',
            'indent'
        ],
        shouldNotGroupWhenFull: false
    },
    plugins: [
        Alignment,
        Autoformat,
        AutoImage,
        Autosave,
        Base64UploadAdapter,
        BlockQuote,
        Bold,
        CloudServices,
        Essentials,
        // Heading,
        ImageBlock,
        ImageCaption,
        ImageInline,
        ImageInsert,
        ImageInsertViaUrl,
        ImageResize,
        ImageStyle,
        ImageTextAlternative,
        ImageToolbar,
        ImageUpload,
        Indent,
        IndentBlock,
        Italic,
        Link,
        LinkImage,
        List,
        ListProperties,
        MediaEmbed,
        Paragraph,
        PasteFromOffice,
        Table,
        TableCaption,
        TableCellProperties,
        TableColumnResize,
        TableProperties,
        TableToolbar,
        TextTransformation,
        TodoList,
        Underline
    ],
    heading: {
        options: [
            {
                model: 'paragraph',
                title: 'Paragraph',
                class: 'ck-heading_paragraph'
            },
            {
                model: 'heading1',
                view: 'h1',
                title: 'Heading 1',
                class: 'ck-heading_heading1'
            },
            {
                model: 'heading2',
                view: 'h2',
                title: 'Heading 2',
                class: 'ck-heading_heading2'
            },
            {
                model: 'heading3',
                view: 'h3',
                title: 'Heading 3',
                class: 'ck-heading_heading3'
            },
            {
                model: 'heading4',
                view: 'h4',
                title: 'Heading 4',
                class: 'ck-heading_heading4'
            },
            {
                model: 'heading5',
                view: 'h5',
                title: 'Heading 5',
                class: 'ck-heading_heading5'
            },
            {
                model: 'heading6',
                view: 'h6',
                title: 'Heading 6',
                class: 'ck-heading_heading6'
            }
        ]
    },
    image: {
        toolbar: [
            'toggleImageCaption',
            'imageTextAlternative',
            '|',
            'imageStyle:inline',
            'imageStyle:wrapText',
            'imageStyle:breakText',
            '|',
            'resizeImage'
        ]
    },
    initialData: '',
    language: 'ko',
    licenseKey: LICENSE_KEY,
    link: {
        addTargetToExternalLinks: true,
        defaultProtocol: 'https://',
        decorators: {
            toggleDownloadable: {
                mode: 'manual',
                label: 'Downloadable',
                attributes: {
                    download: 'file'
                }
            }
        }
    },
    list: {
        properties: {
            styles: true,
            startIndex: true,
            reversed: true
        }
    },
    placeholder: 'Type or paste your content here!',
    table: {
        contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
    }
};
// $main.onsubmit = (e) => {
//     e.preventDefault()
//     // console.log(editor.getData())
//     if ($main['title'].value === '') {
//         return;
//     }
//     if ($main['nickname'.value === '']) {
//         return;
//     }
//     if ($main['password'.value === '']) {
//         return;
//     }
//     if ($main['password'.value !== $main['passwordCheck'].value]) {
//         alert('입력하신 비밀번호가 서로 일치하지 않습니다.')
//         $main['passwordCheck'].focus();
//         return;
//     }
//     const xhr = new XMLHttpRequest();
//     const formData = new FormData();
//     formData.append('nickname', $main['nickname'].value); // 닉네임 싣고
//     formData.append('password', $main['password'].value); // 비밀번호 싣고
//     formData.append('title', $main['title'].value); // 제목 싣고
//     formData.append('content', editor.getData()); // 내용 싣고
//     xhr.onreadystatechange = () => {
//         if (xhr.readyState !== XMLHttpRequest.DONE) {
//             return;
//         }
//         if (xhr.status < 200 || xhr.status >= 300) {
//             alert('게시글을 작성하지 못하였습니다. 잠시 후 다시 시도해 주세요.')
//             return;
//         }
//         const response = JSON.parse(xhr.responseText);
//         if (response['result'] === true) {
//             location.href = `./read?index=${response['index']}`
//         } else {
//             alert('게시글을 작성하지 못하였습니다. 잠시 후 다시 시도해 주세요.')
//         }
//         // TODO
//     };
//     xhr.open('POST', location.href);
//     xhr.send(formData); // xhr.send() 그냥 이렇게 보내면 닉네임 비밀번호 제목 내용 안 보내짐
// };

// region XHR
ClassicEditor.create($mainForm['description'], editorConfig).then((editor) => {
    $mainForm.onsubmit = (e) => {
        e.preventDefault();
        if ($mainForm['title'].value === '') {
            alert('제목을 입력하지 않았습니다. 제목을 작성해 주세요.')
            return;
        }
        if (!document.getElementById('sample6_address').value.trim()) {
            alert('주소를 입력하지 않았습니다. 주소를 입력해 주세요.');
            return;
        }
        if (!document.getElementById('sample6_detailAddress').value.trim()) {
            alert('상세주소를 입력하지 않았습니다. 상세주소를 입력해 주세요.');
            return;
        }
        if (!$mainForm['startDate'].value || $mainForm['startDate'].value === null) {
            alert('축제 시작일을 입력하지 않았습니다. 시작일을 입력해주세요.');
            return;
        }
        if (!$mainForm['endDate'].value || $mainForm['endDate'].value === null) {
            alert('축제 종료일을 입력하지 않았습니다. 종료일을 입력해주세요.');
            return;
        }
        if (new Date($mainForm['startDate'].value) > new Date($mainForm['endDate'].value)) {
            alert('시작일은 종료일보다 늦을 수 없습니다.');
            return;
        }
        if (!$mainForm['coverData'].files || !$mainForm['coverData'].files.length) {
            alert('배너 이미지를 추가하지 않았습니다. 이미지를 추가 해주세요.');
            return;
        }
        const allowedMimeTypes = ['image/jpeg', 'image/png', 'image/gif'];
        const file = $mainForm['coverData'].files[0];
        if (!allowedMimeTypes.includes(file.type)) {
            alert('허용되지 않는 파일 형식입니다. JPG, PNG 또는 GIF 이미지를 업로드하세요.');
            return;
        }
        const address = document.getElementById('sample6_address').value.trim() + ' ' + document.getElementById('sample6_detailAddress').value.trim() + ' ' + document.getElementById('sample6_extraAddress').value.trim();
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('title', $mainForm['title'].value);
        formData.append('location', address);
        formData.append('startDate', $mainForm['startDate'].value);
        formData.append('endDate', $mainForm['endDate'].value);
        formData.append('coverData', $mainForm['coverData'].files[0]); // 파일 객체
        formData.append('description', editor.getData());
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            if (xhr.status < 200 || xhr.status >= 300) {
                alert('게시글 작성에 실패 하였습니다.');
                return;
            }
            const response = JSON.parse(xhr.responseText);
            console.log(response)
            if (response['result'] === 'true') {
                location.href = `./`
                alert('작성 성공')
            } else {
                alert('작성 실패')
                history.back();
            }
        };
        xhr.open('POST', location.href);
        xhr.send(formData);
    }
});
// endregion

// region 배너 이미지 이벤트 발생 시 파일찾기 이벤트 실행
document.addEventListener('DOMContentLoaded', () => {
    const coverImage = document.getElementById('coverImage');
    const coverInput = document.getElementById('coverInput');

    // 커버 이미지 클릭 이벤트
    document.querySelector('.preview-wrapper').addEventListener('click', () => {
        document.getElementById('coverInput').click();
    });

    // 파일 선택 시 이미지 미리보기
    coverInput.addEventListener('change', (event) => {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e) => {
                coverImage.src = e.target.result; // 미리보기 이미지 업데이트
                coverImage.style.display = 'block'; // 이미지 표시
            };
            reader.readAsDataURL(file);
        }
    });
});
// endregion






























