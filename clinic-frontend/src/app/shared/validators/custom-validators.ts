import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class CustomValidators {

  static passwordStrength(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value) {
        return null;
      }

      const hasNumber = /[0-9]/.test(value);
      const hasUpper = /[A-Z]/.test(value);
      const hasLower = /[a-z]/.test(value);
      const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value);
      const hasMinLength = value.length >= 8;

      const passwordValid = hasNumber && hasUpper && hasLower && hasSpecial && hasMinLength;

      if (!passwordValid) {
        return { 
          passwordStrength: {
            hasNumber,
            hasUpper,
            hasLower,
            hasSpecial,
            hasMinLength
          }
        };
      }

      return null;
    };
  }

  /**
   * Validator for phone number format
   * Accepts various international formats
   */
  static phoneNumber(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value) {
        return null;
      }

      // Regex for international phone numbers
      const phoneRegex = /^[\+]?[1-9][\d]{0,15}$/;
      const cleanPhone = value.replace(/[\s\-\(\)]/g, '');

      if (!phoneRegex.test(cleanPhone)) {
        return { phoneNumber: true };
      }

      return null;
    };
  }

  /**
   * Validator for medical license number
   */
  static licenseNumber(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value) {
        return null;
      }

      // Basic license number validation (can be customized per region)
      const licenseRegex = /^[A-Z]{1,3}[0-9]{4,8}$/;

      if (!licenseRegex.test(value)) {
        return { licenseNumber: true };
      }

      return null;
    };
  }

  /**
   * Validator for employee ID format
   */
  static employeeId(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value) {
        return null;
      }

      // Employee ID format: EMP + 4-6 digits
      const empIdRegex = /^EMP[0-9]{4,6}$/;

      if (!empIdRegex.test(value)) {
        return { employeeId: true };
      }

      return null;
    };
  }

  /**
   * Validator to check if date is not in the future
   */
  static pastDate(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value) {
        return null;
      }

      const inputDate = new Date(value);
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      if (inputDate > today) {
        return { pastDate: true };
      }

      return null;
    };
  }

  /**
   * Validator for minimum age requirement
   */
  static minAge(minAge: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value) {
        return null;
      }

      const birthDate = new Date(value);
      const today = new Date();
      const age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();

      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
        const actualAge = age - 1;
        if (actualAge < minAge) {
          return { minAge: { required: minAge, actual: actualAge } };
        }
      } else if (age < minAge) {
        return { minAge: { required: minAge, actual: age } };
      }

      return null;
    };
  }

  /**
   * Cross-field validator for password confirmation
   */
  static passwordMatch(passwordField: string, confirmPasswordField: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const password = control.get(passwordField)?.value;
      const confirmPassword = control.get(confirmPasswordField)?.value;

      if (password && confirmPassword && password !== confirmPassword) {
        return { passwordMatch: true };
      }

      return null;
    };
  }

  /**
   * Validator for email domain restrictions
   */
  static allowedDomains(allowedDomains: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value || !value.includes('@')) {
        return null;
      }

      const domain = value.split('@')[1];
      
      if (!allowedDomains.includes(domain)) {
        return { allowedDomains: { allowed: allowedDomains, actual: domain } };
      }

      return null;
    };
  }

  /**
   * Validator for medical specialization codes
   */
  static specializationCode(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      
      if (!value) {
        return null;
      }

      // Specialization code format: SPEC + 3 digits
      const specRegex = /^SPEC[0-9]{3}$/;

      if (!specRegex.test(value)) {
        return { specializationCode: true };
      }

      return null;
    };
  }
}