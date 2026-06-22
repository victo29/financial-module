import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatSidenavModule,
    MatListModule,
  ],
  template: `
    <mat-sidenav-container style="height: 100vh">
      <mat-sidenav mode="side" opened style="width: 220px; padding: 16px">
        <h3 style="margin: 0 0 16px 0">Attus Financial</h3>
        <mat-nav-list>
          <a mat-list-item routerLink="/products" routerLinkActive="active-link">
            <mat-icon matListItemIcon>inventory</mat-icon>
            <span matListItemTitle>Produtos</span>
          </a>
          <a mat-list-item routerLink="/customers" routerLinkActive="active-link">
            <mat-icon matListItemIcon>people</mat-icon>
            <span matListItemTitle>Clientes</span>
          </a>
          <a mat-list-item routerLink="/accounts" routerLinkActive="active-link">
            <mat-icon matListItemIcon>account_balance</mat-icon>
            <span matListItemTitle>Contas</span>
          </a>
          <a mat-list-item routerLink="/transactions" routerLinkActive="active-link">
            <mat-icon matListItemIcon>swap_horiz</mat-icon>
            <span matListItemTitle>Transações</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content>
        <router-outlet></router-outlet>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .active-link { background: rgba(0,0,0,0.08); border-radius: 4px; }
  `]
})
export class AppComponent {}
