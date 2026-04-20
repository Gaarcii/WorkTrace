import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ProfilePageComponent } from '../../profile/profile-page/profile-page.component';

@Component({
  selector: 'app-worker-perfil',
  imports: [ProfilePageComponent],
  templateUrl: './worker-perfil.component.html',
  styleUrls: ['./worker-perfil.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerPerfilComponent {}
