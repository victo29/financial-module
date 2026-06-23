import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    snackBarSpy = jasmine.createSpyObj<MatSnackBar>('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule],
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: snackBarSpy },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  // ─── SUCCESS ────────────────────────────────────────────────────────────

  describe('given a successful HTTP request', () => {
    it('when the server returns 200, then the response should pass through without calling snackBar.open', () => {
      let responseBody: any;

      httpClient.get('/api/test').subscribe(res => (responseBody = res));

      const req = httpMock.expectOne('/api/test');
      req.flush({ data: 'ok' });

      expect(responseBody).toEqual({ data: 'ok' });
      expect(snackBarSpy.open).not.toHaveBeenCalled();
    });
  });

  // ─── 404 WITH MESSAGE ────────────────────────────────────────────────────

  describe('given an HTTP request that results in a 404 response with an error message', () => {
    it('when the error is received, then snackBar.open should be called with the server error message', () => {
      let errorCaught = false;

      httpClient.get('/api/missing').subscribe({
        error: () => (errorCaught = true),
      });

      const req = httpMock.expectOne('/api/missing');
      req.flush({ message: 'Recurso não encontrado' }, { status: 404, statusText: 'Not Found' });

      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'Recurso não encontrado',
        'Fechar',
        jasmine.objectContaining({ duration: 5000 })
      );
      expect(errorCaught).toBeTrue();
    });
  });

  // ─── 500 WITHOUT MESSAGE ─────────────────────────────────────────────────

  describe('given an HTTP request that results in a 500 response without a message field', () => {
    it('when the error is received, then snackBar.open should be called with the default fallback message', () => {
      httpClient.get('/api/broken').subscribe({ error: () => {} });

      const req = httpMock.expectOne('/api/broken');
      req.flush({}, { status: 500, statusText: 'Internal Server Error' });

      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'Erro ao processar a requisição',
        'Fechar',
        jasmine.objectContaining({ duration: 5000 })
      );
    });
  });

  // ─── 422 BUSINESS RULE VIOLATION ─────────────────────────────────────────

  describe('given an HTTP request that results in a 422 response with a business rule message', () => {
    it('when the error is received, then snackBar.open should display the specific business error message', () => {
      httpClient.post('/api/v1/transactions', {}).subscribe({ error: () => {} });

      const req = httpMock.expectOne('/api/v1/transactions');
      req.flush(
        { code: 'INSUFFICIENT_BALANCE', message: 'Saldo insuficiente' },
        { status: 422, statusText: 'Unprocessable Entity' }
      );

      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'Saldo insuficiente',
        'Fechar',
        jasmine.objectContaining({ panelClass: ['error-snackbar'] })
      );
    });
  });

  // ─── 409 CONFLICT ────────────────────────────────────────────────────────

  describe('given an HTTP request that results in a 409 response for a duplicate resource', () => {
    it('when the error is received, then snackBar.open should display the conflict message and the error should propagate', () => {
      let caughtError: any;

      httpClient.post('/api/v1/customers', {}).subscribe({ error: err => (caughtError = err) });

      const req = httpMock.expectOne('/api/v1/customers');
      req.flush(
        { code: 'CPF_ALREADY_EXISTS', message: 'CPF já cadastrado' },
        { status: 409, statusText: 'Conflict' }
      );

      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'CPF já cadastrado',
        'Fechar',
        jasmine.anything()
      );
      expect(caughtError).toBeTruthy();
    });
  });
});
