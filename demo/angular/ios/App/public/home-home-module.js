(window["webpackJsonp"] = window["webpackJsonp"] || []).push([["home-home-module"],{

/***/ "A3+G":
/*!*********************************************!*\
  !*** ./src/app/home/home-routing.module.ts ***!
  \*********************************************/
/*! exports provided: HomePageRoutingModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "HomePageRoutingModule", function() { return HomePageRoutingModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "mrSG");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "fXoL");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/router */ "tyNb");
/* harmony import */ var _home_page__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./home.page */ "zpKS");




const routes = [
    {
        path: '',
        component: _home_page__WEBPACK_IMPORTED_MODULE_3__["HomePage"],
    }
];
let HomePageRoutingModule = class HomePageRoutingModule {
};
HomePageRoutingModule = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"])([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
        imports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"].forChild(routes)],
        exports: [_angular_router__WEBPACK_IMPORTED_MODULE_2__["RouterModule"]]
    })
], HomePageRoutingModule);



/***/ }),

/***/ "UbpF":
/*!******************************************************************!*\
  !*** /Users/saninn/dev/saninn/capacitor/admob/dist/esm/index.js ***!
  \******************************************************************/
/*! exports provided: AdSize, AdPosition */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _definitions__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./definitions */ "pK9J");
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "AdSize", function() { return _definitions__WEBPACK_IMPORTED_MODULE_0__["AdSize"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "AdPosition", function() { return _definitions__WEBPACK_IMPORTED_MODULE_0__["AdPosition"]; });


//# sourceMappingURL=index.js.map

/***/ }),

/***/ "WcN3":
/*!***************************************************************************!*\
  !*** ./node_modules/raw-loader/dist/cjs.js!./src/app/home/home.page.html ***!
  \***************************************************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony default export */ __webpack_exports__["default"] = ("<ion-header [translucent]=\"true\">\n  <ion-toolbar>\n    <ion-title>capacitor-admob</ion-title>\n  </ion-toolbar>\n</ion-header>\n\n<ion-content [fullscreen]=\"true\">\n  <ion-header collapse=\"condense\">\n    <ion-toolbar>\n      <ion-title size=\"large\">capacitor-admob</ion-title>\n    </ion-toolbar>\n  </ion-header>\n\n  <section class=\"ion-text-center ion-padding-top\" *ngIf=\"isLoading\">\n    <ion-spinner></ion-spinner>\n  </section>\n\n  <ion-list>\n    <ion-list-header><ion-label>Banner</ion-label></ion-list-header>\n    <ion-item detail=\"true\" (click)=\"showTopBanner()\">Show Top Banner</ion-item>\n    <ion-item detail=\"true\" (click)=\"showBottomBanner()\">Show Bottom Banner</ion-item>\n    <ion-item detail=\"true\" (click)=\"hideBanner()\">Hide Banner</ion-item>\n    <ion-item detail=\"true\" (click)=\"resumeBanner()\">Resume Banner</ion-item>\n    <ion-item detail=\"true\" lines=\"full\" (click)=\"removeBanner()\">Remove Banner</ion-item>\n  </ion-list>\n\n  <ion-list>\n    <ion-list-header><ion-label>Interstitial</ion-label></ion-list-header>\n    <ion-item detail=\"true\" (click)=\"prepareInterstitial()\" [disabled]=\"isPrepareInterstitial\">Prepare Interstitial</ion-item>\n    <ion-item detail=\"true\" lines=\"full\" (click)=\"showInterstitial()\" [disabled]=\"!isPrepareInterstitial\">Show Interstitial</ion-item>\n  </ion-list>\n\n  <ion-list>\n    <ion-list-header><ion-label>Reward</ion-label></ion-list-header>\n    <ion-item detail=\"true\" (click)=\"prepareReward()\" [disabled]=\"isPrepareReward\">Prepare Reward</ion-item>\n    <ion-item detail=\"true\" lines=\"full\" (click)=\"showReward()\" [disabled]=\"!isPrepareReward\">Show Reward</ion-item>\n  </ion-list>\n</ion-content>\n\n<ion-footer>\n  <ion-toolbar color=\"primary\" class=\"ion-text-center\">Footer Components</ion-toolbar>\n</ion-footer>\n");

/***/ }),

/***/ "ct+p":
/*!*************************************!*\
  !*** ./src/app/home/home.module.ts ***!
  \*************************************/
/*! exports provided: HomePageModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "HomePageModule", function() { return HomePageModule; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "mrSG");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "fXoL");
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ "ofXK");
/* harmony import */ var _ionic_angular__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @ionic/angular */ "TEn/");
/* harmony import */ var _angular_forms__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/forms */ "3Pt+");
/* harmony import */ var _home_page__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./home.page */ "zpKS");
/* harmony import */ var _home_routing_module__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./home-routing.module */ "A3+G");







let HomePageModule = class HomePageModule {
};
HomePageModule = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"])([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
        imports: [
            _angular_common__WEBPACK_IMPORTED_MODULE_2__["CommonModule"],
            _angular_forms__WEBPACK_IMPORTED_MODULE_4__["FormsModule"],
            _ionic_angular__WEBPACK_IMPORTED_MODULE_3__["IonicModule"],
            _home_routing_module__WEBPACK_IMPORTED_MODULE_6__["HomePageRoutingModule"]
        ],
        declarations: [_home_page__WEBPACK_IMPORTED_MODULE_5__["HomePage"]]
    })
], HomePageModule);



/***/ }),

/***/ "f6od":
/*!*************************************!*\
  !*** ./src/app/home/home.page.scss ***!
  \*************************************/
/*! exports provided: default */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony default export */ __webpack_exports__["default"] = ("#container {\n  text-align: center;\n  position: absolute;\n  left: 0;\n  right: 0;\n  top: 50%;\n  transform: translateY(-50%);\n}\n\n#container strong {\n  font-size: 20px;\n  line-height: 26px;\n}\n\n#container p {\n  font-size: 16px;\n  line-height: 22px;\n  color: #8c8c8c;\n  margin: 0;\n}\n\n#container a {\n  text-decoration: none;\n}\n/*# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL2hvbWUucGFnZS5zY3NzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0VBQ0Usa0JBQUE7RUFFQSxrQkFBQTtFQUNBLE9BQUE7RUFDQSxRQUFBO0VBQ0EsUUFBQTtFQUNBLDJCQUFBO0FBQUY7O0FBR0E7RUFDRSxlQUFBO0VBQ0EsaUJBQUE7QUFBRjs7QUFHQTtFQUNFLGVBQUE7RUFDQSxpQkFBQTtFQUVBLGNBQUE7RUFFQSxTQUFBO0FBRkY7O0FBS0E7RUFDRSxxQkFBQTtBQUZGIiwiZmlsZSI6ImhvbWUucGFnZS5zY3NzIiwic291cmNlc0NvbnRlbnQiOlsiI2NvbnRhaW5lciB7XG4gIHRleHQtYWxpZ246IGNlbnRlcjtcblxuICBwb3NpdGlvbjogYWJzb2x1dGU7XG4gIGxlZnQ6IDA7XG4gIHJpZ2h0OiAwO1xuICB0b3A6IDUwJTtcbiAgdHJhbnNmb3JtOiB0cmFuc2xhdGVZKC01MCUpO1xufVxuXG4jY29udGFpbmVyIHN0cm9uZyB7XG4gIGZvbnQtc2l6ZTogMjBweDtcbiAgbGluZS1oZWlnaHQ6IDI2cHg7XG59XG5cbiNjb250YWluZXIgcCB7XG4gIGZvbnQtc2l6ZTogMTZweDtcbiAgbGluZS1oZWlnaHQ6IDIycHg7XG5cbiAgY29sb3I6ICM4YzhjOGM7XG5cbiAgbWFyZ2luOiAwO1xufVxuXG4jY29udGFpbmVyIGEge1xuICB0ZXh0LWRlY29yYXRpb246IG5vbmU7XG59Il19 */");

/***/ }),

/***/ "pK9J":
/*!************************************************************************!*\
  !*** /Users/saninn/dev/saninn/capacitor/admob/dist/esm/definitions.js ***!
  \************************************************************************/
/*! exports provided: AdSize, AdPosition */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AdSize", function() { return AdSize; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AdPosition", function() { return AdPosition; });
/**
 *  For more information:
 *  https://developers.google.com/admob/ios/banner#banner_sizes
 *  https://developers.google.com/android/reference/com/google/android/gms/ads/AdSize
 * */
var AdSize;
(function (AdSize) {
    /**
     * Mobile Marketing Association (MMA)
     * banner ad size (320x50 density-independent pixels).
     */
    AdSize["BANNER"] = "BANNER";
    /**
     * A dynamically sized banner that matches its parent's
     * width and expands/contracts its height to match the ad's
     * content after loading completes.
     */
    AdSize["FLUID"] = "FLUID";
    /**
     * Interactive Advertising Bureau (IAB)
     * full banner ad size (468x60 density-independent pixels).
     */
    AdSize["FULL_BANNER"] = "FULL_BANNER";
    /**
     * Large banner ad size (320x100 density-independent pixels).
     */
    AdSize["LARGE_BANNER"] = "LARGE_BANNER";
    /**
     * Interactive Advertising Bureau (IAB)
     * leaderboard ad size (728x90 density-independent pixels).
     */
    AdSize["LEADERBOARD"] = "LEADERBOARD";
    /**
     * Interactive Advertising Bureau (IAB)
     * medium rectangle ad size (300x250 density-independent pixels).
     */
    AdSize["MEDIUM_RECTANGLE"] = "MEDIUM_RECTANGLE";
    /**
     * A dynamically sized banner that is full-width and auto-height.
     */
    AdSize["SMART_BANNER"] = "SMART_BANNER";
    /**
     * To define a custom banner size, set your desired AdSize
     */
    AdSize["CUSTOM"] = "CUSTOM";
})(AdSize || (AdSize = {}));
/**
 * @see https://developer.android.com/reference/android/widget/LinearLayout#attr_android:gravity
 */
var AdPosition;
(function (AdPosition) {
    /**
     * Banner position be top-center
     */
    AdPosition["TOP_CENTER"] = "TOP_CENTER";
    /**
     * Banner position be center
     */
    AdPosition["CENTER"] = "CENTER";
    /**
     * Banner position be bottom-center(default)
     */
    AdPosition["BOTTOM_CENTER"] = "BOTTOM_CENTER";
})(AdPosition || (AdPosition = {}));
//# sourceMappingURL=definitions.js.map

/***/ }),

/***/ "zpKS":
/*!***********************************!*\
  !*** ./src/app/home/home.page.ts ***!
  \***********************************/
/*! exports provided: HomePage */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "HomePage", function() { return HomePage; });
/* harmony import */ var tslib__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! tslib */ "mrSG");
/* harmony import */ var _raw_loader_home_page_html__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! raw-loader!./home.page.html */ "WcN3");
/* harmony import */ var _home_page_scss__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./home.page.scss */ "f6od");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! @angular/core */ "fXoL");
/* harmony import */ var _capacitor_core__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @capacitor/core */ "9baa");
/* harmony import */ var _ionic_angular__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @ionic/angular */ "TEn/");
/* harmony import */ var _capacitor_community_admob__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @capacitor-community/admob */ "UbpF");







const { AdMob } = _capacitor_core__WEBPACK_IMPORTED_MODULE_4__["Plugins"];
let HomePage = class HomePage {
    constructor(toastCtrl) {
        this.toastCtrl = toastCtrl;
        /**
         * Height of AdSize
         */
        this.appMargin = 0;
        /**
         * For ion-item of template disabled
         */
        this.isPrepareBanner = false;
        this.isPrepareReward = false;
        this.isPrepareInterstitial = false;
        /**
         * Setting of Ads
         */
        this.bannerTopOptions = {
            adId: 'ca-app-pub-3940256099942544/2934735716',
            adSize: _capacitor_community_admob__WEBPACK_IMPORTED_MODULE_6__["AdSize"].SMART_BANNER,
            position: _capacitor_community_admob__WEBPACK_IMPORTED_MODULE_6__["AdPosition"].TOP_CENTER,
        };
        this.bannerBottomOptions = {
            adId: 'ca-app-pub-3940256099942544/2934735716',
            adSize: _capacitor_community_admob__WEBPACK_IMPORTED_MODULE_6__["AdSize"].SMART_BANNER,
            position: _capacitor_community_admob__WEBPACK_IMPORTED_MODULE_6__["AdPosition"].BOTTOM_CENTER,
            npa: true,
        };
        this.rewardOptions = {
            adId: 'ca-app-pub-3940256099942544/5224354917',
        };
        this.interstitialOptions = {
            adId: 'ca-app-pub-3940256099942544/1033173712',
        };
        this.isLoading = false;
    }
    ngOnInit() {
        /**
         * Run every time the Ad height changes.
         * AdMob cannot be displayed above the content, so create margin for AdMob.
         */
        this.eventOnAdSize = AdMob.addListener('onAdSize', (info) => {
            this.appMargin = parseInt(info.height, 10);
            if (this.appMargin > 0) {
                const body = document.querySelector('body');
                const bodyStyles = window.getComputedStyle(body);
                const safeAreaBottom = bodyStyles.getPropertyValue("--ion-safe-area-bottom");
                const app = document.querySelector('ion-router-outlet');
                if (this.bannerPosition === 'top') {
                    app.style.marginTop = this.appMargin + 'px';
                }
                else {
                    app.style.marginBottom = `calc(${safeAreaBottom} + ${this.appMargin}px)`;
                }
            }
        });
        AdMob.addListener('onInterstitialAdLoaded', (info) => {
            this.isPrepareInterstitial = true;
        });
        /**
         * RewardedVideo ad
         */
        this.eventPrepareReward = AdMob.addListener('onRewardedVideoAdLoaded', (info) => {
            this.isPrepareReward = true;
            this.isLoading = false;
        });
        AdMob.addListener('onRewarded', (info) => {
            this.eventRewardReceived = info;
        });
        AdMob.addListener('onRewardedVideoAdClosed', (info) => Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            if (this.eventRewardReceived) {
                const toast = yield this.toastCtrl.create({
                    message: `AdMob Reward received with currency: ${this.eventRewardReceived.type}, amount ${this.eventRewardReceived.amount}.`,
                    duration: 2000,
                });
                yield toast.present();
            }
        }));
    }
    ngOnDestroy() {
        if (this.eventOnAdSize) {
            this.eventOnAdSize.remove();
        }
        if (this.eventPrepareReward) {
            this.eventPrepareReward.remove();
        }
    }
    /**
     * ==================== BANNER ====================
     */
    showTopBanner() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            this.bannerPosition = 'top';
            const result = yield AdMob.showBanner(this.bannerTopOptions)
                .catch(e => console.log(e));
            if (result === undefined) {
                return;
            }
            this.isPrepareBanner = true;
        });
    }
    showBottomBanner() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            this.bannerPosition = 'bottom';
            const result = yield AdMob.showBanner(this.bannerBottomOptions)
                .catch(e => console.log(e));
            if (result === undefined) {
                return;
            }
            this.isPrepareBanner = true;
        });
    }
    hideBanner() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            const result = yield AdMob.hideBanner()
                .catch(e => console.log(e));
            if (result === undefined) {
                return;
            }
            const app = document.querySelector('ion-router-outlet');
            app.style.marginTop = '0px';
            app.style.marginBottom = '0px';
        });
    }
    resumeBanner() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            const result = yield AdMob.resumeBanner()
                .catch(e => console.log(e));
            if (result === undefined) {
                return;
            }
            const app = document.querySelector('ion-router-outlet');
            app.style.marginBottom = this.appMargin + 'px';
        });
    }
    removeBanner() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            const result = yield AdMob.removeBanner()
                .catch(e => console.log(e));
            if (result === undefined) {
                return;
            }
            const app = document.querySelector('ion-router-outlet');
            app.style.marginBottom = '0px';
            this.appMargin = 0;
            this.isPrepareBanner = false;
        });
    }
    /**
     * ==================== /BANNER ====================
     */
    /**
     * ==================== REWARD ====================
     */
    prepareReward() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            this.isLoading = true;
            const result = yield AdMob.prepareRewardVideoAd(this.rewardOptions)
                .catch(e => console.log(e))
                .finally(() => this.isLoading = false);
            if (result === undefined) {
                return;
            }
        });
    }
    showReward() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            this.eventRewardReceived = undefined;
            const result = AdMob.showRewardVideoAd()
                .catch(e => console.log(e));
            if (result === undefined) {
                return;
            }
            this.isPrepareReward = false;
        });
    }
    /**
     * ==================== /REWARD ====================
     */
    /**
     * ==================== Interstitial ====================
     */
    prepareInterstitial() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            this.isLoading = true;
            const result = AdMob.prepareInterstitial(this.interstitialOptions)
                .catch(e => console.log(e))
                .finally(() => this.isLoading = false);
            if (result === undefined) {
                return;
            }
        });
    }
    showInterstitial() {
        return Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__awaiter"])(this, void 0, void 0, function* () {
            const result = yield AdMob.showInterstitial()
                .catch(e => console.log(e));
            if (result === undefined) {
                return;
            }
            this.isPrepareInterstitial = false;
        });
    }
};
HomePage.ctorParameters = () => [
    { type: _ionic_angular__WEBPACK_IMPORTED_MODULE_5__["ToastController"] }
];
HomePage = Object(tslib__WEBPACK_IMPORTED_MODULE_0__["__decorate"])([
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_3__["Component"])({
        selector: 'app-home',
        template: _raw_loader_home_page_html__WEBPACK_IMPORTED_MODULE_1__["default"],
        styles: [_home_page_scss__WEBPACK_IMPORTED_MODULE_2__["default"]]
    })
], HomePage);



/***/ })

}]);
//# sourceMappingURL=home-home-module.js.map