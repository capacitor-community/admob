import { Injectable, WritableSignal } from '@angular/core';
import { BannerAdPluginEvents } from '@capacitor-community/admob';
import { ValidationTestItem } from './validation-test-item';

@Injectable({ providedIn: 'root' })
export class ValidationResultService {
  async update(
    items: WritableSignal<ValidationTestItem[]>,
    name: string,
    result: boolean | undefined,
    value?: unknown,
  ): Promise<void> {
    let isChanged = false;

    items.update((currentItems) =>
      currentItems.map((item) => {
        if (item.name !== name || item.result !== undefined || isChanged) {
          return item;
        }

        isChanged = true;
        return { ...item, result: this.#resolveResult(item, result, value) };
      }),
    );

    await new Promise<void>((resolve) => setTimeout(resolve, 1000));
  }

  #resolveResult(item: ValidationTestItem, result: boolean | undefined, value: unknown): boolean | undefined {
    if (item.expect === undefined) {
      return result;
    }

    if (Array.isArray(item.expect) && value !== undefined) {
      return item.expect.some((expected) => String(expected) === String(value));
    }

    if (item.name === BannerAdPluginEvents.SizeChanged) {
      return this.#matchesBannerSize(item.expect as number, value as { width: number; height: number });
    }

    if (item.expect === 'error') {
      return this.#isAdMobError(value);
    }

    return false;
  }

  #matchesBannerSize(expected: number, value: { width: number; height: number }): boolean {
    return expected === 0 ? value.width === 0 && value.height === 0 : value.width > 0 && value.height > 0;
  }

  #isAdMobError(value: unknown): boolean {
    return typeof value === 'object' && value !== null && 'code' in value && 'message' in value;
  }
}
