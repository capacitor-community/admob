import { Injectable, NgZone } from '@angular/core';
import { ITestItems } from './interfaces';
import { BannerAdPluginEvents } from '../../../../../dist/esm';

@Injectable({
  providedIn: 'root',
})
export class HelperService {
  constructor(private zone: NgZone) {}

  /**
   * items is not Deep Copy, this is substitution
   */
  public async updateItem(items: ITestItems[], name: string, result: boolean | undefined, value: unknown = undefined) {
    this.zone.run(() => {
      let isChanged = false;
      items = items.map((item) => {
        if (item.name === name && item.result === undefined && !isChanged) {
          isChanged = true;
          if (item.expect === undefined) {
            item.result = result;
          } else if (Array.isArray(item.expect) && value) {
            // @ts-ignore
            item.result = item.expect.includes(value.toString());
          } else {
            if (item.name === BannerAdPluginEvents.SizeChanged) {
              item.result = this.bannerAdPluginEventsSizeChanged(
                item.expect as number,
                value as { width: number; height: number },
              );
            } else if (item.expect === 'error') {
              item.result = this.receiveErrorValue(value);
            }
          }
        }
        return item;
      });
    });
    await new Promise<void>((resolve) => setTimeout(() => resolve(), 1000));
  }

  private bannerAdPluginEventsSizeChanged(expect: number | string, value: { width: number; height: number }): boolean {
    if (expect === 0) {
      return value.width === 0 && value.height === 0;
    }
    return value.width > 0 && value.height > 0;
  }

  private receiveErrorValue(value: unknown): boolean {
    return typeof value === 'object' && value !== null && 'code' in value && 'message' in value;
  }
}
