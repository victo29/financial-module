import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TransactionService } from '../../../core/services/transaction.service';
import { AccountService } from '../../../core/services/account.service';
import { Account } from '../../../core/models/account.model';
import { TransactionType } from '../../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatSnackBarModule
  ],
  template: `
    <div style="padding: 24px; max-width: 600px">
      <h2>Nova Transação</h2>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Conta de Origem</mat-label>
          <mat-select formControlName="accountId">
            <mat-option *ngFor="let a of accounts" [value]="a.id">
              {{ a.number }} — {{ a.customerName }} ({{ a.balance | currency:'BRL' }})
            </mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('accountId')?.hasError('required')">Conta é obrigatória</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Tipo</mat-label>
          <mat-select formControlName="type" (selectionChange)="onTypeChange()">
            <mat-option *ngFor="let t of types" [value]="t">{{ t }}</mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('type')?.hasError('required')">Tipo é obrigatório</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Valor (R$)</mat-label>
          <input matInput type="number" formControlName="amount" step="0.01" min="0.01">
          <mat-error *ngIf="form.get('amount')?.hasError('required')">Valor é obrigatório</mat-error>
          <mat-error *ngIf="form.get('amount')?.hasError('min')">Valor deve ser maior que zero</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px" *ngIf="isTransfer">
          <mat-label>Conta de Destino</mat-label>
          <mat-select formControlName="destinationAccountId">
            <mat-option *ngFor="let a of accounts" [value]="a.id">
              {{ a.number }} — {{ a.customerName }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Descrição</mat-label>
          <input matInput formControlName="description">
        </mat-form-field>

        <div style="display:flex; gap:12px">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Executar</button>
          <button mat-button type="button" routerLink="/accounts">Cancelar</button>
        </div>
      </form>
    </div>
  `
})
export class TransactionFormComponent implements OnInit {
  form!: FormGroup;
  accounts: Account[] = [];
  types: TransactionType[] = ['DEPOSIT', 'WITHDRAWAL', 'TRANSFER'];
  isTransfer = false;

  constructor(
    private fb: FormBuilder,
    private transactionService: TransactionService,
    private accountService: AccountService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      accountId: ['', Validators.required],
      type: ['', Validators.required],
      amount: [null, [Validators.required, Validators.min(0.01)]],
      description: [''],
      destinationAccountId: [null],
    });
    this.accountService.getAll().subscribe(a => this.accounts = a.filter(x => x.status === 'ACTIVE'));
  }

  onTypeChange(): void {
    this.isTransfer = this.form.get('type')?.value === 'TRANSFER';
  }

  submit(): void {
    if (this.form.invalid) return;
    const req = { ...this.form.value };
    if (!this.isTransfer) delete req.destinationAccountId;

    this.transactionService.execute(req).subscribe(() => {
      this.snackBar.open('Transação realizada com sucesso', 'OK', { duration: 3000 });
      this.router.navigate(['/accounts']);
    });
  }
}
