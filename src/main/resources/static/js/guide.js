document.addEventListener('DOMContentLoaded', () => {
    new Guide().init();
});

class Guide {
    constructor() {
        this.currentIndex = 0;
        this.images = {
            // パス指定がわからない
            // thymeleafの機能を使わず、普通にパス指定でいい？
            pcImages: [
                '/images/pc/pc_1.jpg',
                '/images/pc/pc_2.jpg',
                '/images/pc/pc_3.jpg',
                '/images/pc/pc_4.jpg'
            ],
            phoneImages: [
                '/images/phone/phone_1.jpg',
                '/images/phone/phone_2.jpg',
                '/images/phone/phone_3.jpg',
                '/images/phone/phone_4.jpg',
                '/images/phone/phone_5.jpg',
                '/images/phone/phone_6.jpg'
            ]
        };
        this.currentImages = this.images.pcImages;
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

        frameElm.innerHTML = `<img src="${image}">`;
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