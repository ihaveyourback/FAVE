const $main = document.getElementById('main');

const {
    ClassicEditor,
    AccessibilityHelp,
    Autoformat,
    AutoImage,
    Autosave,
    BlockQuote,
    BlockToolbar,
    Bold,
    Code,
    Essentials,
    FontBackgroundColor,
    FontColor,
    FontFamily,
    FontSize,
    Heading,
    Highlight,
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
    RemoveFormat,
    SelectAll,
    SimpleUploadAdapter,
    SpecialCharacters,
    SpecialCharactersArrows,
    SpecialCharactersCurrency,
    SpecialCharactersEssentials,
    SpecialCharactersLatin,
    SpecialCharactersMathematical,
    SpecialCharactersText,
    Strikethrough,
    Subscript,
    Superscript,
    Table,
    TableCaption,
    TableCellProperties,
    TableColumnResize,
    TableProperties,
    TableToolbar,
    TextTransformation,
    TodoList,
    Underline,
    Undo
} = CKEDITOR;

const editorConfig = {
    toolbar: {
        items: [
            'undo',
            'redo',
            '|',
            'heading',
            '|',
            'fontSize',
            'fontFamily',
            'fontColor',
            'fontBackgroundColor',
            '|',
            'bold',
            'italic',
            'underline',
            'strikethrough',
            'subscript',
            'superscript',
            'code',
            'removeFormat',
            '|',
            'specialCharacters',
            'link',
            'insertImage',
            'mediaEmbed',
            'insertTable',
            'highlight',
            'blockQuote',
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
        AccessibilityHelp,
        Autoformat,
        AutoImage,
        Autosave,
        BlockQuote,
        BlockToolbar,
        Bold,
        Code,
        Essentials,
        FontBackgroundColor,
        FontColor,
        FontFamily,
        FontSize,
        // Heading,
        Highlight,
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
        RemoveFormat,
        SelectAll,
        SimpleUploadAdapter,
        SpecialCharacters,
        SpecialCharactersArrows,
        SpecialCharactersCurrency,
        SpecialCharactersEssentials,
        SpecialCharactersLatin,
        SpecialCharactersMathematical,
        SpecialCharactersText,
        Strikethrough,
        Subscript,
        Superscript,
        Table,
        TableCaption,
        TableCellProperties,
        TableColumnResize,
        TableProperties,
        TableToolbar,
        TextTransformation,
        TodoList,
        Underline,
        Undo
    ],
    blockToolbar: [
        'fontSize',
        'fontColor',
        'fontBackgroundColor',
        '|',
        'bold',
        'italic',
        '|',
        'link',
        'insertImage',
        'insertTable',
        '|',
        'bulletedList',
        'numberedList',
        'outdent',
        'indent'
    ],
    fontFamily: {
        supportAllValues: true
    },
    fontSize: {
        options: [10, 12, 14, 'default', 18, 20, 22],
        supportAllValues: true
    },
    // heading: {
    //     options: [
    //         {
    //             model: 'paragraph',
    //             title: 'Paragraph',
    //             class: 'ck-heading_paragraph'
    //         },
    //         {
    //             model: 'heading1',
    //             view: 'h1',
    //             title: 'Heading 1',
    //             class: 'ck-heading_heading1'
    //         },
    //         {
    //             model: 'heading2',
    //             view: 'h2',
    //             title: 'Heading 2',
    //             class: 'ck-heading_heading2'
    //         },
    //         {
    //             model: 'heading3',
    //             view: 'h3',
    //             title: 'Heading 3',
    //             class: 'ck-heading_heading3'
    //         },
    //         {
    //             model: 'heading4',
    //             view: 'h4',
    //             title: 'Heading 4',
    //             class: 'ck-heading_heading4'
    //         },
    //         {
    //             model: 'heading5',
    //             view: 'h5',
    //             title: 'Heading 5',
    //             class: 'ck-heading_heading5'
    //         },
    //         {
    //             model: 'heading6',
    //             view: 'h6',
    //             title: 'Heading 6',
    //             class: 'ck-heading_heading6'
    //         }
    //     ]
    // },

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
    initialData: $main['content'].value,
    language: 'ko',
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

    placeholder: '내용을 입력하세요!',
    table: {
        contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
    },
    simpleUpload: {
        uploadUrl: './image'
    }
};


ClassicEditor.create($main['content'], editorConfig).then((editor) => {
    $main.onsubmit = (e) => {
        e.preventDefault();
        // console.log($main['title'].value);   // 글 제목
        // console.log(editor.getData());
        if ($main['title'].value === '') {
            return;
        }
        const url = new URL(location.href);
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('index', url.searchParams.get('index')); // 주소에 있는 index
        formData.append('title', $main['title'].value); // 제목
        formData.append('content', editor.getData()); // 내용
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            if (xhr.status < 200 || xhr.status >= 300) {
                alert('게시글을 수정하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
                return;
            }
            const response = JSON.parse(xhr.responseText);
            if (response['result'] === true) {
                location.href = `./read?index=${url.searchParams.get('index')}`;
            } else {
                alert('게시글을 수정하지 못하였습니다. 잠시 후 다시 시도해 주세요.');
            }
        };
        xhr.open('PATCH', './modify');
        xhr.send(formData);
    };
});