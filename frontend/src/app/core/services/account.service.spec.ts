import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AccountService } from './account.service';
import { Account, AccountRequest } from '../models/account.model';
import { Transaction } from '../models/transaction.model';

describe('AccountService', () => {
  let service: AccountService;
  let httpMock: HttpTestingController;

  const API_URL = '/api/v1/accounts';

  const mockAccount: Account = {
    id: 'acc-001',
    number: '12345678-9',
    customerId: 'cust-001',
    customerName: 'João Silva',
    productId: 'prod-001',
    productName: 'Conta Corrente',
    balance: 1000.0,
    status: 'ACTIVE',
    openedAt: '2024-01-01T00:00:00',
  };

  const mockTransaction: Transaction = {
    id: 'txn-001',
    accountId: 'acc-001',
    type: 'DEPOSIT',
    amount: 500.0,
    balanceBefore: 500.0,
    balanceAfter: 1000.0,
    status: 'SUCCESS',
    createdAt: '2024-01-02T10:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AccountService],
    });
    service = TestBed.inject(AccountService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ─── getAll ──────────────────────────────────────────────────────────────

  describe('given a GET /api/v1/accounts request', () => {
    it('when the API returns successfully, then it should emit the account array from response.data', () => {
      let result: Account[] | undefined;

      service.getAll().subscribe(accounts => (result = accounts));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: [mockAccount] });

      expect(result).toEqual([mockAccount]);
    });
  });

  // ─── getById ─────────────────────────────────────────────────────────────

  describe('given a GET /api/v1/accounts/:id request for an existing account', () => {
    it('when the API returns successfully, then it should emit the account with its balance and customer info', () => {
      let result: Account | undefined;

      service.getById('acc-001').subscribe(a => (result = a));

      const req = httpMock.expectOne(`${API_URL}/acc-001`);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: mockAccount });

      expect(result?.balance).toBe(1000.0);
      expect(result?.customerName).toBe('João Silva');
      expect(result?.status).toBe('ACTIVE');
    });
  });

  // ─── getStatement ────────────────────────────────────────────────────────

  describe('given a GET /api/v1/accounts/:id/transactions request', () => {
    it('when the API returns a paginated response, then it should emit the transactions from response.data.content', () => {
      let result: Transaction[] | undefined;

      service.getStatement('acc-001').subscribe(txns => (result = txns));

      const req = httpMock.expectOne(`${API_URL}/acc-001/transactions`);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: { content: [mockTransaction], totalElements: 1 } });

      expect(result).toEqual([mockTransaction]);
    });
  });

  describe('given a GET /api/v1/accounts/:id/transactions request with null data', () => {
    it('when the API returns a null content, then it should emit an empty array as fallback', () => {
      let result: Transaction[] | undefined;

      service.getStatement('acc-001').subscribe(txns => (result = txns));

      const req = httpMock.expectOne(`${API_URL}/acc-001/transactions`);
      req.flush({ status: 'success', data: null });

      expect(result).toEqual([]);
    });
  });

  // ─── open ────────────────────────────────────────────────────────────────

  describe('given a POST /api/v1/accounts request with a valid customer and product', () => {
    it('when the API responds with 201, then it should emit the opened account with ACTIVE status and zero balance', () => {
      const request: AccountRequest = { customerId: 'cust-001', productId: 'prod-001' };
      const newAccount: Account = { ...mockAccount, balance: 0 };
      let result: Account | undefined;

      service.open(request).subscribe(a => (result = a));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ status: 'success', data: newAccount });

      expect(result?.status).toBe('ACTIVE');
      expect(result?.balance).toBe(0);
    });
  });

  // ─── changeStatus ────────────────────────────────────────────────────────

  describe('given a PATCH /api/v1/accounts/:id/status?status=BLOCKED request', () => {
    it('when the API responds with 200, then it should emit the account with BLOCKED status', () => {
      const blockedAccount: Account = { ...mockAccount, status: 'BLOCKED' };
      let result: Account | undefined;

      service.changeStatus('acc-001', 'BLOCKED').subscribe(a => (result = a));

      const req = httpMock.expectOne(`${API_URL}/acc-001/status?status=BLOCKED`);
      expect(req.request.method).toBe('PATCH');
      req.flush({ status: 'success', data: blockedAccount });

      expect(result?.status).toBe('BLOCKED');
    });
  });

  describe('given a PATCH /api/v1/accounts/:id/status?status=CLOSED request', () => {
    it('when the API responds with 200, then it should emit the account with CLOSED status', () => {
      const closedAccount: Account = { ...mockAccount, balance: 0, status: 'CLOSED' };
      let result: Account | undefined;

      service.changeStatus('acc-001', 'CLOSED').subscribe(a => (result = a));

      const req = httpMock.expectOne(`${API_URL}/acc-001/status?status=CLOSED`);
      req.flush({ status: 'success', data: closedAccount });

      expect(result?.status).toBe('CLOSED');
    });
  });
});
