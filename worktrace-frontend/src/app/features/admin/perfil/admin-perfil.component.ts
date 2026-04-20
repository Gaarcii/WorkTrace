import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ProfilePageComponent } from '../../profile-page/profile-page.component';

@Component({
  selector: 'app-admin-perfil',
  standalone: true,
  imports: [ProfilePageComponent],
  templateUrl: './admin-perfil.component.html',
  styleUrls: ['./admin-perfil.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminPerfilComponent {}
