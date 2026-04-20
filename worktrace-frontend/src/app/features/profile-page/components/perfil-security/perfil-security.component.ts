import { Component, ChangeDetectionStrategy, output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-perfil-security',
  imports: [MatIconModule, MatButtonModule],
  templateUrl: './perfil-security.component.html',
  styleUrls: ['./perfil-security.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PerfilSecurityComponent {
  readonly changePassword = output<void>();

  onChange(): void {
    this.changePassword.emit();
  }
}
