import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  loading = false;

  private readonly PASSWORD_REGEX =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{6,}$/;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(3)]],
      cpf: [
        '',
        [Validators.required, Validators.pattern(/^\d{3}\.\d{3}\.\d{3}-\d{2}$|^\d{11}$/)],
      ],
      email: ['', [Validators.required, Validators.email]],
      password: [
        '',
        [Validators.required, Validators.pattern(this.PASSWORD_REGEX)],
      ],
      phone: ['', [Validators.required]],
      secondaryPhone: [''],
      cep: ['', [Validators.required]],
      address: ['', [Validators.required]],
      referencePoint: ['', [Validators.required]],
    });
  }

  get f() {
    return this.registerForm.controls;
  }

  isFieldInvalid(field: string): boolean {
    const ctrl = this.registerForm.get(field);
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  }

  getPasswordError(): string {
    const ctrl = this.f['password'];
    if (ctrl.errors?.['required']) return 'Senha é obrigatória';
    if (ctrl.errors?.['pattern']) {
      return 'Mínimo 6 caracteres, 1 maiúscula, 1 minúscula e 1 caractere especial';
    }
    return '';
  }

  getCpfError(): string {
    const ctrl = this.f['cpf'];
    if (ctrl.errors?.['required']) return 'CPF é obrigatório';
    if (ctrl.errors?.['pattern']) return 'CPF inválido. Use 000.000.000-00 ou 11 dígitos';
    return '';
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.messageService.add({
        severity: 'warn',
        summary: 'Formulário inválido',
        detail: 'Corrija os campos marcados em vermelho.',
      });
      return;
    }

    this.loading = true;
    const formValue = { ...this.registerForm.value };

    this.authService.register(formValue).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Cadastro realizado!',
          detail: 'Sua conta foi criada. Faça login para continuar.',
        });
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        const msg = err.error?.message || 'Erro ao realizar cadastro. Tente novamente.';
        this.messageService.add({
          severity: 'error',
          summary: 'Erro no cadastro',
          detail: msg,
        });
      },
      complete: () => (this.loading = false),
    });
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
