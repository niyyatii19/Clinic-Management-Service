import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class GuestGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.authService.isAuthenticated$.pipe(
      map(isAuthenticated => {
        if (!isAuthenticated) {
          return true;
        } else {
          const user = this.authService.getCurrentUser();
          if (user) {
            const dashboardRoutes = {
              admin: '/admin/dashboard',
              doctor: '/doctor/dashboard',
              staff: '/staff/dashboard',
              patient: '/patient/dashboard'
            };
            return this.router.createUrlTree([dashboardRoutes[user.profile]]);
          }
          return this.router.createUrlTree(['/']);
        }
      })
    );
  }
}