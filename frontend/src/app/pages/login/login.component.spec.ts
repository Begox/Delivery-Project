import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { LoginComponent } from './login.component';
import { AuthService } from '../../core/services/auth.service';

// PrimeNG imports needed for the template
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login', 'isAuthenticated']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [LoginComponent],
      imports: [
        ReactiveFormsModule,
        RouterTestingModule,
        BrowserAnimationsModule,
        ButtonModule,
        InputTextModule,
        ToastModule,
        CardModule,
        DividerModule,
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        MessageService,
      ],
    }).compileComponents();

    authServiceSpy.isAuthenticated.and.returnValue(false);
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Validation', () => {
    it('should mark form as invalid when empty', () => {
      expect(component.loginForm.invalid).toBeTrue();
    });

    it('should mark form as valid when both fields are filled', () => {
      component.loginForm.setValue({ identifier: 'joao@email.com', password: 'Senha@123' });
      expect(component.loginForm.valid).toBeTrue();
    });

    it('should show error when identifier is empty', () => {
      component.loginForm.get('identifier')?.setValue('');
      component.loginForm.get('identifier')?.markAsTouched();
      expect(component.isFieldInvalid('identifier')).toBeTrue();
    });

    it('should show error when password is empty', () => {
      component.loginForm.get('password')?.setValue('');
      component.loginForm.get('password')?.markAsTouched();
      expect(component.isFieldInvalid('password')).toBeTrue();
    });
  });

  describe('onSubmit()', () => {
    it('should not call login when form is invalid', () => {
      component.onSubmit();
      expect(authServiceSpy.login).not.toHaveBeenCalled();
    });

    it('should call authService.login with valid credentials', () => {
      authServiceSpy.login.and.returnValue(
        of({ token: 'jwt123', userId: '1', fullName: 'João', email: 'joao@email.com', cpf: '111' })
      );

      component.loginForm.setValue({ identifier: 'joao@email.com', password: 'Senha@123' });
      component.onSubmit();

      expect(authServiceSpy.login).toHaveBeenCalledWith({
        identifier: 'joao@email.com',
        password: 'Senha@123',
      });
    });

    it('should set loading to false on error', () => {
      const error = new HttpErrorResponse({ status: 401, error: { message: 'Credenciais inválidas' } });
      authServiceSpy.login.and.returnValue(throwError(() => error));

      component.loginForm.setValue({ identifier: 'joao@email.com', password: 'errada' });
      component.onSubmit();

      expect(component.loading).toBeFalse();
    });
  });

  describe('Redirect behavior', () => {
    it('should redirect to user-area if already authenticated', () => {
      authServiceSpy.isAuthenticated.and.returnValue(true);
      component.ngOnInit();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/user-area']);
    });
  });
});
