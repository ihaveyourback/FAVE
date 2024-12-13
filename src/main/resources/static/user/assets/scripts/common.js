HTMLElement.prototype.hide = function () {
    this.classList.remove('-visible');
    return this;
}

HTMLElement.prototype.show = function () {
    this.classList.add('-visible');
    return this;
}
/**
 *
 * @param {string} dataId
 * @return {HTMLLabelElement}
 */
HTMLFormElement.prototype.findLabel = function (dataId) {
    return this.querySelector(`label.--obj-label[data-id="${dataId}"]`)
}

/**
 *@param{boolean} b
 * @param {string|undefined} warningText
 * @returns {HTMLLabelElement}
 */
HTMLLabelElement.prototype.setValid = function (b, warningText = undefined) {
    if (b === true) {
        this.classList.remove('-invalid');
    } else if (b === false) {
        this.classList.add('-invalid');
        if (typeof warningText === 'string') {
            this.querySelector(':scope > ._warning').innerText = warningText;
        }
    }
    return this;
}

HTMLLabelElement.prototype.isValid = function () {
    return !this.classList.contains('-invalid')
}

class Dialog {
    /** @type {HTMLElement} */
    static $cover;
    /**@type {Array<HTMLElement>} */
    static $dialogArray = [];
    /**
     * @param{string} title
     * @param{string} content
     * @param{function(HTMLElement)|undefined} onclick
     */
    static defaultOk(title, content, onclick = undefined){
        Dialog.show({
            title: title,
            content: content,
            buttons: [{text: '확인', onclick: ($dialog) => {
                Dialog.hide($dialog);
                if (typeof onclick === 'function') {
                    onclick($dialog);
                }
                }
            }]
        });
    }

    static defaultYesNo(title, content, onYes = undefined, onNo = undefined){
        Dialog.show({
            title: title,
            content: content,
            buttons: [
                {
                    text: '네', onclick: ($dialog) => {
                    Dialog.hide($dialog);
                    if (typeof onYes === 'function') {
                        onYes($dialog);
                    }
                }
            },
                {
                    text: '아니요', onclick: ($dialog) => {
                        Dialog.hide($dialog);
                        if (typeof onNo === 'function') {
                            onNo($dialog);
                        }
                    },
                }
                ],
        });
    }


    /**
     * @param{HTMLElement} $dialog
     */
    static hide($dialog){
        Dialog.$dialogArray.splice(Dialog.$dialogArray.indexOf($dialog), 1);
        if (Dialog.$dialogArray.length === 0){
            Dialog.$cover.hide();
        }
        $dialog.hide();
        setTimeout(()=> $dialog.remove(), 1000);
    }

    /**
     * @param{Object} args
     * @param{string} args.title
     * @param{string} args.content
     * @param{Array<{text: string, onclick: function}> |undefined} args.buttons
     * @param{number} delay
     * @return {HTMLElement}
     */
    static show(args, delay = 50) {
        const $dialog = document.createElement('div');
        $dialog.classList.add('---dialog');
        const $title = document.createElement('div');
        $title.classList.add('_title');
        $title.innerText = args.title;
        const $content = document.createElement('div');
        $content.classList.add('_content');
        $content.innerHTML = args.content;
        $dialog.append($title, $content);
        if (args.buttons != null && args.buttons.length > 0) {
            const $buttonContainer = document.createElement('div');
            $buttonContainer.classList.add('_button-container');
            $buttonContainer.style.gridTemplateColumns = `repeat(${args.buttons.length}, 1fr)`;
            for (const button of args.buttons) {
                const $button = document.createElement('button');
                $button.classList.add('_button');
                $button.setAttribute('type', 'button');
                $button.innerText = button.text;
                if (typeof button.onclick === 'function') {
                    $button.onclick = () => button.onclick($dialog);
                }
                $buttonContainer.append($button);
            }
            $dialog.append($buttonContainer);
        }

        document.body.prepend($dialog);
        Dialog.$dialogArray.push($dialog); // 다이얼로그 배열에 현재 생성한 다이얼로그 추가(이유는 곧 밝혀짐 두두둥)
        if (Dialog.$cover == null) {
            const $cover = document.createElement('div');
            $cover.classList.add('---dialog-cover');
            document.body.prepend($cover);
            Dialog.$cover = $cover;
        }
        setTimeout(() => {
            $dialog.show();
            Dialog.$cover.show();
        }, delay); // delay 밀리초 뒤에 show 해주는 이유는, 요소 생성 직후 -visible 붙이면, 트렌지션이 안 먹기 때문
        return $dialog;
    }
}
class Loading{
    /**@type{HTMLElement} */
    static $element;
    static hide(){
        Loading.$element?.hide();
    }

    /**
     * @param{number} delay
     */
    
    static show(delay = 50){
        if (Loading.$element==null) {
            const $element = document.createElement('div');
            $element.classList.add('---loading');
            const $icon = document.createElement('img');
            $icon.classList.add('_icon');
            $icon.setAttribute('alt', '');
            $icon.setAttribute('src', 'data:image/png;base64,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');
            const $text = document.createElement('span');
            $text.classList.add('_text');
            $text.innerText = '잠시만 기다려 주세요.';
            $element.append($icon, $text);
            document.body.prepend($element);
            Loading.$element = $element;
        }
        setTimeout(()=> Loading.$element.show(), delay);
    }
    
}


