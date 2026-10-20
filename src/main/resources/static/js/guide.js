document.addEventListener('DOMContentLoaded', () => {
    new Guide().init();
});

class Guide {
    constructor() {
        this.currentIndex = 0;

        /**memo: デバッグ用のパス指定
         * サーバー起動とブラウザのみでパス認識が変わるので、条件判定でprefixを切り替え
         */
        const isLocal = window.location.protocol === 'file:';
        const prefix = isLocal ? '../static' : '';

        this.images = {
            pcImages: [
                prefix + '/images/pc/pc_1.jpg',
                prefix + '/images/pc/pc_2.jpg',
                prefix + '/images/pc/pc_3.jpg',
                prefix + '/images/pc/pc_4.jpg'
            ],
            phoneImages: [
                prefix + '/images/phone/phone_1.jpg',
                prefix + '/images/phone/phone_2.jpg',
                prefix + '/images/phone/phone_3.jpg',
                prefix + '/images/phone/phone_4.jpg',
                prefix + '/images/phone/phone_5.jpg',
                prefix + '/images/phone/phone_6.jpg'
            ]
        };
        this.currentImages = this.images.pcImages;
        // 初回ロード時の画像を保持しておく(参照を外さない)
        this.cache = [];
        this.preloadImages();
    }

    /**memo: 画像のプリロード
     * 今までは切り替えたタイミングでダウンロードが走っていた
     * 先にロードを終わらせるためのメソッド
     * cacheに入れておかないとガベージコレクションによって削除されてしまう
     * `...配列`:   スプレッド構文 配列の前に"..."をつけることで要素をバラバラに取り出せる
     *              取り出した値を配列の中に格納して、大きな配列としている
     * forEach:     完成した配列をもとに、画面には表示されないimgタグのエレメントを作る
     *              これによってブラウザはすぐにダウンロードを始める
     */
    preloadImages() {
        // PC用とスマホ用のパスを結合して、すべての画像パスの配列を作る
        const allImagePaths = [...this.images.pcImages, ...this.images.phoneImages];

        allImagePaths.forEach(path => {
            const img = new Image(); // 画面には表示されない「仮想の画像タグ」を作る
            img.src = path;          // srcにパスを入れた瞬間に、ブラウザがダウンロードしてキャッシュ（記憶）する
            this.cache.push(img);    // キャッシュに保存
        });
    }

    init() {
        const prevBtnElm = document.getElementById('prevBtn');
        prevBtnElm.addEventListener('click', () => {
            this.prev();
        });

        const nextBtnElm = document.getElementById('nextBtn');
        nextBtnElm.addEventListener('click', () => {
            this.next();
        });

        const pcBtnElm = document.querySelector('.pc');
        pcBtnElm.addEventListener('click', () => {
            this.changePc();
        });

        const phoneBtnElm = document.querySelector('.phone');
        phoneBtnElm.addEventListener('click', () => {
            this.changePhone();
        });

        this.updateGuide();
    }

    updateGuide() {
        const frameElm = document.getElementById('frame');
        const image = this.currentImages[this.currentIndex];

        // innerHTMLの全書き換えをやめ、既存imgのsrc属性だけを更新する
        let imgElm = frameElm.querySelector('img');
        if (!imgElm) {
            // 初回表示時のみimgタグを作成
            frameElm.innerHTML = `<img src="${image}">`;
        } else {
            // 2回目以降はsrc属性のみを書き換える（DOM再生成を防ぐ）
            imgElm.src = image;
        }
    }

    get lastIndex() {
        return this.currentImages.length - 1;
    }

    prev() {
        if (this.currentIndex === 0) {
            this.currentIndex = this.lastIndex;
        } else {
            this.currentIndex--;
        }
        this.updateGuide();
    }

    next() {
        if (this.currentIndex === this.lastIndex) {
            this.currentIndex = 0;
        } else {
            this.currentIndex++;
        }
        this.updateGuide();
    }

    changePc() {
        this.currentImages = this.images.pcImages;
        this.currentIndex = 0;
        this.updateGuide();
    }

    changePhone() {
        this.currentImages = this.images.phoneImages;
        this.currentIndex = 0;
        this.updateGuide();
    }
}