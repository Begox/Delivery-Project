import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { OrderService } from '../../core/services/order.service';
import { EstimateResponse } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-create-order',
  templateUrl: './create-order.component.html',
  styleUrls: ['./create-order.component.scss'],
})
export class CreateOrderComponent implements OnInit {
  orderForm!: FormGroup;
  estimate: EstimateResponse | null = null;
  calculatingEstimate = false;
  confirmingOrder = false;
  estimateCalculated = false;

  constructor(
    private fb: FormBuilder,
    private orderService: OrderService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.orderForm = this.fb.group({
      pickupAddress: ['', [Validators.required, Validators.minLength(10)]],
      deliveryAddress: ['', [Validators.required, Validators.minLength(10)]],
      itemDescription: ['', [Validators.required]],
    });

    // Reset estimate when addresses change
    this.orderForm.get('pickupAddress')?.valueChanges.subscribe(() => {
      this.estimateCalculated = false;
      this.estimate = null;
    });
    this.orderForm.get('deliveryAddress')?.valueChanges.subscribe(() => {
      this.estimateCalculated = false;
      this.estimate = null;
    });
  }

  get f() { return this.orderForm.controls; }

  isFieldInvalid(field: string): boolean {
    const ctrl = this.orderForm.get(field);
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  }

  calculateEstimate(): void {
    const pickup = this.f['pickupAddress'].value?.trim();
    const delivery = this.f['deliveryAddress'].value?.trim();

    if (!pickup || !delivery) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Atenção',
        detail: 'Preencha os endereços de coleta e entrega para calcular.',
      });
      return;
    }

    this.calculatingEstimate = true;
    this.estimate = null;
    this.estimateCalculated = false;

    this.orderService
      .calculateEstimate({ pickupAddress: pickup, deliveryAddress: delivery })
      .subscribe({
        next: (response: EstimateResponse) => {
          this.estimate = response;
          this.estimateCalculated = true;
          this.calculatingEstimate = false;
          this.messageService.add({
            severity: 'success',
            summary: 'Estimativa calculada!',
            detail: `Distância: ${response.distanceKm} km — Valor: ${this.formatCurrency(response.estimatedValue)}`,
          });
        },
        error: (err: HttpErrorResponse) => {
          this.calculatingEstimate = false;
          const msg =
            err.error?.message ||
            'Não foi possível calcular a estimativa. Verifique os endereços.';
          this.messageService.add({
            severity: 'error',
            summary: 'Erro no cálculo',
            detail: msg,
          });
        },
      });
  }

  confirmOrder(): void {
    if (this.orderForm.invalid) {
      this.orderForm.markAllAsTouched();
      return;
    }
    if (!this.estimateCalculated) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Calcule primeiro',
        detail: 'Calcule a estimativa de entrega antes de confirmar o pedido.',
      });
      return;
    }

    this.confirmingOrder = true;

    this.orderService.createOrder(this.orderForm.value).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Pedido criado!',
          detail: 'Sua entrega foi registrada com sucesso.',
        });
        setTimeout(() => this.router.navigate(['/my-orders']), 1500);
      },
      error: (err: HttpErrorResponse) => {
        this.confirmingOrder = false;
        const msg = err.error?.message || 'Erro ao criar pedido. Tente novamente.';
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: msg,
        });
      },
      complete: () => (this.confirmingOrder = false),
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(value);
  }

  goBack(): void {
    this.router.navigate(['/user-area']);
  }
}
