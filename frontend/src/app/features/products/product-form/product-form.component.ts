import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { ProductType } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule],
  template: `
    <div style="padding: 24px; max-width: 600px">
      <h2>{{ isEdit ? 'Editar' : 'Novo' }} Produto</h2>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="name">
          <mat-error *ngIf="form.get('name')?.hasError('required')">Nome é obrigatório</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Descrição</mat-label>
          <textarea matInput formControlName="description" rows="3"></textarea>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Tipo</mat-label>
          <mat-select formControlName="type">
            <mat-option *ngFor="let t of types" [value]="t">{{ t }}</mat-option>
          </mat-select>
          <mat-error *ngIf="form.get('type')?.hasError('required')">Tipo é obrigatório</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Taxa de Juros (%)</mat-label>
          <input matInput type="number" formControlName="interestRate" step="0.01">
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Saldo Mínimo (R$)</mat-label>
          <input matInput type="number" formControlName="minBalance" step="0.01">
        </mat-form-field>

        <div style="display:flex; gap:12px">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Salvar</button>
          <button mat-button type="button" routerLink="/products">Cancelar</button>
        </div>
      </form>
    </div>
  `
})
export class ProductFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  types: ProductType[] = ['SAVINGS', 'INVESTMENT', 'CREDIT', 'CHECKING'];
  private id?: string;

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: [''],
      type: ['', Validators.required],
      interestRate: [null],
      minBalance: [0, Validators.min(0)],
    });

    this.id = this.route.snapshot.paramMap.get('id') ?? undefined;
    if (this.id) {
      this.isEdit = true;
      this.productService.getById(this.id).subscribe(p => this.form.patchValue(p));
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    const req = this.form.value;
    const action = this.isEdit
      ? this.productService.update(this.id!, req)
      : this.productService.create(req);

    action.subscribe(() => {
      this.snackBar.open('Produto salvo com sucesso', 'OK', { duration: 3000 });
      this.router.navigate(['/products']);
    });
  }
}
