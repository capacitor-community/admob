package com.getcapacitor.community.admob.models;

import com.getcapacitor.PluginCall;

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
   * Banner Ad Size, defaults to SMART_BANNER.
   * IT can be: SMART_BANNER, BANNER, MEDIUM_RECTANGLE,
   * FULL_BANNER, LEADERBOARD, SKYSCRAPER, or CUSTOM
   */
  public final AdSizeEnum adSize;

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

  public AdOptions(PluginCall call) {
    /*
     * TODO: Since the Id in the Typescript AdOptions interface is not optional
     *  the default value here should never be used. In case it is used it means this is an error.
     *  Would not be better to print an error (call.error()) and do not create any Ad?
     *  Why? Because an distracted dev could think that everything is working when it is not.
     */

    this.adId = call.getString("adId", getTestingId());
    this.isTesting = call.getBoolean("isTesting", false);
    this.position = call.getString("position", "BOTTOM_CENTER");
    this.margin = call.getInt("margin", 0);
    this.npa = call.getBoolean("npa", false);

    String sizeString = call.getString("adSize", AdSizeEnum.SMART_BANNER.name);
    this.adSize = AdOptions.adSizeStringToAdSizeEnum(sizeString);
  }

  /**
   * @return the id used for this type of test ads.
   */
  public abstract String getTestingId();

  private static AdSizeEnum adSizeStringToAdSizeEnum(String sizeString) {
    try {
      return AdSizeEnum.valueOf(sizeString);
    } catch (IllegalArgumentException error) {
      return AdSizeEnum.SMART_BANNER;
    }
  }
}
