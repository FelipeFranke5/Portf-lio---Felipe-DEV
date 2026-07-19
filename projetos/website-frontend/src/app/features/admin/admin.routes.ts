import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./admin-dashboard.component').then(
        (m) => m.AdminDashboardComponent
      ),
  },
  {
    path: 'projects',
    loadComponent: () =>
      import('./projects/admin-projects.component').then(
        (m) => m.AdminProjectsComponent
      ),
  },
  {
    path: 'skills',
    loadComponent: () =>
      import('./skills/admin-skills.component').then(
        (m) => m.AdminSkillsComponent
      ),
  },
  {
    path: 'logs',
    loadComponent: () =>
      import('./logs/admin-logs.component').then(
        (m) => m.AdminLogsComponent
      ),
  },
];
