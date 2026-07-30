import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TraineeDashboard } from './trainee-dashboard';

describe('TraineeDashboard', () => {
  let component: TraineeDashboard;
  let fixture: ComponentFixture<TraineeDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TraineeDashboard],
    }).compileComponents();

    fixture = TestBed.createComponent(TraineeDashboard);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
