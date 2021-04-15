import { AdOptions } from '../shared/ad-options.interface';
import type { PluginListenerHandle } from '@capacitor/core';

export interface InterstitialDefinitions {
  /**
   * Prepare interstitial banner
   *
   * @group Interstitial
   * @param options AdOptions
   * @since 1.1.2
   */
   prepareInterstitial(options: AdOptions): Promise<void>;

   /**
    * Show interstitial ad when it’s ready
    *
    * @group Interstitial
    * @since 1.1.2
    */
  showInterstitial(): Promise<void>;
  
    /**
   * Notice: Interstitial ad opened
   *
   * @group fullscreen
   * @param eventName adDidPresentFullScreenContent
   * @param listenerFunc
   * @since 3.0.0
   */
     addListener(
      eventName: 'adDidPresentFullScreenContent',
      listenerFunc: (info: any) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: Dismiss Content
     *
     * @group fullscreen
     * @param eventName adDidDismissFullScreenContent
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'adDidDismissFullScreenContent',
      listenerFunc: (info: any) => void,
    ): PluginListenerHandle;
  
    /**
     * Notice: Interstitial ad is be failed to open
     *
     * @group fullscreen
     * @param eventName didFailToPresentFullScreenContentWithError
     * @param listenerFunc
     * @since 3.0.0
     */
    addListener(
      eventName: 'didFailToPresentFullScreenContentWithError',
      listenerFunc: (info: AdMobError) => void,
    ): PluginListenerHandle;
}