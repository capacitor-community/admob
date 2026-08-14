import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class BannerViewportService {
  setMargin(height: number, position: 'top' | 'bottom'): void {
    const outlet = document.querySelector<HTMLElement>('ion-router-outlet');
    if (outlet === null) {
      return;
    }

    this.clearMargin();
    if (height === 0) {
      return;
    }

    if (position === 'top') {
      outlet.style.marginTop = `${height}px`;
      return;
    }

    const safeAreaBottom = window.getComputedStyle(document.body).getPropertyValue('--ion-safe-area-bottom');
    outlet.style.marginBottom = `calc(${safeAreaBottom} + ${height}px)`;
  }

  clearMargin(): void {
    const outlet = document.querySelector<HTMLElement>('ion-router-outlet');
    if (outlet === null) {
      return;
    }

    outlet.style.marginTop = '';
    outlet.style.marginBottom = '';
  }
}
