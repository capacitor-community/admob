# Breaking Changes
## 0.2.0
__app.component.ts__
```ts
    import { Plugins } from '@capacitor/core';
    
    const { AdMob } = Plugins;
    
    @Component({
      selector: 'app-root',
      templateUrl: 'app.component.html',
      styleUrls: ['app.component.scss']
    })
    export class AppComponent {
        constructor(){
            // Initialize AdMob for your Application
+           AdMob.initialize('[APP_ID]');
-           AdMob.initialize();
        }
    }
```

__admob.component.ts__
```ts
    import { Plugins } from '@capacitor/core';
    import { AdOptions, AdSize, AdPosition } from '@rdlabo/capacitor-admob';
    
    const { AdMob } = Plugins;
    
    @Component({
      selector: 'admob',
      templateUrl: 'admob.component.html',
      styleUrls: ['admob.component.scss']
    })
    export class AdMobComponent {
    
        const options: AdOptions = {
            adId: 'YOUR ADID',
            adSize: AdSize.BANNER,
            position: AdPosition.BOTTOM_CENTER,
-           margin: '0',
+           margin: 0,
        }
    
        constructor(){
            // Show Banner Ad
            AdMob.showBanner(this.options)
            .then(
                (value) => {
                    console.log(value);  // true
                },
                (error) => {
                    console.error(error); // show error
                }
            );
    
            // Subscibe Banner Event Listener
            AdMob.addListener('onAdLoaded', (info: boolean) => {
                 console.log("Banner Ad Loaded");
            });
    
+           // Get Banner Size
+           AdMob.addListener('onAdSize', (info: boolean) => {
+                console.log(info);
+           });
        }
    }
```
