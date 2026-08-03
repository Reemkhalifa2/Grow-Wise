import { TestBed } from '@angular/core/testing';

import { UserDashboard } from './user-dashboard';

describe('UserDashboard', () => {
  let service: UserDashboard;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserDashboard);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
