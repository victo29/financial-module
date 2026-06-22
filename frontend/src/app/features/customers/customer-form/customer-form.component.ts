import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CustomerService } from '../../../core/services/customer.service';
import { cpfValidator } from '../../../shared/validators/cpf.validator';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule,
    MatFormFieldModule, MatInputModule, MatDatepickerModule,
    MatNativeDateModule, MatButtonModule, MatSnackBarModule
  ],
  template: `
    <div style="padding: 24px; max-width: 600px">
      <h2>{{ isEdit ? 'Editar' : 'Novo' }} Cliente</h2>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="name">
          <mat-error *ngIf="form.get('name')?.hasError('required')">Nome é obrigatório</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>CPF</mat-label>
          <input matInput formControlName="cpf" placeholder="000.000.000-00">
          <mat-error *ngIf="form.get('cpf')?.hasError('required')">CPF é obrigatório</mat-error>
          <mat-error *ngIf="form.get('cpf')?.hasError('cpfInvalid')">CPF inválido</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>E-mail</mat-label>
          <input matInput formControlName="email" type="email">
          <mat-error *ngIf="form.get('email')?.hasError('required')">E-mail é obrigatório</mat-error>
          <mat-error *ngIf="form.get('email')?.hasError('email')">E-mail inválido</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Telefone</mat-label>
          <input matInput formControlName="phone">
        </mat-form-field>

        <mat-form-field appearance="outline" style="width:100%; margin-bottom:12px">
          <mat-label>Data de Nascimento</mat-label>
          <input matInput formControlName="birthDate" type="date">
          <mat-error *ngIf="form.get('birthDate')?.hasError('required')">Data é obrigatória</mat-error>
        </mat-form-field>

        <div style="display:flex; gap:12px">
          <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Salvar</button>
          <button mat-button type="button" routerLink="/customers">Cancelar</button>
        </div>
      </form>
    </div>
  `
})
export class CustomerFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  private id?: string;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],
      cpf: ['', [Validators.required, cpfValidator]],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      birthDate: ['', Validators.required],
    });

    this.id = this.route.snapshot.paramMap.get('id') ?? undefined;
    if (this.id) {
      this.isEdit = true;
      this.customerService.getById(this.id).subscribe(c => this.form.patchValue(c));
    }
  }

  submit(): void {
    if (this.form.invalid) return;
    const req = this.form.value;
    const action = this.isEdit
      ? this.customerService.update(this.id!, req)
      : this.customerService.create(req);

    action.subscribe(() => {
      this.snackBar.open('Cliente salvo com sucesso', 'OK', { duration: 3000 });
      this.router.navigate(['/customers']);
    });
  }
}
