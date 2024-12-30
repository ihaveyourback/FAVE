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

ClassicEditor.create($mainForm['description'], editorConfig).then((editor) => {
    editorInstance = editor;

    editor.setData($mainForm['description'].getAttribute('data-value'));

    $mainForm.onsubmit = (e) => {
        e.preventDefault();

        const title = $mainForm['title'].value.trim();
        const address = document.getElementById('sample6_address').value.trim();
        const startDate = $mainForm['startDate'].value;
        const endDate = $mainForm['endDate'].value;
        const coverData = $mainForm['coverData'].files[0];
        const description = editor.getData();

        if (!title) {
            alert('제목을 입력하지 않았습니다. 제목을 작성해 주세요.');
            return;
        }

        if (!address) {
            alert('주소를 입력하지 않았습니다. 주소를 입력해 주세요.');
            return;
        }

        if (!startDate) {
            alert('축제 시작일을 입력하지 않았습니다. 시작일을 입력해주세요.');
            return;
        }

        if (!endDate) {
            alert('축제 종료일을 입력하지 않았습니다. 종료일을 입력해주세요.');
            return;
        }

        if (new Date(startDate) > new Date(endDate)) {
            alert('시작일은 종료일보다 늦을 수 없습니다.');
            return;
        }

        const fullAddress = `${address} ${document.getElementById('sample6_detailAddress').value.trim()} ${document.getElementById('sample6_extraAddress').value.trim()}`;

        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('title', title);
        formData.append('location', fullAddress);
        formData.append('startDate', startDate);
        formData.append('endDate', endDate);
        formData.append('coverData', coverData);
        formData.append('description', description);

        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }

            if (xhr.status < 200 || xhr.status >= 300) {
                alert('게시글 작성에 실패 하였습니다.');
                return;
            }

            const response = JSON.parse(xhr.responseText);
            if (response['result'] === 'true') {
                alert('작성 성공');
            } else {
                alert('작성 실패');
                history.back();
            }
        };

        const index = location.search;
        xhr.open('POST', `/admin/modify/${index}`);
        xhr.send(formData);
    };
});

const coverImage = document.getElementById('coverImage');
const coverInput = document.getElementById('coverInput');

document.querySelector('.preview-wrapper').addEventListener('click', () => {
    coverInput.click();
});

coverInput.addEventListener('change', (event) => {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            coverImage.src = e.target.result;
            coverImage.style.display = 'block';
        };
        reader.readAsDataURL(file);
    }
});
