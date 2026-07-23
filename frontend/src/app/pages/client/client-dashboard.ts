import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NovaService, Product, Purchase, Review } from '../../services/nova.service';
import { fadeInUp, fadeIn, slideInOut } from '../../shared/animations';

interface CartItem {
  product: Product;
  quantity: number;
  instructions: string;
}

interface ChatMessage {
  sender: 'user' | 'ai';
  text: string;
  time: Date;
}

@Component({
  selector: 'app-client-dashboard',
  imports: [FormsModule, CommonModule],
  templateUrl: './client-dashboard.html',
  animations: [fadeInUp, fadeIn, slideInOut]
})
export class ClientDashboard implements OnInit {
  readonly nova = inject(NovaService);
  private readonly router = inject(Router);

  currentTab = signal<string>('catalog');

  products = signal<Product[]>([]);
  myPurchases = signal<Purchase[]>([]);

  cart = signal<CartItem[]>([]);
  showCartPanel = signal<boolean>(false);
  cityDelivery = signal<string>('Lima');
  addressDelivery = signal<string>('Ancon');

  memberReviewBody = signal<string>('');
  memberReviewRating = signal<number>(5);
  reviewSuccess = signal<boolean>(false);

  aiMode = signal<'Support' | 'recommendations'>('Support');
  chatInput = signal<string>('');
  chatMessages = signal<ChatMessage[]>([
    { sender: 'ai', text: 'Hola, soy el Asistente Inteligente de Nova Cameras. ¿En qué puedo ayudarte hoy?', time: new Date() }
  ]);
  aiLoading = signal<boolean>(false);

  profileAlias = signal<string>('');
  profileDireccion = signal<string>('');
  profilePais = signal<string>('');
  profileBirthDate = signal<string>('');
  profileSuccess = signal<boolean>(false);

  infoMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.loadCatalog();
    this.loadPurchases();
    this.initializeProfileForm();
  }

  loadCatalog() {
    this.nova.getProducts().subscribe(prods => {

      this.products.set(prods.filter(p => p.tipoProducto === 'PRODUCTO_TERMINADO'));
    });
  }

  loadPurchases() {
    this.nova.getPurchases().subscribe(purchases => {
      this.myPurchases.set(purchases);
    });
  }

  initializeProfileForm() {
    const user = this.nova.currentUser();
    if (user) {
      this.profileAlias.set(user.alias || '');
      this.profileDireccion.set(user.direccion || '');
      this.profilePais.set(user.pais || '');
      this.profileBirthDate.set(user.fechaNacimiento || '');
    }
  }

  changeTab(tab: string) {
    this.currentTab.set(tab);
    this.clearMessages();
  }

  clearMessages() {
    this.infoMessage.set(null);
    this.errorMessage.set(null);
    this.profileSuccess.set(false);
    this.reviewSuccess.set(false);
  }

  addToCart(prod: Product) {
    if (prod.stock <= 0) {
      this.errorMessage.set('El producto seleccionado no cuenta con stock en este momento.');
      setTimeout(() => this.errorMessage.set(null), 3000);
      return;
    }

    this.cart.update(current => {
      const exists = current.find(item => item.product.idproducto === prod.idproducto);
      if (exists) {

        const newQty = Math.min(exists.quantity + 1, prod.stock);
        if (newQty === exists.quantity) {
          this.errorMessage.set('No puedes agregar más de la cantidad disponible en almacén.');
          setTimeout(() => this.errorMessage.set(null), 3000);
        }
        return current.map(item =>
          item.product.idproducto === prod.idproducto
            ? { ...item, quantity: newQty }
            : item
        );
      } else {
        return [...current, { product: prod, quantity: 1, instructions: '' }];
      }
    });
    this.showCartPanel.set(true);
  }

  updateQuantity(prodId: number, qty: number) {
    this.cart.update(current =>
      current.map(item => {
        if (item.product.idproducto === prodId) {
          const validQty = Math.max(1, Math.min(qty, item.product.stock));
          return { ...item, quantity: validQty };
        }
        return item;
      })
    );
  }

  removeFromCart(prodId: number) {
    this.cart.update(current => current.filter(item => item.product.idproducto !== prodId));
  }

  getCartTotal() {
    return this.cart().reduce((sum, item) => sum + (item.product.precioventa * item.quantity), 0);
  }

  executePurchase() {
    if (this.cart().length === 0) return;
    if (!this.cityDelivery() || !this.addressDelivery()) {
      alert('Por favor especifica la ciudad y dirección de envío.');
      return;
    }

    const payload = {
      montoProcesado: this.getCartTotal(),
      productos: this.cart().map(item => ({
        idProducto: item.product.idproducto,
        quantity: item.quantity,
        instructions: item.instructions || undefined
      })),
      cityDelivery: this.cityDelivery(),
      addressDelivery: this.addressDelivery()
    };

    this.nova.newPurchase(payload).subscribe({
      next: () => {
        this.cart.set([]);
        this.showCartPanel.set(false);
        this.infoMessage.set('Compra procesada exitosamente. Se ha descontado del stock de producción.');
        this.loadCatalog()
        this.loadPurchases()
        setTimeout(() => this.infoMessage.set(null), 5000);
      },
      error: (err) => {
        alert(err.message || 'Error en el checkout. Stock insuficiente.');
      }
    });
  }

  saveProfile() {
    this.nova.updateProfile(
      this.profileAlias(),
      this.profileDireccion(),
      this.profilePais(),
      this.profileBirthDate()
    ).subscribe({
      next: () => {
        this.profileSuccess.set(true);
        this.nova.addNotification('Perfil actualizado correctamente.');
      },
      error: (err) => {
        alert('Error al guardar perfil: ' + err.message);
      }
    });
  }

  sendMemberReview() {
    if (!this.memberReviewBody()) return;
    this.nova.addReview(this.memberReviewBody(), this.memberReviewRating()).subscribe({
      next: () => {
        this.reviewSuccess.set(true);
        this.memberReviewBody.set('');
        this.memberReviewRating.set(5);
        this.nova.addNotification('Nueva opinión publicada.');
      }
    });
  }

  setRating(rating: number) {
    this.memberReviewRating.set(rating);
  }

  sendChatMessage() {
    const prompt = this.chatInput().trim();
    if (!prompt) return;


    this.chatMessages.update(msgs => [...msgs, { sender: 'user', text: prompt, time: new Date() }]);
    this.chatInput.set('');
    this.aiLoading.set(true);

    this.nova.consultarIA(prompt, this.aiMode()).subscribe({
      next: (res) => {
        this.aiLoading.set(false);
        this.chatMessages.update(msgs => [...msgs, { sender: 'ai', text: res.response, time: new Date() }]);
      },
      error: () => {
        this.aiLoading.set(false);
        this.chatMessages.update(msgs => [...msgs, { sender: 'ai', text: 'Error al contactar a la inteligencia artificial de Nova.', time: new Date() }]);
      }
    });
  }

  logout() {
    this.nova.logout().subscribe(() => {
      this.router.navigate(['/']);
    });
  }
}
