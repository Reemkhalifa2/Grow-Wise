import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssetManagement } from './asset-management';

describe('AssetManagement', () => {
  let component: AssetManagement;
  let fixture: ComponentFixture<AssetManagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AssetManagement],
    }).compileComponents();

    fixture = TestBed.createComponent(AssetManagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
