import { Routes } from '@angular/router';
import { Register } from './page/Register/Register'
import { Login} from './page/login/login';

export const routes: Routes = [
  {
    path: 'login',
    component: Login
  },
  {
    path: 'register',
    component: Register
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];

