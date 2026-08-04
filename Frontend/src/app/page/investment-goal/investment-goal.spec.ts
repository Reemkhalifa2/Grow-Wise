import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvestmentGoal } from './investment-goal';

describe('InvestmentGoal', () => {
  let component: InvestmentGoal;
  let fixture: ComponentFixture<InvestmentGoal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvestmentGoal],
    }).compileComponents();

    fixture = TestBed.createComponent(InvestmentGoal);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
