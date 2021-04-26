import { Injectable, NgZone } from '@angular/core';
import {ITestItems} from './interfaces';

@Injectable({
  providedIn: 'root'
})
export class HelperService {

  constructor(private zone: NgZone) { }

  /**
   * items is not Deep Copy, this is substitution
   */
  public async updateItem(items: ITestItems[], name: string, result: boolean, time = 500) {
    this.zone.run(() => {
      items = items.map((item) => {
        if (item.name === name) {
          item.result = result;
        }
        return item;
      });
    });
    await new Promise(resolve => setTimeout(() => resolve(), time));
  }
}
