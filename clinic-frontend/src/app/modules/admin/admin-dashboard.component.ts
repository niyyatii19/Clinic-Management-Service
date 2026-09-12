import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { User } from '../../shared/models/user.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  currentUser: User | null = null;
  
  dashboardStats = {
    totalPatients: 1250,
    totalDoctors: 35,
    totalStaff: 82,
    totalAppointments: 156,
    monthlyRevenue: 125000,
    pendingApprovals: 12
  };

  quickActions = [
    {
      title: 'User Management',
      description: 'Manage doctors, staff, and patient accounts',
      icon: 'fas fa-users-cog',
      color: 'primary',
      route: '/admin/users'
    },
    {
      title: 'System Settings',
      description: 'Configure system preferences and settings',
      icon: 'fas fa-cogs',
      color: 'secondary',
      route: '/admin/settings'
    },
    {
      title: 'Reports & Analytics',
      description: 'View comprehensive reports and analytics',
      icon: 'fas fa-chart-bar',
      color: 'success',
      route: '/admin/reports'
    },
    {
      title: 'Billing Management',
      description: 'Oversee billing and payment processing',
      icon: 'fas fa-file-invoice-dollar',
      color: 'warning',
      route: '/admin/billing'
    },
    {
      title: 'Inventory Control',
      description: 'Manage medical supplies and equipment',
      icon: 'fas fa-boxes',
      color: 'info',
      route: '/admin/inventory'
    },
    {
      title: 'Security Logs',
      description: 'Monitor system security and access logs',
      icon: 'fas fa-shield-alt',
      color: 'danger',
      route: '/admin/security'
    }
  ];

  recentActivity = [
    {
      user: 'Dr. Sarah Johnson',
      action: 'Updated patient record',
      time: '2 minutes ago',
      icon: 'fas fa-file-medical',
      type: 'update'
    },
    {
      user: 'Nurse Mike Wilson',
      action: 'Scheduled new appointment',
      time: '15 minutes ago',
      icon: 'fas fa-calendar-plus',
      type: 'create'
    },
    {
      user: 'Admin System',
      action: 'Daily backup completed',
      time: '1 hour ago',
      icon: 'fas fa-database',
      type: 'system'
    },
    {
      user: 'Dr. Robert Chen',
      action: 'Approved prescription',
      time: '2 hours ago',
      icon: 'fas fa-prescription-bottle',
      type: 'approval'
    }
  ];

  systemAlerts = [
    {
      message: 'Server maintenance scheduled for tonight at 11 PM',
      type: 'warning',
      icon: 'fas fa-exclamation-triangle'
    },
    {
      message: '12 user accounts pending approval',
      type: 'info',
      icon: 'fas fa-user-clock'
    },
    {
      message: 'Backup storage is 85% full',
      type: 'danger',
      icon: 'fas fa-hdd'
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // this.authService.fetchUserDetails().
    //   subscribe({
    //     next: (user) => {
    //       this.currentUser = user;
    //     }
    //   });
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/auth/login']);
      },
      error: (error) => {
        console.error('Logout error:', error);
        // Force logout even if server request fails
        this.router.navigate(['/auth/login']);
      }
    });
  }

  getGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good Morning';
    if (hour < 17) return 'Good Afternoon';
    return 'Good Evening';
  }

  getStatIcon(statKey: string): string {
    const icons = {
      totalPatients: 'fas fa-user-injured',
      totalDoctors: 'fas fa-user-md',
      totalStaff: 'fas fa-users',
      totalAppointments: 'fas fa-calendar-check',
      monthlyRevenue: 'fas fa-dollar-sign',
      pendingApprovals: 'fas fa-clock'
    };
    return icons[statKey as keyof typeof icons] || 'fas fa-chart-line';
  }

  getStatColor(statKey: string): string {
    const colors = {
      totalPatients: 'primary',
      totalDoctors: 'success',
      totalStaff: 'info',
      totalAppointments: 'warning',
      monthlyRevenue: 'success',
      pendingApprovals: 'danger'
    };
    return colors[statKey as keyof typeof colors] || 'primary';
  }

  formatNumber(num: number): string {
    if (num >= 1000000) {
      return (num / 1000000).toFixed(1) + 'M';
    }
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'K';
    }
    return num.toString();
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0
    }).format(amount);
  }
}