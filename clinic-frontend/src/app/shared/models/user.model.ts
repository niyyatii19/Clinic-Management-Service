// User Profile Types
export type UserProfile = 'doctor' | 'patient' | 'staff' | 'admin';

// User Model
export interface User {
  id: string;
  email: string;
  username?: string;
  firstName: string;
  lastName: string;
  profile: UserProfile;
  phoneNumber?: string;
  dateOfBirth?: string;
  address?: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
  
  // Profile-specific fields
  doctorDetails?: DoctorDetails;
  patientDetails?: PatientDetails;
  staffDetails?: StaffDetails;
  adminDetails?: AdminDetails;
}

// Doctor-specific details
export interface DoctorDetails {
  licenseNumber: string;
  specialization: string[];
  experience: number;
  department: string;
  qualifications: string[];
  consultationFee: number;
  availableSlots: TimeSlot[];
}

// Patient-specific details  
export interface PatientDetails {
  emergencyContact: EmergencyContact;
  medicalHistory: MedicalHistory[];
  insuranceDetails?: InsuranceDetails;
  bloodGroup?: string;
  allergies?: string[];
}

// Staff-specific details
export interface StaffDetails {
  employeeId: string;
  department: string;
  designation: string;
  joiningDate: Date;
  salary?: number;
  permissions: string[];
}

// Admin-specific details
export interface AdminDetails {
  adminLevel: 'super' | 'manager' | 'support';
  permissions: string[];
  lastLogin: Date;
}

// Supporting interfaces
export interface TimeSlot {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  isAvailable: boolean;
}

export interface EmergencyContact {
  name: string;
  relationship: string;
  phoneNumber: string;
  email?: string;
}

export interface MedicalHistory {
  condition: string;
  diagnosedDate: Date;
  treatment: string;
  status: 'active' | 'resolved' | 'chronic';
}

export interface InsuranceDetails {
  provider: string;
  policyNumber: string;
  coverageAmount: number;
  expiryDate: Date;
}

// Backend API Request/Response interfaces
export interface AuthRequest {
  loginInput: string;
  password: string;
}

export interface RegistrationRequest {
  email: string;
  username?: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  profile: UserProfile;
  phoneNumber?: string;
  dateOfBirth?: string;
  address?: string;
  
  // Profile-specific registration fields
  doctorDetails?: Partial<DoctorDetails>;
  patientDetails?: Partial<PatientDetails>;
  staffDetails?: Partial<StaffDetails>;
}

export enum ResponseStatus {
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR'
}

export interface AuthResponse {
  token: string;
  role: string;
  message: string;
  status: ResponseStatus;
}

// Session Management
export interface UserSession {
  token: string;
  role: UserProfile;
  user?: User;
  expiresAt?: Date;
  isValid: boolean;
}

// Token Validation
export interface TokenInfo {
  token: string;
  isExpired: boolean;
  expirationDate?: Date;
}

// Profile Access Permissions
export interface ProfilePermissions {
  canAccessDashboard: boolean;
  canAccessPatients: boolean;
  canAccessDoctors: boolean;
  canAccessAppointments: boolean;
  canAccessReports: boolean;
  canAccessSettings: boolean;
  canAccessUserManagement: boolean;
  canAccessBilling: boolean;
  canAccessInventory: boolean;
  canManageSystem: boolean;
}

// Route Access Configuration
export const PROFILE_ACCESS: Record<UserProfile, ProfilePermissions> = {
  admin: {
    canAccessDashboard: true,
    canAccessPatients: true,
    canAccessDoctors: true,
    canAccessAppointments: true,
    canAccessReports: true,
    canAccessSettings: true,
    canAccessUserManagement: true,
    canAccessBilling: true,
    canAccessInventory: true,
    canManageSystem: true
  },
  doctor: {
    canAccessDashboard: true,
    canAccessPatients: true,
    canAccessDoctors: false,
    canAccessAppointments: true,
    canAccessReports: true,
    canAccessSettings: false,
    canAccessUserManagement: false,
    canAccessBilling: false,
    canAccessInventory: false,
    canManageSystem: false
  },
  staff: {
    canAccessDashboard: true,
    canAccessPatients: true,
    canAccessDoctors: true,
    canAccessAppointments: true,
    canAccessReports: false,
    canAccessSettings: false,
    canAccessUserManagement: false,
    canAccessBilling: true,
    canAccessInventory: true,
    canManageSystem: false
  },
  patient: {
    canAccessDashboard: true,
    canAccessPatients: false,
    canAccessDoctors: true,
    canAccessAppointments: true,
    canAccessReports: false,
    canAccessSettings: false,
    canAccessUserManagement: false,
    canAccessBilling: false,
    canAccessInventory: false,
    canManageSystem: false
  }
};

// API Response wrapper
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
  errors?: string[];
  timestamp: Date;
}