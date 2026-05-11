import type { PluginListenerHandle } from '@capacitor/core';

import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AdMobError } from '../shared';

import type { AppOpenAdOptions } from './app-open-ad-options.interface';
import type { AppOpenAdPluginEvents } from './app-open-ad-plugin-events.enum';

export type AppOpenDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<
  AppOpenAdPluginEvents,
  AppOpenAdPlugin
>;

export interface AppOpenAdPlugin {
  /**
   * Load an App Open ad
   */
  loadAppOpen(options: AppOpenAdOptions): Promise<void>;

  /**
   * Shows the App Open ad if loaded
   */
  showAppOpen(): Promise<void>;

  /**
   * Check if the App Open ad is loaded
   */
  isAppOpenLoaded(): Promise<{ value: boolean }>;

  addListener(eventName: AppOpenAdPluginEvents.Loaded, listenerFunc: () => void): Promise<PluginListenerHandle>;

  addListener(
    eventName: AppOpenAdPluginEvents.FailedToLoad,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;

  addListener(eventName: AppOpenAdPluginEvents.Opened, listenerFunc: () => void): Promise<PluginListenerHandle>;

  addListener(eventName: AppOpenAdPluginEvents.Closed, listenerFunc: () => void): Promise<PluginListenerHandle>;

  addListener(
    eventName: AppOpenAdPluginEvents.FailedToShow,
    listenerFunc: (error: AdMobError) => void,
  ): Promise<PluginListenerHandle>;
}
