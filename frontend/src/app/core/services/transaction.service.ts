import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Transaction, TransactionRequest } from '../models/transaction.model';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly url = '/api/v1/transactions';
  constructor(private http: HttpClient) {}

  execute(request: TransactionRequest): Observable<Transaction> {
    return this.http.post<any>(this.url, request).pipe(map(r => r.data));
  }
}
