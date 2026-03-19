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
   * Carga un anuncio App Open
   */
  loadAppOpen(options: AppOpenAdOptions): Promise<void>;

  /**
   * Muestra el anuncio App Open si está cargado
   */
  showAppOpen(): Promise<void>;

  /**
   * Verifica si el anuncio App Open está cargado
   */
  isAppOpenLoaded(): Promise<{ value: boolean }>;

  /**
   * Agrega listeners para eventos de App Open
   */
  addListener(
    eventName: AppOpenAdPluginEvents,
    listenerFunc: (...args: any[]) => void,
  ): Promise<PluginListenerHandle>;
}
