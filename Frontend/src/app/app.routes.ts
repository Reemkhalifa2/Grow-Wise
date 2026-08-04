import { Routes } from '@angular/router';

import { Login } from './page/login/login';
import { Register } from './page/Register/Register';
import { UserDashboard } from './page/user-dashboard/user-dashboard';
import { FinancialProfile } from './page/financial-profile/financial-profile';
import { AdminDashboard } from './page/dashboard/dashboard';
import { MainLayout } from './layout/main-layout/main-layout';
import {AssetManagement} from './page/asset-management/asset-management';
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
        component: UserDashboard
      },
      {
        path: 'financial-profile',
        component: FinancialProfile
      },
      {
        path: 'admin-dashboard',
        component: AdminDashboard
      },
      {
        path: 'assets',
        component: AssetManagement
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
    redirectTo: ''
  }
];