import { Routes } from '@angular/router';

import { Login } from './page/login/login';
import { Register } from './page/Register/Register';
import { Dashboard } from './page/user-dashboard/user-dashboard';
import { MainLayout } from './layout/main-layout/main-layout';

export const routes: Routes = [

  {
    path: '',
    component: Login
  },

  {
    path: 'register',
    component: Register
  },

  {
    path: '',
    component: MainLayout,
    children: [
      {
        path: 'dashboard',
        component: Dashboard
      },

      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      }
    ]
  },

  {
    path: '**',
    redirectTo: 'login'
  }
];