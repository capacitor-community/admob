import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideIonicAngular } from '@ionic/angular/standalone';
import { vi } from 'vitest';

vi.mock('@capacitor-community/admob', () => ({
  AdMob: {
    initialize: vi.fn().mockResolvedValue(undefined),
    setApplicationMuted: vi.fn().mockResolvedValue(undefined),
    setApplicationVolume: vi.fn().mockResolvedValue(undefined),
  },
}));

import { App } from './app';

describe('App', () => {
  let fixture: ComponentFixture<App>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideIonicAngular(), provideRouter([])],
    });
    fixture = TestBed.createComponent(App);
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});
