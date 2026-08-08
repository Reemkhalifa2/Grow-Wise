import { Routes } from '@angular/router';

import { Login } from './page/login/login';
import { Register } from './page/Register/Register';
import { ForgotPassword } from './page/forgot-password/forgot-password';
import { ResetPassword } from './page/reset-password/reset-password';
import { UserDashboard } from './page/user-dashboard/user-dashboard';
import { FinancialProfile } from './page/financial-profile/financial-profile';
import { AdminDashboard } from './page/dashboard/dashboard';
import { MainLayout } from './layout/main-layout/main-layout';
import { AssetManagement } from './page/asset-management/asset-management';
import { FinancialGoal } from './page/investment-goal/investment-goal';
import { authGuard } from './authGuard'; // <-- Add this
import {TaskComponent} from './page/task/task.component';
import {InvestmentPlan} from './page/investment-plan/investment-plan';
import {Portfolio} from './page/portfolio/portfolio';

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
    path: 'forgot-password',
    component: ForgotPassword
  },
  {
    path: 'reset-password',
    component: ResetPassword
  },

  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard], // <-- Protect all child routes
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
        path: 'financial-goal',
        component: FinancialGoal
      },
      {
        path: 'investment-plan',
        component: InvestmentPlan
      },
      {
        path: 'to-do',
        component: TaskComponent
      },
      {
        path: 'portfolio',
        component: Portfolio
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