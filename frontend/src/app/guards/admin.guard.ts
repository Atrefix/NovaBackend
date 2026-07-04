import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { NovaService } from '../services/nova.service';

export const adminGuard = () => {
  const router = inject(Router);
  const novaService = inject(NovaService);

  if (novaService.isAuthenticated() && novaService.isAdmin()) {
    return true;
  }

  if (novaService.isAuthenticated()) {
    router.navigate(['/client']);
  } else {
    router.navigate(['/login']);
  }
  return false;
};
