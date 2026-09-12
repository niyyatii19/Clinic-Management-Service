import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ProfilePermissions } from '../../shared/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class PermissionGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    const requiredPermission = route.data['permission'] as keyof ProfilePermissions;
    
    return this.authService.currentUser$.pipe(
      map(user => {
        if (!user) {
          return this.router.createUrlTree(['/auth/login']);
        }

        if (!requiredPermission) {
          return true;
        }

        if (this.authService.hasPermission(requiredPermission)) {
          return true;
        }
        
        return this.router.createUrlTree(['/access-denied']);
      })
    );
  }
}