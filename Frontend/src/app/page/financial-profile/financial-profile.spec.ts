import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinancialProfile } from './financial-profile';

describe('FinancialProfile', () => {
  let component: FinancialProfile;
  let fixture: ComponentFixture<FinancialProfile>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinancialProfile],
    }).compileComponents();

    fixture = TestBed.createComponent(FinancialProfile);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
