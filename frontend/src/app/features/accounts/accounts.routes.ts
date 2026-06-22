import { Routes } from '@angular/router';

export const ACCOUNT_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./account-list/account-list.component').then(m => m.AccountListComponent) },
  { path: 'new', loadComponent: () => import('./account-form/account-form.component').then(m => m.AccountFormComponent) },
  { path: ':id', loadComponent: () => import('./account-detail/account-detail.component').then(m => m.AccountDetailComponent) },
];
