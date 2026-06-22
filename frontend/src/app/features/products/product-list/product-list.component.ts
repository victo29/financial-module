import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterModule, MatTableModule, MatButtonModule, MatIconModule, MatChipsModule, MatSnackBarModule],
  template: `
    <div style="padding: 24px">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px">
        <h2>Produtos Financeiros</h2>
        <button mat-raised-button color="primary" routerLink="new">
          <mat-icon>add</mat-icon> Novo Produto
        </button>
      </div>
      <table mat-table [dataSource]="products" style="width:100%">
        <ng-container matColumnDef="name">
          <th mat-header-cell *matHeaderCellDef>Nome</th>
          <td mat-cell *matCellDef="let p">{{ p.name }}</td>
        </ng-container>
        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef>Tipo</th>
          <td mat-cell *matCellDef="let p">{{ p.type }}</td>
        </ng-container>
        <ng-container matColumnDef="interestRate">
          <th mat-header-cell *matHeaderCellDef>Taxa (%)</th>
          <td mat-cell *matCellDef="let p">{{ p.interestRate ?? '—' }}</td>
        </ng-container>
        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Status</th>
          <td mat-cell *matCellDef="let p">
            <mat-chip [color]="p.status === 'ACTIVE' ? 'primary' : 'warn'" highlighted>{{ p.status }}</mat-chip>
          </td>
        </ng-container>
        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Ações</th>
          <td mat-cell *matCellDef="let p">
            <button mat-icon-button [routerLink]="[p.id, 'edit']"><mat-icon>edit</mat-icon></button>
            <button mat-icon-button (click)="toggleStatus(p)"><mat-icon>{{ p.status === 'ACTIVE' ? 'toggle_on' : 'toggle_off' }}</mat-icon></button>
            <button mat-icon-button color="warn" (click)="delete(p.id)"><mat-icon>delete</mat-icon></button>
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </div>
  `
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  displayedColumns = ['name', 'type', 'interestRate', 'status', 'actions'];

  constructor(private productService: ProductService, private snackBar: MatSnackBar) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.productService.getAll().subscribe(p => this.products = p);
  }

  toggleStatus(product: Product): void {
    this.productService.toggleStatus(product.id).subscribe(() => {
      this.snackBar.open('Status alterado com sucesso', 'OK', { duration: 3000 });
      this.load();
    });
  }

  delete(id: string): void {
    if (confirm('Deseja excluir este produto?')) {
      this.productService.delete(id).subscribe(() => {
        this.snackBar.open('Produto excluído', 'OK', { duration: 3000 });
        this.load();
      });
    }
  }
}
