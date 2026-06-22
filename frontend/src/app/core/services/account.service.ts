import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Account, AccountRequest } from '../models/account.model';
import { Transaction } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly url = '/api/v1/accounts';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Account[]> {
    return this.http.get<any>(this.url).pipe(map(r => r.data));
  }
  getById(id: string): Observable<Account> {
    return this.http.get<any>(`${this.url}/${id}`).pipe(map(r => r.data));
  }
  getStatement(id: string): Observable<Transaction[]> {
    return this.http.get<any>(`${this.url}/${id}/transactions`).pipe(map(r => r.data?.content ?? []));
  }
  open(request: AccountRequest): Observable<Account> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
  changeStatus(id: string, status: string): Observable<Account> {
    return this.http.patch<any>(`${this.url}/${id}/status?status=${status}`, {}).pipe(map(r => r.data));
  }
}
