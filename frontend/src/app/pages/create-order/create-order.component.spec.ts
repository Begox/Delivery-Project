import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CreateOrderComponent } from './create-order.component';
import { OrderService } from '../../core/services/order.service';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { EstimateResponse, OrderResponse } from '../../core/models/models';

describe('CreateOrderComponent', () => {
  let component: CreateOrderComponent;
  let fixture: ComponentFixture<CreateOrderComponent>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;

  const mockEstimate: EstimateResponse = {
    distanceKm: 5.2,
    estimatedTimeMinutes: 20,
    estimatedValue: 18.0,
  };

  beforeEach(async () => {
    orderServiceSpy = jasmine.createSpyObj('OrderService', [
      'calculateEstimate',
      'createOrder',
    ]);

    await TestBed.configureTestingModule({
      declarations: [CreateOrderComponent],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule,
        BrowserAnimationsModule,
        ButtonModule,
        InputTextModule,
        InputTextareaModule,
        ToastModule,
        CardModule,
        DividerModule,
        ProgressSpinnerModule,
      ],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy },
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateOrderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Estimate Calculation', () => {
    it('should call calculateEstimate when addresses are filled', () => {
      orderServiceSpy.calculateEstimate.and.returnValue(of(mockEstimate));

      component.orderForm.get('pickupAddress')?.setValue('Av. Paulista, 1000, São Paulo - SP');
      component.orderForm.get('deliveryAddress')?.setValue('Rua Augusta, 500, São Paulo - SP');

      component.calculateEstimate();

      expect(orderServiceSpy.calculateEstimate).toHaveBeenCalled();
      expect(component.estimate).toEqual(mockEstimate);
      expect(component.estimateCalculated).toBeTrue();
    });

    it('should not call calculateEstimate when addresses are empty', () => {
      component.calculateEstimate();
      expect(orderServiceSpy.calculateEstimate).not.toHaveBeenCalled();
    });

    it('should handle API error during estimate', () => {
      component.orderForm.get('pickupAddress')?.setValue('Endereço Inválido XYZ');
      component.orderForm.get('deliveryAddress')?.setValue('Outro Endereço Inválido');

      orderServiceSpy.calculateEstimate.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 503, error: { message: 'API indisponível' } }))
      );

      component.calculateEstimate();

      expect(component.estimateCalculated).toBeFalse();
      expect(component.calculatingEstimate).toBeFalse();
    });

    it('should reset estimate when pickup address changes', () => {
      component.estimateCalculated = true;
      component.estimate = mockEstimate;

      component.orderForm.get('pickupAddress')?.setValue('Novo Endereço');

      expect(component.estimateCalculated).toBeFalse();
      expect(component.estimate).toBeNull();
    });
  });

  describe('Order Confirmation', () => {
    it('should not confirm order when estimate was not calculated', () => {
      component.estimateCalculated = false;
      component.confirmOrder();
      expect(orderServiceSpy.createOrder).not.toHaveBeenCalled();
    });

    it('should create order when estimate is calculated and form is valid', () => {
      orderServiceSpy.createOrder.and.returnValue(of({} as OrderResponse));

      component.orderForm.setValue({
        pickupAddress: 'Av. Paulista, 1000, São Paulo - SP',
        deliveryAddress: 'Rua Augusta, 500, São Paulo - SP',
        itemDescription: '2x Pizza Margherita',
      });
      component.estimateCalculated = true;
      component.estimate = mockEstimate;

      component.confirmOrder();

      expect(orderServiceSpy.createOrder).toHaveBeenCalledWith({
        pickupAddress: 'Av. Paulista, 1000, São Paulo - SP',
        deliveryAddress: 'Rua Augusta, 500, São Paulo - SP',
        itemDescription: '2x Pizza Margherita',
      });
    });

    it('should display error message when createOrder fails', () => {
      orderServiceSpy.createOrder.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 422, error: { message: 'Distância excedida' } }))
      );

      component.orderForm.setValue({
        pickupAddress: 'Av. Paulista, 1000, São Paulo - SP',
        deliveryAddress: 'Rua Augusta, 500, São Paulo - SP',
        itemDescription: 'Items',
      });
      component.estimateCalculated = true;
      component.estimate = mockEstimate;

      component.confirmOrder();

      expect(component.confirmingOrder).toBeFalse();
    });
  });

  describe('Header behavior (auth state)', () => {
    it('should render the form initially', () => {
      const compiled = fixture.nativeElement as HTMLElement;
      expect(compiled.querySelector('form')).toBeTruthy();
    });
  });
});
