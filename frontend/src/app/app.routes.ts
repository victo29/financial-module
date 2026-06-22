import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'products', pathMatch: 'full' },
  { path: 'products', loadChildren: () => import('./features/products/products.routes').then(m => m.PRODUCT_ROUTES) },
  { path: 'customers', loadChildren: () => import('./features/customers/customers.routes').then(m => m.CUSTOMER_ROUTES) },
  { path: 'accounts', loadChildren: () => import('./features/accounts/accounts.routes').then(m => m.ACCOUNT_ROUTES) },
  { path: 'transactions', loadChildren: () => import('./features/transactions/transactions.routes').then(m => m.TRANSACTION_ROUTES) },
];
