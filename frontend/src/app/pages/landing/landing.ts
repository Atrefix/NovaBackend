import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NovaService, Review, Product } from '../../services/nova.service';
import { fadeInUp, fadeIn } from '../../shared/animations';

@Component({
  selector: 'app-landing',
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './landing.html',
  animations: [fadeInUp, fadeIn]
})
export class Landing implements OnInit {
  readonly nova = inject(NovaService);
  private readonly router = inject(Router);

  reviews = signal<Review[]>([]);
  finishedCameras = signal<Product[]>([]);

  guestName = signal<string>('');
  guestEmail = signal<string>('');
  guestBody = signal<string>('');
  guestRating = signal<number>(5);

  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.nova.getReviews().subscribe(revs => {
      this.reviews.set(revs);
    });

    this.nova.getProducts().subscribe(prods => {

      this.finishedCameras.set(prods.filter(p => p.tipoProducto === 'PRODUCTO_TERMINADO'));
    });
  }

  submitGuestReview() {
    if (!this.guestName() || !this.guestEmail() || !this.guestBody()) {
      this.errorMessage.set('Por favor, completa todos los campos.');
      return;
    }

    this.nova.addReviewGuest(
      this.guestName(),
      this.guestEmail(),
      this.guestBody(),
      this.guestRating()
    ).subscribe({
      next: () => {
        this.successMessage.set('¡Reseña de invitado enviada con éxito!');
        this.errorMessage.set(null);
        this.guestName.set('');
        this.guestEmail.set('');
        this.guestBody.set('');
        this.guestRating.set(5);
        this.loadData()
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.errorMessage.set(err.message || 'Error al enviar reseña.');
      }
    });
  }

  setRating(rating: number) {
    this.guestRating.set(rating);
  }

  goToDashboard() {
    if (this.nova.isAuthenticated()) {
      if (this.nova.isAdmin()) {
        this.router.navigate(['/admin']);
      } else {
        this.router.navigate(['/client']);
      }
    } else {
      this.router.navigate(['/login']);
    }
  }
}
