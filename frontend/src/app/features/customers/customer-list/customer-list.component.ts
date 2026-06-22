import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer } from '../../../core/models/customer.model';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, RouterModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule],
  template: `
    <div style="padding: 24px">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
        <h2>Clientes</h2>
        <button mat-raised-button color="primary" routerLink="new">
          <mat-icon>add</mat-icon> Novo Cliente
        </button>
      </div>
      <table mat-table [dataSource]="customers" style="width:100%">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Nome</th>
          <td mat-cell *matCellDef="let c">{{ c.name }}</td>
        </ng-container>
        <ng-container matColumnDef="cpf">
          <th mat-header-cell *matHeaderCellDef>CPF</th>
          <td mat-cell *matCellDef="let c">{{ c.cpf }}</td>
        </ng-container>
        <ng-container matColumnDef="email">
          <th mat-header-cell *matHeaderCellDef>E-mail</th>
          <td mat-cell *matCellDef="let c">{{ c.email }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let c">
            <mat-chip [color]="c.status === 'ACTIVE' ? 'primary' : 'warn'" highlighted>{{ c.status }}</mat-chip>
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Ações</th>
          <td mat-cell *matCellDef="let c">
            <button mat-icon-button [routerLink]="[c.id, 'edit']"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button (click)="toggleStatus(c)">
              <mat-icon>{{ c.status === 'ACTIVE' ? 'toggle_on' : 'toggle_off' }}</mat-icon>
            </button>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </div>
  `
})
export class CustomerListComponent implements OnInit {
  customers: Customer[] = [];
  displayedColumns = ['name', 'cpf', 'email', 'status', 'actions'];

  constructor(private customerService: CustomerService, private snackBar: MatSnackBar) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.customerService.getAll().subscribe(c => this.customers = c);
  }

  toggleStatus(customer: Customer): void {
    this.customerService.toggleStatus(customer.id).subscribe(() => {
      this.snackBar.open('Status alterado', 'OK', { duration: 3000 });
      this.load();
    });
  }
}
