import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { UserService } from '../../core/services/user.service';
import { UserResponse } from '../../core/models/models';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-update-user',
  templateUrl: './update-user.component.html',
  styleUrls: ['./update-user.component.scss'],
})
export class UpdateUserComponent implements OnInit {
  updateForm!: FormGroup;
  loading = false;
  loadingProfile = true;
  currentProfile: UserResponse | null = null;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.updateForm = this.fb.group({
      phone: [''],
      secondaryPhone: [''],
      cep: [''],
      address: [''],
      referencePoint: [''],
    });

    this.loadProfile();
  }

  private loadProfile(): void {
    this.userService.getMyProfile().subscribe({
      next: (profile: UserResponse) => {
        this.currentProfile = profile;
        this.updateForm.patchValue({
          phone: profile.phone,
          secondaryPhone: profile.secondaryPhone || '',
          cep: profile.cep,
          address: profile.address,
          referencePoint: profile.referencePoint,
        });
        this.loadingProfile = false;
      },
      error: () => {
        this.loadingProfile = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: 'Não foi possível carregar seus dados.',
        });
      },
    });
  }

  onSubmit(): void {
    this.loading = true;
    this.userService.updateMyProfile(this.updateForm.value).subscribe({
      next: (updated: UserResponse) => {
        this.currentProfile = updated;
        this.loading = false;
        this.messageService.add({
          severity: 'success',
          summary: 'Dados atualizados!',
          detail: 'Suas informações foram salvas com sucesso.',
        });
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.messageService.add({
          severity: 'error',
          summary: 'Erro',
          detail: err.error?.message || 'Erro ao atualizar dados.',
        });
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/user-area']);
  }
}
