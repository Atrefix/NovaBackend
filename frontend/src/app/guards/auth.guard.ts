import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { NovaService } from '../services/nova.service';

export const authGuard = () => {
  const router = inject(Router);
  const novaService = inject(NovaService);

  if (novaService.isAuthenticated()) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};
