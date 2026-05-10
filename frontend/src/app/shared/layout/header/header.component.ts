import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  items: MenuItem[] = [];
  private authSub!: Subscription;

  constructor(public authService: AuthService, public router: Router) {}

  ngOnInit(): void {
    this.buildMenu(this.authService.isAuthenticated());

    this.authSub = this.authService.isAuthenticated$.subscribe((auth) => {
      this.buildMenu(auth);
    });
  }

  ngOnDestroy(): void {
    this.authSub?.unsubscribe();
  }

  private buildMenu(isAuthenticated: boolean): void {
    if (isAuthenticated) {
      const user = this.authService.getCurrentUser();
      this.items = [
        {
          label: 'Realizar Pedido',
          icon: 'pi pi-shopping-cart',
          command: () => this.router.navigate(['/create-order']),
        },
        {
          label: 'Meus Pedidos',
          icon: 'pi pi-list',
          command: () => this.router.navigate(['/my-orders']),
        },
        {
          label: 'Área do Usuário',
          icon: 'pi pi-user',
          command: () => this.router.navigate(['/user-area']),
        },
        {
          label: 'Sair',
          icon: 'pi pi-sign-out',
          command: () => this.logout(),
          style: { 'margin-left': 'auto' },
        },
      ];
    } else {
      this.items = [
        {
          label: 'Login',
          icon: 'pi pi-sign-in',
          command: () => this.router.navigate(['/login']),
        },
        {
          label: 'Cadastre-se',
          icon: 'pi pi-user-plus',
          command: () => this.router.navigate(['/register']),
        },
      ];
    }
  }

  private logout(): void {
    this.authService.logout();
  }
}
