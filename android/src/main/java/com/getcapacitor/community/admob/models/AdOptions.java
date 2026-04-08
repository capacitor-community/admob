package com.getcapacitor.community.admob.models;

import androidx.annotation.VisibleForTesting;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.banner.BannerAdSizeEnum;
import com.getcapacitor.community.admob.rewarded.models.SsvInfo;

/**
 * Holds the options for an Ad Request
 * TODO: automatically create type definitions https://github.com/vojtechhabarta/typescript-generator ?
 */
public abstract class AdOptions {

    /**
     * The ad unit ID that you want to request
     *
     * @see <a href="https://support.google.com/admob/answer/7356431?hl=en">Find ad Unit ID of an app</a>
     */
    public final String adId;
    /**
     * Banner Ad Size, defaults to ADAPTIVE_BANNER.
     * IT can be: ADAPTIVE_BANNER, BANNER, MEDIUM_RECTANGLE,
     * FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM
     */
    public final BannerAdSizeEnum adSize;

    public static final String BANNER_TESTER_ID = "ca-app-pub-3940256099942544/6300978111";
    public static final String INTERSTITIAL_TESTER_ID = "ca-app-pub-3940256099942544/1033173712";
    public static final String REWARD_VIDEO_TESTER_ID = "ca-app-pub-3940256099942544/5224354917";
    public static final String REWARD_INTERSTITIAL_TESTER_ID = "ca-app-pub-3940256099942544/5354046379";

    /**
     * The position of the ad, it can be TOP_CENTER,
     * CENTER or BOTTOM
     * <p>
     * TODO: Make an enum
     */
    public final String position;

    /**
     * If set to true, an test app will be requested using the official sample ads unit ids
     *
     * @see <a href="https://developers.google.com/admob/android/test-ads#sample_ad_units">Sample ad units</a>
     */
    public final boolean isTesting;

    /**
     * Margin Banner. Default is 0 px;
     * If position is BOTTOM_CENTER, margin is be margin-bottom.
     * If position is TOP_CENTER, margin is be margin-top.
     */
    public final int margin;

    /**
     * The default behavior of the Google Mobile Ads SDK is to serve personalized ads.
     * Set this to true to request Non-Personalized Ads
     *
     * @see <a href="https://developers.google.com/admob/android/eu-consent">EU-Consent</a>
     * <p>
     * Default is false
     */
    public final boolean npa;

    /**
     * Used for Server side verification of Reward Ads
     */
    public final SsvInfo ssvInfo;

    private AdOptions(PluginCall call) {
        /*
         * TODO: Since the Id in the Typescript AdOptions interface is not optional
         *  the default value here should never be used. In case it is used it means this is an error.
         *  Would not be better to print an error (call.reject()) and do not create any Ad?
         *  Why? Because an distracted dev could think that everything is working when it is not.
         */

        this.adId = call.getString("adId", getTestingId());
        this.isTesting = call.getBoolean("isTesting", false);
        this.position = call.getString("position", "BOTTOM_CENTER");
        this.margin = call.getInt("margin", 0);
        this.npa = call.getBoolean("npa", false);
        this.ssvInfo = new SsvInfo(call);

        String sizeString = call.getString("adSize", BannerAdSizeEnum.ADAPTIVE_BANNER.name());
        this.adSize = AdOptions.adSizeStringToAdSizeEnum(sizeString);
    }

    private AdOptions(String id, boolean isTesting, String position, int margin, boolean npa, BannerAdSizeEnum adSize, SsvInfo ssvInfo) {
        this.adId = id;
        this.isTesting = isTesting;
        this.position = position;
        this.margin = margin;
        this.npa = npa;
        this.adSize = adSize;
        this.ssvInfo = ssvInfo;
    }

    /**
     * @return the id used for this type of test ads.
     */
    public abstract String getTestingId();

    private static BannerAdSizeEnum adSizeStringToAdSizeEnum(String sizeString) {
        try {
            return BannerAdSizeEnum.valueOf(sizeString);
        } catch (IllegalArgumentException error) {
            return BannerAdSizeEnum.ADAPTIVE_BANNER;
        }
    }

    public static class AdOptionsFactory {

        private AdOptionsFactory() {}

        public AdOptions createBannerOptions(PluginCall call) {
            return new AdOptions(call) {
                @Override
                public String getTestingId() {
                    return AdOptions.BANNER_TESTER_ID;
                }
            };
        }

        public AdOptions createInterstitialOptions(PluginCall call) {
            return new AdOptions(call) {
                @Override
                public String getTestingId() {
                    return AdOptions.INTERSTITIAL_TESTER_ID;
                }
            };
        }

        public AdOptions createRewardVideoOptions(PluginCall call) {
            return new AdOptions(call) {
                @Override
                public String getTestingId() {
                    return AdOptions.REWARD_VIDEO_TESTER_ID;
                }
            };
        }

        public AdOptions createRewardInterstitialOptions(PluginCall call) {
            return new AdOptions(call) {
                @Override
                public String getTestingId() {
                    return AdOptions.REWARD_INTERSTITIAL_TESTER_ID;
                }
            };
        }

        public AdOptions createGenericOptions(PluginCall call, final String testingID) {
            return new AdOptions(call) {
                @Override
                public String getTestingId() {
                    return testingID;
                }
            };
        }
    }

    private static AdOptionsFactory factory;

    public static AdOptionsFactory getFactory() {
        if (AdOptions.factory != null) {
            return AdOptions.factory;
        }
        AdOptions.factory = new AdOptionsFactory();
        return AdOptions.factory;
    }

    /**
     * Just for testing
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    public static class TesterAdOptionsBuilder {

        private String id = "TesterAdOptionsBuilder__defaultID";
        private String testingID = "TesterAdOptionsBuilder__testingID";
        private boolean isTesting = true;
        private String position = "TesterAdOptionsBuilder__position";
        private int margin = 1;
        private boolean npa = false;
        private BannerAdSizeEnum adSize = BannerAdSizeEnum.ADAPTIVE_BANNER;
        private SsvInfo ssvInfo = new SsvInfo();

        public TesterAdOptionsBuilder setSsvInfo(SsvInfo info) {
            ssvInfo = info;
            return this;
        }

        public TesterAdOptionsBuilder setIsTesting(boolean value) {
            isTesting = value;
            return this;
        }

        public TesterAdOptionsBuilder setNpa(boolean value) {
            npa = value;
            return this;
        }

        public TesterAdOptionsBuilder setPosition(String value) {
            position = value;
            return this;
        }

        public TesterAdOptionsBuilder setTestingID(String value) {
            testingID = value;
            return this;
        }

        public TesterAdOptionsBuilder setID(String value) {
            id = value;
            return this;
        }

        public TesterAdOptionsBuilder setMargin(int value) {
            margin = value;
            return this;
        }

        public TesterAdOptionsBuilder setAdSize(BannerAdSizeEnum value) {
            adSize = value;
            return this;
        }

        public AdOptions build() {
            return new AdOptions(id, isTesting, position, margin, npa, adSize, ssvInfo) {
                @Override
                public String getTestingId() {
                    return testingID;
                }
            };
        }
    }
}
