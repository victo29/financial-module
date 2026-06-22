import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Customer, CustomerRequest } from '../models/customer.model';
import { Account } from '../models/account.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly url = '/api/v1/customers';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Customer[]> {
    return this.http.get<any>(this.url).pipe(map(r => r.data));
  }
  getById(id: string): Observable<Customer> {
    return this.http.get<any>(`${this.url}/${id}`).pipe(map(r => r.data));
  }
  getAccounts(id: string): Observable<Account[]> {
    return this.http.get<any>(`${this.url}/${id}/accounts`).pipe(map(r => r.data));
  }
  create(request: CustomerRequest): Observable<Customer> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
  update(id: string, request: CustomerRequest): Observable<Customer> {
    return this.http.put<any>(`${this.url}/${id}`, request).pipe(map(r => r.data));
  }
  toggleStatus(id: string): Observable<void> {
    return this.http.patch<void>(`${this.url}/${id}/status`, {});
  }
}
