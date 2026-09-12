import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {
  protected title = 'clinic-frontend';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // The AuthService constructor will automatically call loadUserFromStorage()
    // which handles token validation and refresh if needed
    console.log('Clinic Management System initialized');
  }
}
