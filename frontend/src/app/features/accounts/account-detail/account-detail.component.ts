import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { AccountService } from '../../../core/services/account.service';
import { Account, AccountStatus } from '../../../core/models/account.model';
import { Transaction } from '../../../core/models/transaction.model';

@Component({
  selector: 'app-account-detail',
  standalone: true,
  imports: [
    CommonModule, RouterModule, MatCardModule, MatTableModule,
    MatButtonModule, MatChipsModule, MatSelectModule,
    MatFormFieldModule, MatSnackBarModule, FormsModule
  ],
  template: `
    <div style="padding: 24px" *ngIf="account">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
        <h2>Conta {{ account.number }}</h2>
        <button mat-button routerLink="/accounts">Voltar</button>
      </div>

      <mat-card style="margin-bottom:24px">
        <mat-card-content>
          <p><strong>Cliente:</strong> {{ account.customerName }}</p>
          <p><strong>Produto:</strong> {{ account.productName }}</p>
          <p><strong>Saldo:</strong> {{ account.balance | currency:'BRL' }}</p>
          <p><strong>Status:</strong>
            <mat-chip [color]="account.status === 'ACTIVE' ? 'primary' : 'warn'" highlighted>{{ account.status }}</mat-chip>
          </p>
          <p><strong>Aberta em:</strong> {{ account.openedAt | date:'dd/MM/yyyy HH:mm' }}</p>

          <div style="margin-top:12px">
            <mat-form-field appearance="outline">
              <mat-label>Alterar Status</mat-label>
              <mat-select [(ngModel)]="selectedStatus">
                <mat-option value="ACTIVE">ACTIVE</mat-option>
                <mat-option value="BLOCKED">BLOCKED</mat-option>
                <mat-option value="CLOSED">CLOSED</mat-option>
              </mat-select>
            </mat-form-field>
            <button mat-raised-button color="accent" (click)="changeStatus()" style="margin-left:12px">Alterar</button>
          </div>
        </mat-card-content>
      </mat-card>

      <h3>Extrato</h3>
      <table mat-table [dataSource]="transactions" style="width:100%">
        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef>Tipo</th>
          <td mat-cell *matCellDef="let t">{{ t.type }}</td>
        </ng-container>
        <ng-container matColumnDef="amount">
          <th mat-header-cell *matHeaderCellDef>Valor</th>
          <td mat-cell *matCellDef="let t">{{ t.amount | currency:'BRL' }}</td>
        </ng-container>
        <ng-container matColumnDef="description">
          <th mat-header-cell *matHeaderCellDef>Descrição</th>
          <td mat-cell *matCellDef="let t">{{ t.description ?? '—' }}</td>
        </ng-container>
        <ng-container matColumnDef="balanceAfter">
          <th mat-header-cell *matHeaderCellDef>Saldo Após</th>
          <td mat-cell *matCellDef="let t">{{ t.balanceAfter | currency:'BRL' }}</td>
        </ng-container>
        <ng-container matColumnDef="createdAt">
          <th mat-header-cell *matHeaderCellDef>Data</th>
          <td mat-cell *matCellDef="let t">{{ t.createdAt | date:'dd/MM/yyyy HH:mm' }}</td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </div>
  `
})
export class AccountDetailComponent implements OnInit {
  account?: Account;
  transactions: Transaction[] = [];
  displayedColumns = ['type', 'amount', 'description', 'balanceAfter', 'createdAt'];
  selectedStatus: AccountStatus = 'ACTIVE';

  constructor(
    private accountService: AccountService,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.accountService.getById(id).subscribe(a => {
      this.account = a;
      this.selectedStatus = a.status;
    });
    this.accountService.getStatement(id).subscribe(t => this.transactions = t);
  }

  changeStatus(): void {
    this.accountService.changeStatus(this.account!.id, this.selectedStatus).subscribe(a => {
      this.account = a;
      this.snackBar.open('Status alterado', 'OK', { duration: 3000 });
    });
  }
}
