import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  showPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    // Redirect if already authenticated
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/user-area']);
      return;
    }

    this.loginForm = this.fb.group({
      identifier: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  isFieldInvalid(field: string): boolean {
    const ctrl = this.loginForm.get(field);
    return !!(ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched));
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.messageService.add({
          severity: 'success',
          summary: 'Bem-vindo!',
          detail: 'Login realizado com sucesso.',
        });
        setTimeout(() => this.router.navigate(['/user-area']), 500);
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        const msg =
          err.error?.message || 'Credenciais inválidas. Verifique e tente novamente.';
        this.messageService.add({
          severity: 'error',
          summary: 'Erro ao entrar',
          detail: msg,
        });
      },
      complete: () => (this.loading = false),
    });
  }

  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  forgotPassword(): void {
    this.messageService.add({
      severity: 'info',
      summary: 'Em desenvolvimento',
      detail: 'A funcionalidade de recuperação de senha estará disponível em breve.',
    });
  }
}
