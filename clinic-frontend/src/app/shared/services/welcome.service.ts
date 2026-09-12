import { Injectable } from '@angular/core';
import { User } from '../models/user.model';

export interface WelcomeMessage {
  title: string;
  message: string;
  icon: string;
  color: string;
}

@Injectable({
  providedIn: 'root'
})
export class WelcomeService {

  constructor() { }

  /**
   * Get personalized welcome message based on user profile
   */
  getWelcomeMessage(user: User): WelcomeMessage {
    const messages = {
      admin: {
        title: `Welcome back, ${user.firstName}!`,
        message: `Ready to manage the clinic operations? You have full administrative access to all systems.`,
        icon: 'fas fa-user-shield',
        color: 'primary'
      },
      doctor: {
        title: `Welcome back, Dr. ${user.lastName}!`,
        message: `Ready to provide excellent patient care today? Your expertise makes a difference in every life you touch.`,
        icon: 'fas fa-user-md',
        color: 'success'
      },
      staff: {
        title: `Welcome back, ${user.firstName}!`,
        message: `Let's make today productive! Your dedication keeps our clinic running smoothly.`,
        icon: 'fas fa-users',
        color: 'info'
      },
      patient: {
        title: `Welcome back, ${user.firstName}!`,
        message: `We're here to take care of your health and wellness. Your wellbeing is our priority.`,
        icon: 'fas fa-user-injured',
        color: 'warning'
      }
    };
    const defaultMsg: WelcomeMessage = {
      title: `Welcome back, ${user.firstName || 'User'}!`,
      message: `Welcome to the clinic management system.`,
      icon: 'fas fa-handshake',
      color: 'primary'
    };

    return (messages as any)[user.profile] || defaultMsg;
  }

  /**
   * Show welcome popup (can be implemented with your preferred modal/toast library)
   */
  showWelcomePopup(user: User): void {
    const welcomeData = this.getWelcomeMessage(user);
    
    // For now, we'll use a simple alert
    // In a real application, you would integrate with a toast or modal service
    if ('Notification' in window) {
      // Check if we can show browser notifications
      Notification.requestPermission().then(permission => {
        if (permission === 'granted' && welcomeData) {
          new Notification(welcomeData.title, {
            body: welcomeData.message,
            icon: '/favicon.ico'
          });
        }
      });
    }

    // You can also dispatch a custom event that components can listen to
    const event = new CustomEvent('welcome-message', {
      detail: welcomeData
    });
    window.dispatchEvent(event);
  }

  getDashboardRoute(user: User): string {
    const routes = {
      admin: '/admin/dashboard',
      doctor: '/doctor/dashboard',
      staff: '/staff/dashboard',
      patient: '/patient/dashboard'
    };
    return (routes as any)[user.profile] || '/';
  }

  getQuickActions(user: User): Array<{title: string; description: string; route: string; icon: string}> {
    const actions = {
      admin: [
        {
          title: 'User Management',
          description: 'Manage user accounts and permissions',
          route: '/admin/users',
          icon: 'fas fa-users-cog'
        },
        {
          title: 'System Reports',
          description: 'View system analytics and reports',
          route: '/admin/reports',
          icon: 'fas fa-chart-bar'
        },
        {
          title: 'Settings',
          description: 'Configure system settings',
          route: '/admin/settings',
          icon: 'fas fa-cogs'
        }
      ],
      doctor: [
        {
          title: 'My Patients',
          description: 'View and manage your patients',
          route: '/doctor/patients',
          icon: 'fas fa-user-injured'
        },
        {
          title: 'Appointments',
          description: 'Manage your appointment schedule',
          route: '/doctor/appointments',
          icon: 'fas fa-calendar-check'
        },
        {
          title: 'Medical Records',
          description: 'Access patient medical records',
          route: '/doctor/records',
          icon: 'fas fa-file-medical'
        }
      ],
      staff: [
        {
          title: 'Appointments',
          description: 'Manage patient appointments',
          route: '/staff/appointments',
          icon: 'fas fa-calendar-plus'
        },
        {
          title: 'Patient Registration',
          description: 'Register new patients',
          route: '/staff/register-patient',
          icon: 'fas fa-user-plus'
        },
        {
          title: 'Billing',
          description: 'Handle billing and payments',
          route: '/staff/billing',
          icon: 'fas fa-file-invoice-dollar'
        }
      ],
      patient: [
        {
          title: 'Book Appointment',
          description: 'Schedule a new appointment',
          route: '/patient/book-appointment',
          icon: 'fas fa-calendar-plus'
        },
        {
          title: 'My Records',
          description: 'View your medical records',
          route: '/patient/records',
          icon: 'fas fa-file-medical'
        },
        {
          title: 'Prescriptions',
          description: 'View your prescriptions',
          route: '/patient/prescriptions',
          icon: 'fas fa-prescription-bottle'
        }
      ]
    };

    return actions[user.profile] || [];
  }
}