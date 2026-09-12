import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../shared/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const requiredRoles = route.data['roles'] as UserProfile[];
    
    return this.authService.currentUser$.pipe(
      map(user => {
        if (!user) {
          return this.router.createUrlTree(['/auth/login']);
        }

        if (!requiredRoles || requiredRoles.length === 0) {
          return true;
        }

        if (requiredRoles.includes(user.profile)) {
          return true;
        }

        return this.router.createUrlTree([this.getDefaultDashboardRoute(user.profile)]);
      })
    );
  }

  private getDefaultDashboardRoute(profile: UserProfile): string {
    const dashboardRoutes = {
      admin: '/admin/dashboard',
      doctor: '/doctor/dashboard',
      staff: '/staff/dashboard',
      patient: '/patient/dashboard'
    };

    return dashboardRoutes[profile] || '/';
  }
}