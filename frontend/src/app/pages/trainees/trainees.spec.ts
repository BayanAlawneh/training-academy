import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Trainees } from './trainees';

describe('Trainees', () => {
  let component: Trainees;
  let fixture: ComponentFixture<Trainees>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Trainees],
    }).compileComponents();

    fixture = TestBed.createComponent(Trainees);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
