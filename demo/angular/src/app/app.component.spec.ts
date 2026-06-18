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

import { AppComponent } from './app.component';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideIonicAngular(), provideRouter([])],
    });
    fixture = TestBed.createComponent(AppComponent);
  });

  it('should create', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });
});
