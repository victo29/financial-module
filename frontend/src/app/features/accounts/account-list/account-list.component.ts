import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ReactiveFormsModule } from '@angular/forms';
import { AccountService } from '../../../core/services/account.service';
import { Account, AccountStatus } from '../../../core/models/account.model';

@Component({
  selector: 'app-account-list',
  standalone: true,
  imports: [
    CommonModule, RouterModule, MatTableModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatSnackBarModule,
    MatSelectModule, MatFormFieldModule, ReactiveFormsModule
  ],
  template: `
    <div style="padding: 24px">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
        <h2>Contas</h2>
        <button mat-raised-button color="primary" routerLink="new">
          <mat-icon>add</mat-icon> Abrir Conta
        </button>
      </div>
      <table mat-table [dataSource]="accounts" style="width:100%">
        <ng-container matColumnDef="number">
          <th mat-header-cell *matHeaderCellDef>Número</th>
          <td mat-cell *matCellDef="let a">{{ a.number }}</td>
        </ng-container>
        <ng-container matColumnDef="customerName">
          <th mat-header-cell *matHeaderCellDef>Cliente</th>
          <td mat-cell *matCellDef="let a">{{ a.customerName }}</td>
        </ng-container>
        <ng-container matColumnDef="productName">
          <th mat-header-cell *matHeaderCellDef>Produto</th>
          <td mat-cell *matCellDef="let a">{{ a.productName }}</td>
        </ng-container>
        <ng-container matColumnDef="balance">
          <th mat-header-cell *matHeaderCellDef>Saldo</th>
          <td mat-cell *matCellDef="let a">{{ a.balance | currency:'BRL' }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let a">
            <mat-chip [color]="a.status === 'ACTIVE' ? 'primary' : 'warn'" highlighted>{{ a.status }}</mat-chip>
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Ações</th>
          <td mat-cell *matCellDef="let a">
            <button mat-icon-button [routerLink]="[a.id]"><mat-icon>visibility</mat-icon></button>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </div>
  `
})
export class AccountListComponent implements OnInit {
  accounts: Account[] = [];
  displayedColumns = ['number', 'customerName', 'productName', 'balance', 'status', 'actions'];

  constructor(private accountService: AccountService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.accountService.getAll().subscribe(a => this.accounts = a);
  }
}
