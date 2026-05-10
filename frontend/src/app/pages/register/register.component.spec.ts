import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../core/services/auth.service';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  const validFormData = {
    fullName: 'João Silva',
    cpf: '123.456.789-09',
    email: 'joao@email.com',
    password: 'Senha@123',
    phone: '11999999999',
    secondaryPhone: '',
    cep: '01310-100',
    address: 'Av. Paulista, 1000, São Paulo - SP',
    referencePoint: 'Próximo ao MASP',
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['register']);

    await TestBed.configureTestingModule({
      declarations: [RegisterComponent],
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
        MessageService,
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  describe('Form Validation — Required Fields', () => {
    it('should be invalid when empty', () => {
      expect(component.registerForm.invalid).toBeTrue();
    });

    it('should be valid with all required fields filled correctly', () => {
      component.registerForm.setValue(validFormData);
      expect(component.registerForm.valid).toBeTrue();
    });

    it('should validate fullName is required', () => {
      component.registerForm.get('fullName')?.setValue('');
      expect(component.registerForm.get('fullName')?.invalid).toBeTrue();
    });

    it('should validate email format', () => {
      component.registerForm.get('email')?.setValue('not-an-email');
      expect(component.registerForm.get('email')?.invalid).toBeTrue();
    });

    it('should validate CPF format', () => {
      component.registerForm.get('cpf')?.setValue('111');
      expect(component.registerForm.get('cpf')?.invalid).toBeTrue();
    });
  });

  describe('Password Policy Validation', () => {
    it('should reject password with fewer than 6 characters', () => {
      component.registerForm.get('password')?.setValue('Ab@1');
      expect(component.registerForm.get('password')?.invalid).toBeTrue();
    });

    it('should reject password without uppercase letter', () => {
      component.registerForm.get('password')?.setValue('senha@123');
      expect(component.registerForm.get('password')?.invalid).toBeTrue();
    });

    it('should reject password without special character', () => {
      component.registerForm.get('password')?.setValue('Senha123');
      expect(component.registerForm.get('password')?.invalid).toBeTrue();
    });

    it('should reject password without lowercase letter', () => {
      component.registerForm.get('password')?.setValue('SENHA@123');
      expect(component.registerForm.get('password')?.invalid).toBeTrue();
    });

    it('should accept valid password with all policy requirements', () => {
      component.registerForm.get('password')?.setValue('Senha@123');
      expect(component.registerForm.get('password')?.valid).toBeTrue();
    });
  });

  describe('onSubmit()', () => {
    it('should not call register when form is invalid', () => {
      component.onSubmit();
      expect(authServiceSpy.register).not.toHaveBeenCalled();
    });

    it('should call authService.register with valid form data', () => {
      authServiceSpy.register.and.returnValue(of({} as any));
      component.registerForm.setValue(validFormData);
      component.onSubmit();
      expect(authServiceSpy.register).toHaveBeenCalled();
    });

    it('should set loading to false on error', () => {
      authServiceSpy.register.and.returnValue(
        throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'CPF já cadastrado' } }))
      );
      component.registerForm.setValue(validFormData);
      component.onSubmit();
      expect(component.loading).toBeFalse();
    });
  });
});
