import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  EstimateRequest,
  EstimateResponse,
  OrderRequest,
  OrderResponse,
  PageResponse,
} from '../models/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private readonly API_URL = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  calculateEstimate(request: EstimateRequest): Observable<EstimateResponse> {
    return this.http.post<EstimateResponse>(
      `${this.API_URL}/calculate-estimate`,
      request
    );
  }

  createOrder(request: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.API_URL, request);
  }

  getMyOrders(
    page: number = 0,
    size: number = 10
  ): Observable<PageResponse<OrderResponse>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PageResponse<OrderResponse>>(
      `${this.API_URL}/my-orders`,
      { params }
    );
  }

  getOrdersByUserId(userId: string): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.API_URL}/user/${userId}`);
  }
}
