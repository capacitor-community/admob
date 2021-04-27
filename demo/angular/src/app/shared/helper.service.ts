import { Injectable, NgZone } from '@angular/core';
import {ITestItems} from './interfaces';
import {BannerAdPluginEvents} from '../../../../../dist/esm/banner';

@Injectable({
  providedIn: 'root'
})
export class HelperService {

  constructor(private zone: NgZone) { }

  /**
   * items is not Deep Copy, this is substitution
   */
  public async updateItem(items: ITestItems[], name: string, result: boolean, value: any = undefined) {
    this.zone.run(() => {
      let isChanged = false;
      items = items.map((item) => {
        if (item.name === name && item.result === undefined && !isChanged) {
          isChanged = true;
          if (item.expect === undefined) {
            item.result = result;
          } else {
            if (item.name === BannerAdPluginEvents.SizeChanged) {
              item.result = this.bannerAdPluginEventsSizeChanged(item.expect, value);
            } else if (item.expect === 'error') {
              item.result = this.receiveErrorValue(value)
            }
          }
        }
        return item;
      });
    });
    await new Promise(resolve => setTimeout(() => resolve(), 1000));
  }

  private bannerAdPluginEventsSizeChanged(expect: number | string, value: any): boolean {
    if (expect === 0) {
      return value.width === 0 && value.height === 0;
    }
    return value.width > 0 && value.height > 0
  }

  private receiveErrorValue(value: any): boolean {
    console.log(['これ', value])
    return value.hasOwnProperty('code') &&  value.hasOwnProperty('message');
  }
}
