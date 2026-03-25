import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-worker-perfil-identity',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './worker-perfil-identity.component.html',
  styleUrls: ['./worker-perfil-identity.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WorkerPerfilIdentityComponent {
  readonly avatarUrl = input<string | undefined | null>(null);
  readonly nombreCompleto = input<string | undefined | null>(null);
  readonly puestoTrabajo = input<string | undefined | null>(null);

  readonly editAvatar = output<void>();

  onEditAvatar(): void {
    this.editAvatar.emit();
  }
}
