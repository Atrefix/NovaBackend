import { Component, inject, signal, effect, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { NovaService } from './services/nova.service';
import { pageTransitions } from './shared/animations';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
  animations: [pageTransitions]
})
export class App implements OnInit {
  readonly nova = inject(NovaService);
  private readonly router = inject(Router);

  recentNotification = signal<string | null>(null);
  showQuickAccess = signal<boolean>(false);

  constructor() {
    effect(() => {
      this.nova.getNotificationsList().subscribe({
        next: (list) => {
          if (list.length > 0 && !list[0].leido) {
            this.recentNotification.set(list[0].cuerpo);
            setTimeout(() => {
              this.recentNotification.set(null);
            }, 5000);
          }
        }
      });
    });
  }

  ngOnInit() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      window.scrollTo(0, 0);
    });
  }

  prepareRoute(outlet: RouterOutlet) {
    return outlet && outlet.activatedRouteData && outlet.activatedRouteData['animation'];
  }

  toggleMock() {
    this.nova.mockMode.update(val => !val);
    const modeStr = this.nova.mockMode() ? 'Simulado' : 'Live API (Conectado a puerto 8080)';
    this.nova.addNotification(`Modo de datos cambiado a: ${modeStr}`);
  }

  quickLogin(role: 'admin' | 'client') {
    const email = role === 'admin' ? 'gaylin773@gmail.com' : 'testuser@gmail.com';
    this.nova.login(email, 'prueba123').subscribe({
      next: () => {
        this.showQuickAccess.set(false);
        if (role === 'admin') {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/client']);
        }
      },
      error: (err) => {
        alert(err.message);
      }
    });
  }

  logout() {
    this.nova.logout().subscribe({
      next: () => {
        this.router.navigate(['/']);
      }
    });
  }
}
