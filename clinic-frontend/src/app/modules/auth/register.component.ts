import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { NgbAlert, NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { AuthService } from '../../services/auth.service';
import { RegistrationRequest, UserProfile, AuthResponse, ResponseStatus } from '../../shared/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgbAlert],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent implements OnInit, OnDestroy {
  registerForm: FormGroup;
  profileDetailsForm: FormGroup | null = null;
  
  isLoading = false;
  showPassword = false;
  showConfirmPassword = false;
  currentStep = 1;
  totalSteps = 3;
  
  errorMessage = '';
  successMessage = '';
  
  selectedProfile: UserProfile | null = null;
  
  profiles = [
    {
      type: 'patient' as UserProfile,
      title: 'Patient',
      description: 'I need medical care and want to book appointments',
      icon: 'fas fa-user-injured',
      color: 'primary'
    },
    {
      type: 'doctor' as UserProfile,
      title: 'Doctor',
      description: 'I am a medical professional providing healthcare services',
      icon: 'fas fa-user-md',
      color: 'success'
    },
    {
      type: 'staff' as UserProfile,
      title: 'Staff',
      description: 'I work at the clinic in administrative or support role',
      icon: 'fas fa-users',
      color: 'info'
    },
    {
      type: 'admin' as UserProfile,
      title: 'Administrator',
      description: 'I manage the clinic operations and system',
      icon: 'fas fa-user-shield',
      color: 'warning'
    }
  ];

  specializations = [
    'Cardiology', 'Dermatology', 'Endocrinology', 'Gastroenterology',
    'General Medicine', 'Neurology', 'Oncology', 'Orthopedics',
    'Pediatrics', 'Psychiatry', 'Radiology', 'Surgery'
  ];

  departments = [
    'Emergency', 'Outpatient', 'Inpatient', 'ICU', 'Laboratory',
    'Radiology', 'Pharmacy', 'Administration', 'Nursing', 'Reception'
  ];

  designations = [
    'Nurse', 'Receptionist', 'Lab Technician', 'Pharmacist',
    'Administrator', 'Manager', 'Coordinator', 'Assistant'
  ];

  bloodGroups = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.createRegisterForm();
  }

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/']);
    }

    this.setupFormValidation();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private createRegisterForm(): FormGroup {
    return this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      username: ['', [Validators.minLength(3), Validators.maxLength(20)]],
      password: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', [Validators.required]],
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      phoneNumber: ['', [Validators.pattern(/^\+?[\d\s-()]+$/)]],
      dateOfBirth: [''],
      address: [''],
      agreeToTerms: [false, [Validators.requiredTrue]]
    }, { validators: this.passwordMatchValidator });
  }

  private createProfileDetailsForm(profile: UserProfile): FormGroup {
    switch (profile) {
      case 'doctor':
        return this.fb.group({
          licenseNumber: ['', [Validators.required]],
          specialization: [[], [Validators.required]],
          experience: [0, [Validators.required, Validators.min(0)]],
          department: ['', [Validators.required]],
          qualifications: ['', [Validators.required]],
          consultationFee: [0, [Validators.min(0)]]
        });
      
      case 'staff':
        return this.fb.group({
          employeeId: ['', [Validators.required]],
          department: ['', [Validators.required]],
          designation: ['', [Validators.required]],
          joiningDate: ['', [Validators.required]]
        });
      
      case 'patient':
        return this.fb.group({
          emergencyContactName: ['', [Validators.required]],
          emergencyContactPhone: ['', [Validators.required, Validators.pattern(/^\+?[\d\s-()]+$/)]],
          emergencyContactRelation: ['', [Validators.required]],
          bloodGroup: [''],
          allergies: [''],
          medicalHistory: ['']
        });
      
      case 'admin':
        return this.fb.group({
          adminLevel: ['manager', [Validators.required]],
          managerCode: ['', [Validators.required]]
        });
      
      default:
        return this.fb.group({});
    }
  }

  private setupFormValidation(): void {
    // Email existence check
    this.registerForm.get('email')?.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(email => {
      if (email && this.registerForm.get('email')?.valid) {
        this.checkEmailExists(email);
      }
    });

    // Username existence check
    this.registerForm.get('username')?.valueChanges.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(username => {
      if (username && this.registerForm.get('username')?.valid) {
        this.checkUsernameExists(username);
      }
    });
  }

  private passwordValidator(control: AbstractControl): {[key: string]: any} | null {
    const value = control.value;
    if (!value) return null;

    const hasNumber = /[0-9]/.test(value);
    const hasUpper = /[A-Z]/.test(value);
    const hasLower = /[a-z]/.test(value);
    const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value);

    const valid = hasNumber && hasUpper && hasLower && hasSpecial;
    
    if (!valid) {
      return { 'passwordStrength': true };
    }
    
    return null;
  }

  private passwordMatchValidator(group: AbstractControl): {[key: string]: any} | null {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    
    if (password && confirmPassword && password !== confirmPassword) {
      return { 'passwordMismatch': true };
    }
    
    return null;
  }

  private checkEmailExists(email: string): void {
    this.authService.checkEmailExists(email).subscribe({
      next: (exists) => {
        if (exists) {
          this.registerForm.get('email')?.setErrors({ 'emailExists': true });
        }
      },
      error: (error) => {
        console.error('Error checking email:', error);
      }
    });
  }

  private checkUsernameExists(username: string): void {
    this.authService.checkUsernameExists(username).subscribe({
      next: (exists) => {
        if (exists) {
          this.registerForm.get('username')?.setErrors({ 'usernameExists': true });
        }
      },
      error: (error) => {
        console.error('Error checking username:', error);
      }
    });
  }

  selectProfile(profile: UserProfile): void {
    this.selectedProfile = profile;
    this.profileDetailsForm = this.createProfileDetailsForm(profile);
  }

  nextStep(): void {
    if (this.currentStep === 1 && this.selectedProfile) {
      this.currentStep = 2;
    } else if (this.currentStep === 2 && this.registerForm.valid) {
      this.currentStep = 3;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  onSubmit(): void {
    if (this.registerForm.valid && this.profileDetailsForm?.valid && !this.isLoading) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const formValue = this.registerForm.value;
      const profileDetails = this.profileDetailsForm.value;

      const registerRequest: RegistrationRequest = {
        email: formValue.email.trim(),
        username: formValue.username?.trim(),
        password: formValue.password,
        confirmPassword: formValue.confirmPassword,
        firstName: formValue.firstName.trim(),
        lastName: formValue.lastName.trim(),
        profile: this.selectedProfile!,
        phoneNumber: formValue.phoneNumber?.trim(),
        dateOfBirth: formValue.dateOfBirth,
        address: formValue.address?.trim()
      };

      // Add profile-specific details
      this.addProfileSpecificDetails(registerRequest, profileDetails);

      this.authService.register(registerRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response: AuthResponse) => {
            this.isLoading = false;
            if (response.status === ResponseStatus.SUCCESS) {
              this.successMessage = 'Registration successful! Redirecting to dashboard...';
              setTimeout(() => {
                const user = this.authService.getCurrentUser();
                if (user) {
                  const dashboardRoute = this.getDashboardRoute(user.profile);
                  this.router.navigate([dashboardRoute]);
                } else {
                  this.router.navigate(['/auth/login']);
                }
              }, 2000);
            } else {
              this.errorMessage = response.message || 'Registration failed. Please try again.';
            }
          },
          error: (error) => {
            this.isLoading = false;
            this.errorMessage = error || 'An error occurred during registration. Please try again.';
          }
        });
    } else {
      this.markAllFormGroupsTouched();
    }
  }

  private addProfileSpecificDetails(request: RegistrationRequest, details: any): void {
    switch (this.selectedProfile) {
      case 'doctor':
        request.doctorDetails = {
          licenseNumber: details.licenseNumber,
          specialization: details.specialization,
          experience: details.experience,
          department: details.department,
          qualifications: details.qualifications.split(',').map((q: string) => q.trim()),
          consultationFee: details.consultationFee,
          availableSlots: []
        };
        break;
      
      case 'staff':
        request.staffDetails = {
          employeeId: details.employeeId,
          department: details.department,
          designation: details.designation,
          joiningDate: new Date(details.joiningDate),
          permissions: []
        };
        break;
      
      case 'patient':
        request.patientDetails = {
          emergencyContact: {
            name: details.emergencyContactName,
            phoneNumber: details.emergencyContactPhone,
            relationship: details.emergencyContactRelation
          },
          medicalHistory: details.medicalHistory ? [{
            condition: details.medicalHistory,
            diagnosedDate: new Date(),
            treatment: '',
            status: 'active' as any
          }] : [],
          bloodGroup: details.bloodGroup,
          allergies: details.allergies ? details.allergies.split(',').map((a: string) => a.trim()) : []
        };
        break;
    }
  }

  togglePasswordVisibility(field: 'password' | 'confirmPassword'): void {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  goToLogin(): void {
    this.router.navigate(['/auth/login']);
  }

  getFieldError(fieldName: string, formGroup?: FormGroup): string {
    const form = formGroup || this.registerForm;
    const field = form.get(fieldName);
    
    if (field?.errors && field.touched) {
      const errors = field.errors;
      
      if (errors['required']) return `${this.getFieldLabel(fieldName)} is required`;
      if (errors['email']) return 'Please enter a valid email address';
      if (errors['minlength']) return `${this.getFieldLabel(fieldName)} must be at least ${errors['minlength'].requiredLength} characters`;
      if (errors['maxlength']) return `${this.getFieldLabel(fieldName)} cannot exceed ${errors['maxlength'].requiredLength} characters`;
      if (errors['pattern']) return `${this.getFieldLabel(fieldName)} format is invalid`;
      if (errors['min']) return `${this.getFieldLabel(fieldName)} must be at least ${errors['min'].min}`;
      if (errors['passwordStrength']) return 'Password must contain uppercase, lowercase, number, and special character';
      if (errors['passwordMismatch']) return 'Passwords do not match';
      if (errors['emailExists']) return 'This email is already registered';
      if (errors['usernameExists']) return 'This username is already taken';
      if (errors['requiredTrue']) return 'You must agree to the terms and conditions';
    }
    
    return '';
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      email: 'Email',
      username: 'Username',
      password: 'Password',
      confirmPassword: 'Confirm Password',
      firstName: 'First Name',
      lastName: 'Last Name',
      phoneNumber: 'Phone Number',
      dateOfBirth: 'Date of Birth',
      address: 'Address'
    };
    return labels[fieldName] || fieldName;
  }

  hasFieldError(fieldName: string, formGroup?: FormGroup): boolean {
    const form = formGroup || this.registerForm;
    const field = form.get(fieldName);
    return !!(field?.errors && field.touched);
  }

  private markAllFormGroupsTouched(): void {
    this.markFormGroupTouched(this.registerForm);
    if (this.profileDetailsForm) {
      this.markFormGroupTouched(this.profileDetailsForm);
    }
  }

  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  closeAlert(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  getCurrentYear(): number {
    return new Date().getFullYear();
  }

  private getDashboardRoute(profile: UserProfile): string {
    const routes = {
      admin: '/admin/dashboard',
      doctor: '/doctor/dashboard',
      staff: '/staff/dashboard',
      patient: '/patient/dashboard'
    };
    return routes[profile];
  }

  getProgressPercentage(): number {
    return (this.currentStep / this.totalSteps) * 100;
  }
}