import { TestBed } from '@angular/core/testing';

import { PasswordGenerationService } from './password-generation.service';

describe('PasswordGenerationService', () => {
  let service: PasswordGenerationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PasswordGenerationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
