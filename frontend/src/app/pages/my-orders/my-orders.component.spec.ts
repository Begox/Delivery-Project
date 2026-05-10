import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { MyOrdersComponent } from './my-orders.component';
import { OrderService } from '../../core/services/order.service';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { TagModule } from 'primeng/tag';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { OrderResponse, PageResponse } from '../../core/models/models';

describe('MyOrdersComponent', () => {
  let component: MyOrdersComponent;
  let fixture: ComponentFixture<MyOrdersComponent>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;

  const mockOrders: OrderResponse[] = [
    {
      id: '1',
      userId: 'u1',
      pickupAddress: 'Av. Paulista, 1000, São Paulo - SP',
      deliveryAddress: 'Rua Augusta, 500, São Paulo - SP',
      itemDescription: '2x Pizza',
      distanceKm: 5.2,
      estimatedTimeMinutes: 20,
      estimatedValue: 18.0,
      createdAt: '2026-05-09T12:00:00',
      status: 'PENDING',
    },
  ];

  const mockPage: PageResponse<OrderResponse> = {
    content: mockOrders,
    totalElements: 1,
    totalPages: 1,
    size: 10,
    number: 0,
    first: true,
    last: true,
  };

  beforeEach(async () => {
    orderServiceSpy = jasmine.createSpyObj('OrderService', ['getMyOrders']);
    orderServiceSpy.getMyOrders.and.returnValue(of(mockPage));

    await TestBed.configureTestingModule({
      declarations: [MyOrdersComponent],
      imports: [
        RouterTestingModule,
        BrowserAnimationsModule,
        ButtonModule,
        ToastModule,
        CardModule,
        TableModule,
        PaginatorModule,
        TagModule,
        ProgressSpinnerModule,
      ],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy },
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MyOrdersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Orders listing', () => {
    it('should call getMyOrders on init', () => {
      expect(orderServiceSpy.getMyOrders).toHaveBeenCalledWith(0, 10);
    });

    it('should render orders after loading', () => {
      expect(component.orders.length).toBe(1);
      expect(component.orders[0].id).toBe('1');
    });

    it('should set totalRecords correctly', () => {
      expect(component.totalRecords).toBe(1);
    });

    it('should set loading to false after load', () => {
      expect(component.loading).toBeFalse();
    });
  });

  describe('Status mapping', () => {
    it('should return warning severity for PENDING', () => {
      expect(component.getStatusSeverity('PENDING')).toBe('warning');
    });

    it('should return success severity for DELIVERED', () => {
      expect(component.getStatusSeverity('DELIVERED')).toBe('success');
    });

    it('should return correct Portuguese label for IN_PROGRESS', () => {
      expect(component.getStatusLabel('IN_PROGRESS')).toBe('Em Andamento');
    });

    it('should return Cancelado for CANCELED', () => {
      expect(component.getStatusLabel('CANCELED')).toBe('Cancelado');
    });
  });

  describe('Error handling', () => {
    it('should set loading to false on error', () => {
      orderServiceSpy.getMyOrders.and.returnValue(throwError(() => new Error('Server error')));
      component.loadOrders(0, 10);
      expect(component.loading).toBeFalse();
    });
  });
});
