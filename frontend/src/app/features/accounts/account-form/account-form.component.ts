import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AccountService } from '../../../core/services/account.service';
import { CustomerService } from '../../../core/services/customer.service';
import { ProductService } from '../../../core/services/product.service';
import { Customer } from '../../../core/models/customer.model';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-account-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule,
    MatFormFieldModule, MatSelectModule, MatButtonModule, MatSnackBarModule
  ],
  template: `
    <div style="padding: 24px; max-width: 600px">
      <h2>Abrir Conta</h2>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Cliente</mat-label>
          <mat-select formControlName="customerId">
            <mat-option *ngFor="let c of customers" [value]="c.id">{{ c.name }} — {{ c.cpf }}</mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('customerId')?.hasError('required')">Cliente é obrigatório</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Produto</mat-label>
          <mat-select formControlName="productId">
            <mat-option *ngFor="let p of products" [value]="p.id">{{ p.name }} ({{ p.type }})</mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('productId')?.hasError('required')">Produto é obrigatório</mat-error>
        </mat-form-field>

        <div style="display:flex; gap:12px">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Abrir Conta</button>
          <button mat-button type="button" routerLink="/accounts">Cancelar</button>
        </div>
      </form>
    </div>
  `
})
export class AccountFormComponent implements OnInit {
  form!: FormGroup;
  customers: Customer[] = [];
  products: Product[] = [];

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private customerService: CustomerService,
    private productService: ProductService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      customerId: ['', Validators.required],
      productId: ['', Validators.required],
    });
    this.customerService.getAll().subscribe(c => this.customers = c.filter(x => x.status === 'ACTIVE'));
    this.productService.getAll().subscribe(p => this.products = p.filter(x => x.status === 'ACTIVE'));
  }

  submit(): void {
    if (this.form.invalid) return;
    this.accountService.open(this.form.value).subscribe(() => {
      this.snackBar.open('Conta aberta com sucesso', 'OK', { duration: 3000 });
      this.router.navigate(['/accounts']);
    });
  }
}
