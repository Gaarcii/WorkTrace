import { Component } from '@angular/core';
import { AdminHomeStatsComponent } from './stats/admin-home-stats.component';
import { AdminRecentAlertsComponent } from './alerts/admin-recent-alerts.component';
import { AdminWeeklyChartComponent } from './weekly-chart/admin-weekly-chart.component';

@Component({
  selector: 'app-home.component',
  imports: [AdminHomeStatsComponent, AdminRecentAlertsComponent, AdminWeeklyChartComponent],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.scss',
})
export class AdminHomeComponent {}
