import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TransactionService } from './transaction.service';
import { Transaction, TransactionRequest } from '../models/transaction.model';

describe('TransactionService', () => {
  let service: TransactionService;
  let httpMock: HttpTestingController;

  const API_URL = '/api/v1/transactions';

  const buildTransaction = (type: Transaction['type'], before: number, after: number): Transaction => ({
    id: 'txn-001',
    accountId: 'acc-001',
    type,
    amount: Math.abs(after - before),
    balanceBefore: before,
    balanceAfter: after,
    status: 'SUCCESS',
    createdAt: '2024-01-01T10:00:00',
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TransactionService],
    });
    service = TestBed.inject(TransactionService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ─── DEPOSIT ────────────────────────────────────────────────────────────

  describe('given a DEPOSIT transaction request for R$500.00', () => {
    it('when execute() is called, then it should POST to /transactions and emit the transaction with balanceAfter increased', () => {
      const request: TransactionRequest = {
        accountId: 'acc-001',
        type: 'DEPOSIT',
        amount: 500,
      };
      const savedTransaction = buildTransaction('DEPOSIT', 0, 500);
      let result: Transaction | undefined;

      service.execute(request).subscribe(t => (result = t));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ status: 'success', data: savedTransaction });

      expect(result?.type).toBe('DEPOSIT');
      expect(result?.balanceAfter).toBe(500);
      expect(result?.status).toBe('SUCCESS');
    });
  });

  // ─── WITHDRAWAL ─────────────────────────────────────────────────────────

  describe('given a WITHDRAWAL transaction request for R$300.00', () => {
    it('when execute() is called, then it should POST to /transactions and emit the transaction with balanceAfter decreased', () => {
      const request: TransactionRequest = {
        accountId: 'acc-001',
        type: 'WITHDRAWAL',
        amount: 300,
        description: 'Saque caixa eletrônico',
      };
      const savedTransaction = buildTransaction('WITHDRAWAL', 1000, 700);
      let result: Transaction | undefined;

      service.execute(request).subscribe(t => (result = t));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.body).toEqual(request);
      req.flush({ status: 'success', data: savedTransaction });

      expect(result?.type).toBe('WITHDRAWAL');
      expect(result?.balanceBefore).toBe(1000);
      expect(result?.balanceAfter).toBe(700);
    });
  });

  // ─── TRANSFER ───────────────────────────────────────────────────────────

  describe('given a TRANSFER transaction request with a destination account', () => {
    it('when execute() is called, then it should POST to /transactions with the destination account ID in the body', () => {
      const request: TransactionRequest = {
        accountId: 'acc-001',
        type: 'TRANSFER',
        amount: 400,
        destinationAccountId: 'acc-002',
      };
      const savedTransaction = buildTransaction('TRANSFER', 1000, 600);
      let result: Transaction | undefined;

      service.execute(request).subscribe(t => (result = t));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.body.destinationAccountId).toBe('acc-002');
      req.flush({ status: 'success', data: savedTransaction });

      expect(result?.type).toBe('TRANSFER');
      expect(result?.balanceAfter).toBe(600);
    });
  });

  // ─── DESCRIPTION ────────────────────────────────────────────────────────

  describe('given a transaction request with a custom description', () => {
    it('when execute() is called, then the description should be included in the POST body', () => {
      const request: TransactionRequest = {
        accountId: 'acc-001',
        type: 'DEPOSIT',
        amount: 100,
        description: 'Salário mensal',
      };

      service.execute(request).subscribe();

      const req = httpMock.expectOne(API_URL);
      expect(req.request.body.description).toBe('Salário mensal');
      req.flush({ status: 'success', data: buildTransaction('DEPOSIT', 0, 100) });
    });
  });
});
