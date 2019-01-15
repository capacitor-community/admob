package app.xplatform.capacitor.plugins;

import android.Manifest;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


@NativePlugin(
    permissions = {
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
    }
)
public class AdMob extends Plugin {

    private String appId;


    private String adId;


    private AdView adView;

    @PluginMethod()
    public void initialize(PluginCall call) {
        this.appId = call.getString("appId", " ca-app-pub-3940256099942544~3347511713");
        MobileAds.initialize(this.getContext(), this.appId);

        this.adView = new AdView(this.getContext());
    }

    @PluginMethod()
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }

    @PluginMethod()
    public void createBanner(PluginCall call) {
        this.adId = call.getString("adId", "ca-app-pub-3940256099942544/6300978111");
        this.adView.setAdUnitId(this.adId);
        this.adView.setAdSize(AdSize.SMART_BANNER);
        this.adView.loadAd(new AdRequest.Builder().build());

        this.adView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                notifyListeners("onAdLoaded", new JSObject().put("value", true));
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                notifyListeners("onAdFailedToLoad", new JSObject().put("errorCode", i));
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdOpened() {
                notifyListeners("onAdOpened", new JSObject().put("value", true));
                super.onAdOpened();
            }

            @Override
            public void onAdClosed() {
                notifyListeners("onAdClosed", new JSObject().put("value", true));
                super.onAdClosed();
            }
        });

        call.success(new JSObject().put("value", true));
    }


    @PluginMethod()
    public void hideBanner(PluginCall call) {
        this.adView.pause();
        call.resolve(new JSObject().put("value", true));
    }


    @PluginMethod()
    public void resumeBanner(PluginCall call) {
        this.adView.resume();
        call.resolve(new JSObject().put("value", true));
    }


    @PluginMethod()
    public void removeBanner(PluginCall call) {
        this.adView.destroy();
        call.resolve(new JSObject().put("value", true));
    }
}
