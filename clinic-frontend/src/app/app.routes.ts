import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { GuestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  // Default redirect
  { path: '', redirectTo: '/auth/login', pathMatch: 'full' },
  
  // Authentication routes (for guests only)
  {
    path: 'auth',
    canActivate: [GuestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () => import('./modules/auth/login.component').then(c => c.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./modules/auth/register.component').then(c => c.RegisterComponent)
      }
    ]
  },
  
  // Admin routes
  {
    path: 'admin',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['admin'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/admin/admin-dashboard.component').then(c => c.AdminDashboardComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  
  // Doctor routes
  {
    path: 'doctor',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['doctor'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/doctor/doctor-dashboard.component').then(c => c.DoctorDashboardComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  
  // Staff routes
  {
    path: 'staff',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['staff'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/staff/staff-dashboard.component').then(c => c.StaffDashboardComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  
  // Patient routes
  {
    path: 'patient',
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['patient'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./modules/patient/patient-dashboard.component').then(c => c.PatientDashboardComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  
  // Access denied page
  {
    path: 'access-denied',
    loadComponent: () => import('./shared/components/access-denied.component').then(c => c.AccessDeniedComponent)
  },
  
  // Wildcard route - must be last
  { path: '**', redirectTo: '/auth/login' }
];
