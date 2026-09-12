import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { 
  User, 
  AuthRequest, 
  AuthResponse, 
  RegistrationRequest, 
  ResponseStatus,
  UserSession,
  TokenInfo,
  ApiResponse,
  UserProfile,
  ProfilePermissions,
  PROFILE_ACCESS
} from '../shared/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_BASE_URL = 'http://localhost:8089';
  private readonly TOKEN_KEY = 'clinic_auth_token';
  private readonly USER_KEY = 'clinic_user';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  
  public currentUser$ = this.currentUserSubject.asObservable();
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.loadUserFromStorage();

    // Expose a quick-reset helper in development for convenience
    try {
      (window as any).resetAuth = () => this.resetAuth();
    } catch (e) {
      // ignore in environments where window isn't available
    }
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const userJson = localStorage.getItem(this.USER_KEY);
    
    if (token && userJson) {
      try {
        const user: User = JSON.parse(userJson);
        if (this.isTokenExpired(token)) {
          console.log('Token expired, attempting refresh...');
          this.refreshToken().subscribe({
            next: (newToken) => {
              console.log('Token refreshed successfully');
              this.currentUserSubject.next(user);
              this.isAuthenticatedSubject.next(true);
            },
            error: (error) => {
              console.log('Token refresh failed, clearing auth data');
              this.clearAuthData();
            }
          });
        } else {
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);
        }
      } catch (error) {
        console.error('Error parsing stored user data:', error);
        this.clearAuthData();
      }
    }
  }

  login(loginRequest: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_BASE_URL}/auth/login`, loginRequest)
      .pipe(
        tap(authResponse => {
          if (authResponse.status === ResponseStatus.SUCCESS && authResponse.token) {
            this.setAuthData(authResponse);
          }
        }),
        catchError(this.handleError)
      );
  }

  register(registerRequest: RegistrationRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_BASE_URL}/auth/register`, registerRequest)
      .pipe(
        tap(authResponse => {
          if (authResponse.status === ResponseStatus.SUCCESS && authResponse.token) {
            this.setAuthData(authResponse);
          }
        }),
        catchError(this.handleError)
      );
  }

  // fetchUserDetails()

  logout(): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_BASE_URL}/auth/logout`, {})
      .pipe(
        tap(() => this.clearAuthData()),
        catchError((error) => {
          // Even if logout fails on server, clear local data
          this.clearAuthData();
          return throwError(error);
        })
      );
  }

  refreshToken(): Observable<string> {
    return this.http.post<AuthResponse>(`${this.API_BASE_URL}/auth/refresh`, {})
      .pipe(
        map(response => {
          if (response.status === ResponseStatus.SUCCESS && response.token) {
            localStorage.setItem(this.TOKEN_KEY, response.token);
            return response.token;
          }
          throw new Error(response.message || 'Token refresh failed');
        }),
        catchError(this.handleError)
      );
  }

  hasPermission(permission: keyof ProfilePermissions): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    
    const permissions = PROFILE_ACCESS[user.profile];
    return permissions[permission];
  }

  getUserProfile(): UserProfile | null {
    const user = this.getCurrentUser();
    return user ? user.profile : null;
  }

  isAdmin(): boolean {
    return this.getUserProfile() === 'admin';
  }

  isDoctor(): boolean {
    return this.getUserProfile() === 'doctor';
  }

  isPatient(): boolean {
    return this.getUserProfile() === 'patient';
  }

  isStaff(): boolean {
    return this.getUserProfile() === 'staff';
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  checkEmailExists(email: string): Observable<boolean> {
    return this.http.get<{exists: boolean}>(`${this.API_BASE_URL}/auth/check-email/${email}`)
      .pipe(
        map(response => response.exists),
        catchError(this.handleError)
      );
  }

  checkUsernameExists(username: string): Observable<boolean> {
    return this.http.get<{exists: boolean}>(`${this.API_BASE_URL}/auth/check-username/${username}`)
      .pipe(
        map(response => response.exists),
        catchError(this.handleError)
      );
  }

  changePassword(currentPassword: string, newPassword: string): Observable<AuthResponse> {
    const request = { currentPassword, newPassword };
    return this.http.post<AuthResponse>(`${this.API_BASE_URL}/auth/change-password`, request)
      .pipe(
        catchError(this.handleError)
      );
  }

  getPermissions(): ProfilePermissions | null {
    const user = this.getCurrentUser();
    return user ? PROFILE_ACCESS[user.profile] : null;
  }

  private setAuthData(authResponse: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResponse.token);
    const user: Partial<User> = {
      id: '',
      profile: this.normalizeRole(authResponse.role),
      firstName: '',
      lastName: '',
      email: '',
      isActive: true,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    try {
      const tokenPayload = JSON.parse(atob(authResponse.token.split('.')[1]));
      if (tokenPayload.firstName) user.firstName = tokenPayload.firstName;
      if (tokenPayload.lastName) user.lastName = tokenPayload.lastName;
      if (tokenPayload.email) user.email = tokenPayload.email;
      if (tokenPayload.userId) user.id = tokenPayload.userId;
    } catch (error) {
      console.warn('Could not extract user info from token:', error);
    }
    
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    
    this.currentUserSubject.next(user as User);
    this.isAuthenticatedSubject.next(true);
  }

  private normalizeRole(role: string | undefined): UserProfile {
    if (!role) return 'patient';
    const r = role.toString().toLowerCase();
    if (r.includes('admin')) return 'admin';
    if (r.includes('doctor')) return 'doctor';
    if (r.includes('staff') || r.includes('employee')) return 'staff';
    if (r.includes('patient') || r.includes('user')) return 'patient';
    return 'patient';
  }

  private clearAuthData(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }
  resetAuth(navigateToLogin = true): void {
    this.clearAuthData();
    if (navigateToLogin) {
      try {
        this.router.navigateByUrl('/auth/login');
      } catch (e) {
        // ignore navigation errors during reset
      }
    }
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp < currentTime;
    } catch (error) {
      console.error('Error parsing token:', error);
      return true; // Consider invalid tokens as expired
    }
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else {
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.error && error.error.errors && error.error.errors.length > 0) {
        errorMessage = error.error.errors.join(', ');
      } else {
        errorMessage = `Server returned code ${error.status}: ${error.statusText}`;
      }
    }
    
    console.error('Auth Service Error:', errorMessage);
    return throwError(errorMessage);
  }
}