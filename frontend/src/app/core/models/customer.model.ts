export type CustomerStatus = 'ACTIVE' | 'INACTIVE';

export interface Customer {
  id: string;
  name: string;
  cpf: string;
  email: string;
  phone?: string;
  birthDate: string;
  status: CustomerStatus;
  createdAt: string;
}

export interface CustomerRequest {
  name: string;
  cpf: string;
  email: string;
  phone?: string;
  birthDate: string;
}
