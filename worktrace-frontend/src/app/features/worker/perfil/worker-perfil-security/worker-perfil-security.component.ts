import { Component, ChangeDetectionStrategy, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-worker-perfil-security',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './worker-perfil-security.component.html',
  styleUrls: ['./worker-perfil-security.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerPerfilSecurityComponent {
  readonly changePassword = output<void>();

  onChange(): void {
    this.changePassword.emit();
  }
}
