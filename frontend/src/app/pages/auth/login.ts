import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NovaService } from '../../services/nova.service';
import { fadeInUp, fadeIn } from '../../shared/animations';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  animations: [fadeInUp, fadeIn]
})
export class Login {
  readonly nova = inject(NovaService);
  private readonly router = inject(Router);

  // 'login' | 'register' | 'recuperar' | 'cambiar-password'
  activeTab = signal<string>('login');

  // Input Fields
  username = signal<string>('');
  password = signal<string>('');
  
  registerUsername = signal<string>('');
  registerPassword = signal<string>('');
  registerConfirmPassword = signal<string>('');

  recoverEmail = signal<string>('');

  resetEmail = signal<string>('');
  resetToken = signal<string>('');
  resetNewPassword = signal<string>('');

  // Status Indicators
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);
  loading = signal<boolean>(false);

  clearMessages() {
    this.successMessage.set(null);
    this.errorMessage.set(null);
  }

  changeTab(tab: string) {
    this.activeTab.set(tab);
    this.clearMessages();
  }

  handleLogin() {
    if (!this.username() || !this.password()) {
      this.errorMessage.set('Por favor, ingresa tus credenciales.');
      return;
    }

    this.loading.set(true);
    this.clearMessages();

    this.nova.login(this.username(), this.password()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.nova.addNotification(`Bienvenido de nuevo, ${res.user?.alias || res.user?.username || 'Usuario'}`);
        if (this.nova.isAdmin()) {
          this.router.navigate(['/admin']);
        } else {
          this.router.navigate(['/client']);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.message || 'Error al iniciar sesión. Inténtalo de nuevo.');
      }
    });
  }

  handleRegister() {
    if (!this.registerUsername() || !this.registerPassword()) {
      this.errorMessage.set('Por favor, completa todos los campos.');
      return;
    }

    if (this.registerPassword() !== this.registerConfirmPassword()) {
      this.errorMessage.set('Las contraseñas no coinciden.');
      return;
    }

    this.loading.set(true);
    this.clearMessages();

    this.nova.register(this.registerUsername(), this.registerPassword()).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Registro exitoso. Ya puedes iniciar sesión.');
        this.username.set(this.registerUsername());
        this.password.set(this.registerPassword());
        setTimeout(() => {
          this.changeTab('login');
        }, 1500);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.message || 'Error al registrar usuario.');
      }
    });
  }

  handleRecover() {
    if (!this.recoverEmail()) {
      this.errorMessage.set('Por favor, ingresa tu correo electrónico.');
      return;
    }

    this.loading.set(true);
    this.clearMessages();

    this.nova.recuperarContrasena(this.recoverEmail()).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.successMessage.set('Token de recuperación enviado. Revisa tu bandeja.');
        this.resetEmail.set(this.recoverEmail());
        
        // Auto-fill token if in mock mode for faster testing
        if (this.nova.mockMode()) {
          this.resetToken.set('d1364738-e432-46f7-bdd2-3cd17aff81cf');
        }

        setTimeout(() => {
          this.changeTab('cambiar-password');
        }, 2000);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.message || 'Error al solicitar recuperación.');
      }
    });
  }

  handleResetPassword() {
    if (!this.resetEmail() || !this.resetToken() || !this.resetNewPassword()) {
      this.errorMessage.set('Por favor, completa todos los campos.');
      return;
    }

    this.loading.set(true);
    this.clearMessages();

    this.nova.cambiarContrasena(this.resetEmail(), this.resetToken(), this.resetNewPassword()).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Contraseña modificada correctamente. Redireccionando...');
        this.username.set(this.resetEmail());
        this.password.set(this.resetNewPassword());
        setTimeout(() => {
          this.changeTab('login');
        }, 2000);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.message || 'Error al cambiar contraseña. Verifica tu token.');
      }
    });
  }
}
