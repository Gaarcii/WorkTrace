import { ChangeDetectionStrategy, Component, OnInit, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProfileService } from '../../services/profile.service';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-header-bar',
  imports: [CommonModule],
  templateUrl: './header-bar.component.html',
  styleUrls: ['./header-bar.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderBarComponent implements OnInit {
  private readonly profileService = inject(ProfileService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly userSignal = this.profileService.currentUser;

  readonly userName = computed(() => this.userSignal()?.fullName ?? 'Usuario');

  readonly userRole = computed(() => this.userSignal()?.jobPosition ?? 'Empleado');

  readonly userAvatar = computed(() => this.userSignal()?.avatarUrl ?? null);

  ngOnInit(): void {
    if (!this.userSignal()) {
      this.profileService.fetchMyProfile().subscribe();
    }
  }

  handleLogout(): void {
    this.profileService.currentUser.set(null);
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
