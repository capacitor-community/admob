import { PreloadAllModules, Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'tabs',
    loadComponent: () =>
      import('./validation/validation-shell/validation-shell').then(({ ValidationShell }) => ValidationShell),
    children: [
      {
        path: 'demo',
        loadComponent: () => import('./demo/demo').then(({ Demo }) => Demo),
      },
      {
        path: 'banner',
        loadComponent: () =>
          import('./validation/banner-validation/banner-validation').then(({ BannerValidation }) => BannerValidation),
      },
      {
        path: 'interstitial',
        loadComponent: () =>
          import('./validation/interstitial-validation/interstitial-validation').then(
            ({ InterstitialValidation }) => InterstitialValidation,
          ),
      },
      {
        path: 'reward',
        loadComponent: () =>
          import('./validation/reward-validation/reward-validation').then(({ RewardValidation }) => RewardValidation),
      },
      {
        path: 'app-open',
        loadComponent: () =>
          import('./validation/app-open-validation/app-open-validation').then(
            ({ AppOpenValidation }) => AppOpenValidation,
          ),
      },
      {
        path: '',
        redirectTo: 'demo',
        pathMatch: 'full',
      },
    ],
  },
  {
    path: '',
    redirectTo: 'tabs/demo',
    pathMatch: 'full',
  },
];

export const routerPreloadingStrategy = PreloadAllModules;
