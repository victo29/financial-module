export type ProductType = 'SAVINGS' | 'INVESTMENT' | 'CREDIT' | 'CHECKING';
export type ProductStatus = 'ACTIVE' | 'INACTIVE';

export interface Product {
  id: string;
  name: string;
  description?: string;
  type: ProductType;
  interestRate?: number;
  minBalance: number;
  status: ProductStatus;
  createdAt: string;
}

export interface ProductRequest {
  name: string;
  description?: string;
  type: ProductType;
  interestRate?: number;
  minBalance: number;
}
