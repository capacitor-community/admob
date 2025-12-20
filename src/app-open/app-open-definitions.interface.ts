import type { PluginListenerHandle } from '@capacitor/core';
import type { ValidateAllEventsEnumAreImplemented } from '../private/validate-all-events-implemented.type';
import type { AppOpenAdPluginEvents } from './app-open-ad-plugin-events.enum';
import type { AppOpenAdOptions } from './app-open-ad-options.interface';

export type AppOpenDefinitionsHasAllEvents = ValidateAllEventsEnumAreImplemented<
  AppOpenAdPluginEvents,
  AppOpenAdPlugin
>;

export interface AppOpenAdPlugin {
  /**
   * Load an ad App Open
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

  /**
   * Add listeners for App Open events
   */
  addListener(
    eventName: AppOpenAdPluginEvents,
    listenerFunc: (...args: any[]) => void,
  ): Promise<PluginListenerHandle>;
}
