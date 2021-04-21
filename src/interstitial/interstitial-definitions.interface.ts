import { AdOptions } from '../shared/ad-options.interface';
import type { PluginListenerHandle } from '@capacitor/core';
import { AdLoadInfo, AdMobError } from '../shared';
import { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import { InterstitialAdPluginEvents } from './interstitial-ad-plugin-events.enum';


// This is just to validate that we do not forget to implement any event name
export type InterstitialDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<InterstitialAdPluginEvents, InterstitialDefinitions>;

export interface InterstitialDefinitions {
  /**
   * Prepare interstitial banner
   *
   * @group Interstitial
   * @param options AdOptions
   * @since 1.1.2
   */
   prepareInterstitial(options: AdOptions): Promise<AdLoadInfo>;

   /**
    * Show interstitial ad when it’s ready
    *
    * @group Interstitial
    * @since 1.1.2
    */
  showInterstitial(): Promise<void>;
  
  addListener(
    eventName: InterstitialAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): PluginListenerHandle;

  addListener (
    eventName: InterstitialAdPluginEvents.Loaded,
    listenerFunc: (info: AdLoadInfo ) => void,
  ): PluginListenerHandle;


  addListener(
    eventName: InterstitialAdPluginEvents.Dismissed,
    listenerFunc: () => void,
  ): PluginListenerHandle;

  
  addListener(
    eventName: InterstitialAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ) : PluginListenerHandle;


  addListener (
    eventName: InterstitialAdPluginEvents.Showed,
    listenerFunc: () => void,
  ): PluginListenerHandle;
}