import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FinancialGoal } from './investment-goal';

describe('InvestmentGoal', () => {
  let component: FinancialGoal;
  let fixture: ComponentFixture<FinancialGoal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FinancialGoal],
    }).compileComponents();

    fixture = TestBed.createComponent(FinancialGoal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
