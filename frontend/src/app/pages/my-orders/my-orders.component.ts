import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { OrderService } from '../../core/services/order.service';
import { OrderResponse, PageResponse } from '../../core/models/models';

@Component({
  selector: 'app-my-orders',
  templateUrl: './my-orders.component.html',
  styleUrls: ['./my-orders.component.scss'],
})
export class MyOrdersComponent implements OnInit {
  orders: OrderResponse[] = [];
  loading = true;
  totalRecords = 0;
  rows = 10;
  first = 0;

  constructor(
    private orderService: OrderService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.loadOrders(0, this.rows);
  }

  loadOrders(page: number, size: number): void {
    this.loading = true;
    this.orderService.getMyOrders(page, size).subscribe({
      next: (response: PageResponse<OrderResponse>) => {
        this.orders = response.content;
        this.totalRecords = response.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Não foi possível carregar os pedidos.',
        });
      },
    });
  }

  onPageChange(event: any): void {
    this.first = event.first;
    const page = event.first / event.rows;
    this.loadOrders(page, event.rows);
  }

  getStatusSeverity(status: string): "success" | "secondary" | "info" | "warning" | "danger" | "contrast" | undefined {
    const map: Record<string, "success" | "secondary" | "info" | "warning" | "danger" | "contrast" | undefined> = {
      PENDING: 'warning',
      IN_PROGRESS: 'info',
      DELIVERED: 'success',
      CANCELED: 'danger',
    };
    return map[status] || 'info';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'Pendente',
      IN_PROGRESS: 'Em Andamento',
      DELIVERED: 'Entregue',
      CANCELED: 'Cancelado',
    };
    return map[status] || status;
  }

  goBack(): void {
    this.router.navigate(['/user-area']);
  }

  goToCreateOrder(): void {
    this.router.navigate(['/create-order']);
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  }
}
