import { WebPlugin } from '@capacitor/core';
import { AdMobPlugin, AdOptions } from './definitions';

export class AdMobWeb extends WebPlugin implements AdMobPlugin {
  constructor() {
    super({
      name: 'AdMob',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return Promise.resolve({ value: options.value });
  }

  async createBanner(adOptions: AdOptions): Promise<{response: boolean}> {
    console.log("ECHO", adOptions);
    return Promise.resolve({response: true});
  }
}

const AdMob = new AdMobWeb();

export { AdMob };
