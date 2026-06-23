import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { Product, ProductRequest } from '../models/product.model';

describe('ProductService', () => {
  let service: ProductService;
  let httpMock: HttpTestingController;

  const API_URL = '/api/v1/products';

  const mockProduct: Product = {
    id: 'abc-123',
    name: 'Poupança Premium',
    type: 'SAVINGS',
    interestRate: 0.5,
    minBalance: 0,
    status: 'ACTIVE',
    createdAt: '2024-01-01T00:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductService],
    });
    service = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ─── getAll ──────────────────────────────────────────────────────────────

  describe('given a GET /api/v1/products request', () => {
    it('when the API returns successfully, then it should emit the product array from response.data', () => {
      let result: Product[] | undefined;

      service.getAll().subscribe(products => (result = products));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: [mockProduct] });

      expect(result).toEqual([mockProduct]);
    });
  });

  // ─── getById ─────────────────────────────────────────────────────────────

  describe('given a GET /api/v1/products/:id request for an existing product', () => {
    it('when the API returns successfully, then it should emit the single product from response.data', () => {
      let result: Product | undefined;

      service.getById('abc-123').subscribe(p => (result = p));

      const req = httpMock.expectOne(`${API_URL}/abc-123`);
      expect(req.request.method).toBe('GET');
      req.flush({ status: 'success', data: mockProduct });

      expect(result).toEqual(mockProduct);
    });
  });

  // ─── create ──────────────────────────────────────────────────────────────

  describe('given a POST /api/v1/products request with valid payload', () => {
    it('when the API responds with 201, then it should emit the created product from response.data', () => {
      const request: ProductRequest = {
        name: 'Poupança Premium',
        type: 'SAVINGS',
        interestRate: 0.5,
        minBalance: 0,
      };
      let result: Product | undefined;

      service.create(request).subscribe(p => (result = p));

      const req = httpMock.expectOne(API_URL);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush({ status: 'success', data: mockProduct });

      expect(result).toEqual(mockProduct);
      expect(result?.status).toBe('ACTIVE');
    });
  });

  // ─── update ──────────────────────────────────────────────────────────────

  describe('given a PUT /api/v1/products/:id request with updated payload', () => {
    it('when the API responds with 200, then it should emit the updated product from response.data', () => {
      const request: ProductRequest = {
        name: 'Poupança Atualizada',
        type: 'SAVINGS',
        minBalance: 100,
      };
      const updatedProduct: Product = { ...mockProduct, name: 'Poupança Atualizada', minBalance: 100 };
      let result: Product | undefined;

      service.update('abc-123', request).subscribe(p => (result = p));

      const req = httpMock.expectOne(`${API_URL}/abc-123`);
      expect(req.request.method).toBe('PUT');
      req.flush({ status: 'success', data: updatedProduct });

      expect(result?.name).toBe('Poupança Atualizada');
      expect(result?.minBalance).toBe(100);
    });
  });

  // ─── toggleStatus ────────────────────────────────────────────────────────

  describe('given a PATCH /api/v1/products/:id/status request', () => {
    it('when the API responds with 204, then the observable should complete without emitting a value', (done) => {
      service.toggleStatus('abc-123').subscribe({
        complete: () => done(),
      });

      const req = httpMock.expectOne(`${API_URL}/abc-123/status`);
      expect(req.request.method).toBe('PATCH');
      req.flush(null, { status: 204, statusText: 'No Content' });
    });
  });

  // ─── delete ──────────────────────────────────────────────────────────────

  describe('given a DELETE /api/v1/products/:id request', () => {
    it('when the API responds with 204, then the observable should complete without error', (done) => {
      service.delete('abc-123').subscribe({
        complete: () => done(),
      });

      const req = httpMock.expectOne(`${API_URL}/abc-123`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null, { status: 204, statusText: 'No Content' });
    });
  });
});
