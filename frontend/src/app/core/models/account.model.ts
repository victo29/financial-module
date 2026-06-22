export type AccountStatus = 'ACTIVE' | 'BLOCKED' | 'CLOSED';

export interface Account {
  id: string;
  number: string;
  customerId: string;
  customerName: string;
  productId: string;
  productName: string;
  balance: number;
  status: AccountStatus;
  openedAt: string;
}

export interface AccountRequest {
  customerId: string;
  productId: string;
}
