export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';

export interface Transaction {
  id: string;
  accountId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  balanceBefore: number;
  balanceAfter: number;
  status: string;
  createdAt: string;
}

export interface TransactionRequest {
  accountId: string;
  type: TransactionType;
  amount: number;
  description?: string;
  destinationAccountId?: string;
}
