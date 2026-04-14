import { Component } from '@angular/core';
import { AdminHomeStatsComponent } from './stats/admin-home-stats.component';

@Component({
  selector: 'app-home.component',
  imports: [AdminHomeStatsComponent],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.scss',
})
export class AdminHomeComponent {}
