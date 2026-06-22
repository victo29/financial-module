import { Routes } from '@angular/router';

export const TRANSACTION_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./transaction-form/transaction-form.component').then(m => m.TransactionFormComponent) },
];
