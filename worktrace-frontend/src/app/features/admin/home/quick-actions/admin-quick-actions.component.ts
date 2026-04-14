import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { QuickAction } from '../admin-home.types';

@Component({
  selector: 'app-admin-quick-actions',
  templateUrl: './admin-quick-actions.component.html',
  styleUrls: ['./admin-quick-actions.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminQuickActionsComponent {
  readonly acciones = input<QuickAction[]>([]);

  readonly actionClick = output<QuickAction>();

  emitAction(accion: QuickAction): void {
    this.actionClick.emit(accion);
  }

  resolveIcon(icono: string): string {
    switch (icono) {
      case 'mdi-download':
        return 'download';
      case 'mdi-account-plus':
        return 'person_add';
      case 'mdi-calendar-blank':
        return 'calendar_month';
      case 'mdi-bell-outline':
        return 'notifications';
      case 'mdi-cog-outline':
        return 'settings';
      default:
        return 'apps';
    }
  }
}
