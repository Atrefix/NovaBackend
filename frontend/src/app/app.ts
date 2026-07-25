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
}

