import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Product, ProductRequest } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly url = '/api/v1/products';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Product[]> {
    return this.http.get<any>(this.url).pipe(map(r => r.data));
  }
  getById(id: string): Observable<Product> {
    return this.http.get<any>(`${this.url}/${id}`).pipe(map(r => r.data));
  }
  create(request: ProductRequest): Observable<Product> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
  update(id: string, request: ProductRequest): Observable<Product> {
    return this.http.put<any>(`${this.url}/${id}`, request).pipe(map(r => r.data));
  }
  toggleStatus(id: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/status`, {});
  }
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
