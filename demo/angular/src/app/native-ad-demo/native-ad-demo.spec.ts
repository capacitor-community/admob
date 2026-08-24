import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { beforeEach, describe, expect, it } from 'vitest';
import { NativeAdDemo } from './native-ad-demo';

describe('NativeAdDemo', () => {
  let fixture: ComponentFixture<NativeAdDemo>;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideIonicAngular()] });
    fixture = TestBed.createComponent(NativeAdDemo);
  });

  it('uses stable logical keys for each native ad row', () => {
    expect(fixture.componentInstance.vm.feedItems.filter((item) => item.kind === 'native-ad')).toEqual([
      expect.objectContaining({ id: 'ad-after-capacitor', slotKey: 'sponsored-after-capacitor' }),
      expect.objectContaining({ id: 'ad-after-ionic', slotKey: 'sponsored-after-ionic' }),
    ]);
  });
});
