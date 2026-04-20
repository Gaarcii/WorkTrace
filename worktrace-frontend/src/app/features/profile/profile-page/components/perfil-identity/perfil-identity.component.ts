import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-perfil-identity',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './perfil-identity.component.html',
  styleUrls: ['./perfil-identity.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilIdentityComponent {
  readonly avatarUrl = input<string | undefined | null>(null);
  readonly nombreCompleto = input<string | undefined | null>(null);
  readonly puestoTrabajo = input<string | undefined | null>(null);

  readonly editAvatar = output<void>();

  onEditAvatar(): void {
    this.editAvatar.emit();
  }
}
