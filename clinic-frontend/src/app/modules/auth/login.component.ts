import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NgbAlert } from '@ng-bootstrap/ng-bootstrap';

import { AuthService } from '../../services/auth.service';
import { AuthRequest, AuthResponse, ResponseStatus, User } from '../../shared/models/user.model';
import { WelcomeService } from '../../shared/services/welcome.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgbAlert],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm: FormGroup;
  isLoading = false;
  showPassword = false;
  errorMessage = '';
  successMessage = '';
  
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private welcomeService: WelcomeService
  ) {
    this.loginForm = this.createLoginForm();
  }

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.redirectToUserDashboard();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private createLoginForm(): FormGroup {
    return this.fb.group({
      emailOrUsername: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const authRequest: AuthRequest = {
        loginInput: this.loginForm.get('emailOrUsername')?.value.trim(),
        password: this.loginForm.get('password')?.value
      };

      this.authService.login(authRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: AuthResponse) => {
            this.isLoading = false;
            if (response.status === ResponseStatus.SUCCESS) {
              this.successMessage = response.message || 'Login successful!';
              
              setTimeout(() => {
                const user = this.authService.getCurrentUser();
                if (user) {
                  this.showWelcomeMessage(user);
                }
                this.redirectToUserDashboard();
              }, 1500);
            } else {
              this.errorMessage = response.message || 'Login failed. Please try again.';
            }
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = error || 'An error occurred during login. Please try again.';
          }
        });
    } else {
      this.markFormGroupTouched();
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  goToRegister(): void {
    this.router.navigate(['/auth/register']);
  }

  goToForgotPassword(): void {
    this.router.navigate(['/auth/forgot-password']);
  }

  getFieldError(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${this.getFieldLabel(fieldName)} is required`;
      }
      if (field.errors['minlength']) {
        return `${this.getFieldLabel(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      }
    }
    return '';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      emailOrUsername: 'Email or Username',
      password: 'Password'
    };
    return labels[fieldName] || fieldName;
  }

  hasFieldError(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field?.errors && field.touched);
  }

  private markFormGroupTouched(): void {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  private showWelcomeMessage(user: User): void {
    this.welcomeService.showWelcomePopup(user);
  }

  private redirectToUserDashboard(): void {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    const dashboardRoute = this.welcomeService.getDashboardRoute(user);
    if (!dashboardRoute) return;
    this.router.navigateByUrl(dashboardRoute);
  }

  closeAlert(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  getCurrentYear(): number {
    return new Date().getFullYear();
  }
}