// ==========================================
// DELIVERY PGE — TypeScript Models
// ==========================================

export interface LoginRequest {
  identifier: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  fullName: string;
  email: string;
  cpf: string;
}

export interface RegisterRequest {
  fullName: string;
  cpf: string;
  email: string;
  password: string;
  phone: string;
  secondaryPhone?: string;
  cep: string;
  address: string;
  referencePoint: string;
}

export interface UserResponse {
  id: string;
  fullName: string;
  cpf: string;
  email: string;
  phone: string;
  secondaryPhone?: string;
  cep: string;
  address: string;
  referencePoint: string;
  createdAt: string;
}

export interface UpdateUserRequest {
  phone?: string;
  secondaryPhone?: string;
  cep?: string;
  address?: string;
  referencePoint?: string;
}

export interface OrderRequest {
  pickupAddress: string;
  deliveryAddress: string;
  itemDescription: string;
}

export interface OrderResponse {
  id: string;
  userId: string;
  pickupAddress: string;
  deliveryAddress: string;
  itemDescription: string;
  distanceKm: number;
  estimatedTimeMinutes: number;
  estimatedValue: number;
  createdAt: string;
  status: OrderStatus;
}

export type OrderStatus = 'PENDING' | 'IN_PROGRESS' | 'DELIVERED' | 'CANCELED';

export interface EstimateRequest {
  pickupAddress: string;
  deliveryAddress: string;
}

export interface EstimateResponse {
  distanceKm: number;
  estimatedTimeMinutes: number;
  estimatedValue: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  message: string;
}
