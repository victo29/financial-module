import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CustomerService } from './customer.service';
import { Customer, CustomerRequest } from '../models/customer.model';
import { Account } from '../models/account.model';

describe('CustomerService', () => {
  let service: CustomerService;
  let httpMock: HttpTestingController;

  const API_URL = '/api/v1/customers';

  const mockCustomer: Customer = {
    id: 'cust-001',
    name: 'João Silva',
    cpf: '529.982.247-25',
    email: 'joao@email.com',
    phone: '(11) 99999-1111',
    birthDate: '1990-05-15',
    status: 'ACTIVE',
    createdAt: '2024-01-01T00:00:00',
  };

  const mockAccount: Account = {
    id: 'acc-001',
    number: '12345678-9',
    customerId: 'cust-001',
    customerName: 'João Silva',
    productId: 'prod-001',
    productName: 'Conta Corrente',
    balance: 0,
    status: 'ACTIVE',
    openedAt: '2024-01-01T00:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CustomerService],
    });
    service = TestBed.inject(CustomerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ─── getAll ──────────────────────────────────────────────────────────────

  describe('given a GET /api/v1/customers request', () => {
    it('when the API returns successfully, then it should emit the customer array from response.data', () => {
      let result: Customer[] | undefined;

      service.getAll().subscribe(customers => (result = customers));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: [mockCustomer] });

      expect(result).toEqual([mockCustomer]);
      expect(result?.length).toBe(1);
    });
  });

  // ─── getById ─────────────────────────────────────────────────────────────

  describe('given a GET /api/v1/customers/:id request for an existing customer', () => {
    it('when the API returns successfully, then it should emit the customer object from response.data', () => {
      let result: Customer | undefined;

      service.getById('cust-001').subscribe(c => (result = c));

      const req = httpMock.expectOne(`${API_URL}/cust-001`);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: mockCustomer });

      expect(result?.name).toBe('João Silva');
      expect(result?.cpf).toBe('529.982.247-25');
    });
  });

  // ─── getAccounts ─────────────────────────────────────────────────────────

  describe('given a GET /api/v1/customers/:id/accounts request', () => {
    it('when the API returns successfully, then it should emit the accounts array from response.data', () => {
      let result: Account[] | undefined;

      service.getAccounts('cust-001').subscribe(accounts => (result = accounts));

      const req = httpMock.expectOne(`${API_URL}/cust-001/accounts`);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: [mockAccount] });

      expect(result).toEqual([mockAccount]);
    });
  });

  describe('given a customer with no accounts', () => {
    it('when getAccounts is called and API returns empty array, then it should emit an empty array', () => {
      let result: Account[] | undefined;

      service.getAccounts('cust-001').subscribe(accounts => (result = accounts));

      const req = httpMock.expectOne(`${API_URL}/cust-001/accounts`);
      req.flush({ status: 'success', data: [] });

      expect(result).toEqual([]);
    });
  });

  // ─── create ──────────────────────────────────────────────────────────────

  describe('given a POST /api/v1/customers request with valid payload', () => {
    it('when the API responds with 201, then it should emit the created customer with ACTIVE status', () => {
      const request: CustomerRequest = {
        name: 'João Silva',
        cpf: '529.982.247-25',
        email: 'joao@email.com',
        birthDate: '1990-05-15',
      };
      let result: Customer | undefined;

      service.create(request).subscribe(c => (result = c));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ status: 'success', data: mockCustomer });

      expect(result?.status).toBe('ACTIVE');
      expect(result?.id).toBe('cust-001');
    });
  });

  // ─── update ──────────────────────────────────────────────────────────────

  describe('given a PUT /api/v1/customers/:id request with updated contact data', () => {
    it('when the API responds with 200, then it should emit the updated customer from response.data', () => {
      const request: CustomerRequest = {
        name: 'João Atualizado',
        cpf: '529.982.247-25',
        email: 'joao.novo@email.com',
        birthDate: '1990-05-15',
      };
      const updatedCustomer: Customer = { ...mockCustomer, name: 'João Atualizado', email: 'joao.novo@email.com' };
      let result: Customer | undefined;

      service.update('cust-001', request).subscribe(c => (result = c));

      const req = httpMock.expectOne(`${API_URL}/cust-001`);
      expect(req.request.method).toBe('PUT');
      req.flush({ status: 'success', data: updatedCustomer });

      expect(result?.name).toBe('João Atualizado');
      expect(result?.email).toBe('joao.novo@email.com');
    });
  });

  // ─── toggleStatus ────────────────────────────────────────────────────────

  describe('given a PATCH /api/v1/customers/:id/status request', () => {
    it('when the API responds with 204, then the observable should complete without error', (done) => {
      service.toggleStatus('cust-001').subscribe({
        complete: () => done(),
      });

      const req = httpMock.expectOne(`${API_URL}/cust-001/status`);
      expect(req.request.method).toBe('PATCH');
      req.flush(null, { status: 204, statusText: 'No Content' });
    });
  });
});
