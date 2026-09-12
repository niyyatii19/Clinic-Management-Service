import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-access-denied',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './access-denied.component.html',
  styleUrl: './access-denied.component.scss'
})
export class AccessDeniedComponent {
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  goBack(): void {
    const user = this.authService.getCurrentUser();
    if (user) {
      const dashboardRoutes = {
        admin: '/admin/dashboard',
        doctor: '/doctor/dashboard',
        staff: '/staff/dashboard',
        patient: '/patient/dashboard'
      };
      this.router.navigate([dashboardRoutes[user.profile]]);
    } else {
      this.router.navigate(['/auth/login']);
    }
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: () => {
        this.router.navigate(['/auth/login']);
      }
    });
  }
}