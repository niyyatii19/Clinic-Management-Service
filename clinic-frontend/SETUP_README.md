# Clinic Management System - Frontend

A comprehensive Angular-based frontend application for clinic management with role-based authentication and beautiful UI.

## 🚀 Features

### Authentication & Authorization
- **Multi-Profile Registration**: Support for Doctor, Patient, Staff, and Admin profiles
- **Role-Based Access Control**: Different dashboard and permissions for each user type
- **Secure Authentication**: JWT token-based authentication with refresh token support
- **Route Guards**: Protected routes based on user roles and permissions

### User Profiles & Dashboards
- **Admin Dashboard**: Complete system oversight with user management, reports, and system settings
- **Doctor Dashboard**: Patient management, appointment scheduling, and medical records
- **Staff Dashboard**: Appointment management, patient registration, and billing support
- **Patient Dashboard**: Appointment booking, medical records view, and prescription management

### UI/UX Features
- **Professional Design**: Modern, responsive design using Bootstrap 5
- **Smooth Animations**: CSS animations and transitions throughout the application
- **Glass Morphism Effects**: Modern glassmorphism design elements
- **Mobile Responsive**: Fully responsive design for all screen sizes
- **Accessibility**: WCAG compliant with proper ARIA labels and keyboard navigation

## 🛠️ Technology Stack

- **Frontend Framework**: Angular 20.x
- **UI Library**: Bootstrap 5 + ng-bootstrap
- **Icons**: Font Awesome 6
- **Animations**: CSS3 animations + Animate.css
- **State Management**: RxJS + Angular Services
- **Form Validation**: Angular Reactive Forms with custom validators
- **HTTP Client**: Angular HttpClient with interceptors
- **Routing**: Angular Router with lazy loading

## 📋 Prerequisites

- Node.js (v20.19.0 or higher)
- npm (v8.0.0 or higher)
- Angular CLI (v20.x)

## 🚀 Getting Started

### 1. Clone the Repository
\`\`\`bash
git clone <repository-url>
cd clinic-frontend
\`\`\`

### 2. Install Dependencies
\`\`\`bash
npm install
\`\`\`

### 3. Install Additional UI Dependencies
\`\`\`bash
npm install bootstrap@5.3.0 @ng-bootstrap/ng-bootstrap @fortawesome/fontawesome-free animate.css
\`\`\`

### 4. Development Server
\`\`\`bash
npm start
# or
ng serve
\`\`\`

Navigate to \`http://localhost:4200/\`. The application will automatically reload if you change any source files.

### 5. Build for Production
\`\`\`bash
npm run build
# or
ng build --prod
\`\`\`

## 📁 Project Structure

\`\`\`
src/
├── app/
│   ├── core/                    # Core functionality (guards, interceptors)
│   │   ├── guards/             # Route guards (auth, role, permission)
│   │   └── interceptors/       # HTTP interceptors
│   ├── modules/                # Feature modules
│   │   ├── admin/              # Admin dashboard and features
│   │   ├── auth/               # Authentication (login, register)
│   │   ├── doctor/             # Doctor dashboard and features
│   │   ├── patient/            # Patient dashboard and features
│   │   └── staff/              # Staff dashboard and features
│   ├── services/               # Global services
│   │   └── auth.service.ts     # Authentication service
│   ├── shared/                 # Shared components and utilities
│   │   ├── components/         # Reusable components
│   │   ├── models/             # TypeScript interfaces and models
│   │   ├── services/           # Shared services
│   │   ├── utils/              # Utility functions
│   │   └── validators/         # Custom form validators
│   ├── app.config.ts           # App configuration
│   ├── app.routes.ts           # Main routing configuration
│   └── app.component.*         # Root component
├── styles.scss                 # Global styles
└── index.html                  # Main HTML file
\`\`\`

## 🔐 User Roles & Permissions

### Admin
- **Full Access**: Complete system management
- **Features**: User management, system settings, reports, billing oversight
- **Dashboard**: Comprehensive admin panel with system statistics

### Doctor
- **Medical Focus**: Patient care and medical records
- **Features**: Patient management, appointment scheduling, medical records
- **Dashboard**: Medical-focused interface with patient information

### Staff
- **Operational Support**: Administrative and support functions
- **Features**: Appointment management, patient registration, billing
- **Dashboard**: Operational interface for clinic support

### Patient
- **Self-Service**: Personal health management
- **Features**: Appointment booking, medical records view, prescriptions
- **Dashboard**: Personal health portal

## 🎨 UI Components & Styling

### Design System
- **Color Palette**: Professional medical theme with gradient backgrounds
- **Typography**: Inter font family for modern readability
- **Spacing**: Consistent spacing using Bootstrap's spacing utilities
- **Animations**: Smooth transitions and micro-interactions

### Key Components
- **Authentication Forms**: Multi-step registration with profile selection
- **Dashboard Cards**: Animated statistics and quick action cards
- **Navigation**: Role-based navigation with dropdown menus
- **Alerts**: Contextual alerts with auto-dismiss functionality

## 🔧 Configuration

### Environment Variables
Create environment files in \`src/environments/\`:

\`\`\`typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:3000/api',
  apiTimeout: 30000,
  enableDebug: true
};
\`\`\`

### API Configuration
Update the \`AuthService\` with your backend API URL:

\`\`\`typescript
private readonly API_BASE_URL = 'http://your-api-url/api';
\`\`\`

## 🛡️ Security Features

### Authentication
- JWT token-based authentication
- Automatic token refresh
- Secure token storage
- Session timeout handling

### Authorization
- Role-based route protection
- Permission-based UI rendering
- Secure API communication
- CSRF protection ready

### Data Protection
- Input validation and sanitization
- XSS protection
- Secure form handling
- HIPAA compliance considerations

## 🧪 Testing

### Running Unit Tests
\`\`\`bash
npm test
# or
ng test
\`\`\`

### Running End-to-End Tests
\`\`\`bash
npm run e2e
# or
ng e2e
\`\`\`

## 📱 Mobile Responsiveness

The application is fully responsive with:
- Mobile-first design approach
- Touch-friendly interfaces
- Optimized layouts for tablets and phones
- Progressive Web App (PWA) ready

## 🎯 Future Enhancements

- [ ] Real-time notifications with WebSocket
- [ ] Offline capability with service workers
- [ ] Advanced reporting dashboard
- [ ] Multi-language support (i18n)
- [ ] Advanced search and filtering
- [ ] File upload and document management
- [ ] Integration with external medical systems
- [ ] Advanced calendar and scheduling

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (\`git checkout -b feature/amazing-feature\`)
3. Commit your changes (\`git commit -m 'Add amazing feature'\`)
4. Push to the branch (\`git push origin feature/amazing-feature\`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation wiki

## 🙏 Acknowledgments

- Angular team for the excellent framework
- Bootstrap team for the UI components
- Font Awesome for the icon library
- The open-source community for various packages and inspiration

---

**Note**: This is a frontend application that requires a backend API to be fully functional. Make sure to set up the corresponding backend service and update the API endpoints accordingly.