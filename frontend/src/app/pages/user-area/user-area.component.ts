import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LoginResponse } from '../../core/models/models';

@Component({
  selector: 'app-user-area',
  templateUrl: './user-area.component.html',
  styleUrls: ['./user-area.component.scss'],
})
export class UserAreaComponent implements OnInit {
  currentUser: LoginResponse | null = null;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
  }

  goToMyOrders(): void { this.router.navigate(['/my-orders']); }
  goToCreateOrder(): void { this.router.navigate(['/create-order']); }
  goToUpdateUser(): void { this.router.navigate(['/update-user']); }
  logout(): void { this.authService.logout(); }
}
