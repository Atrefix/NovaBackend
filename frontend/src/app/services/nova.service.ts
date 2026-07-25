import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { tap, catchError, map } from 'rxjs/operators';

export interface Product {
  idproducto: number;
  codproducto: string;
  nombre: string;
  categoria: string;
  stock: number;
  preciocompra: number;
  precioventa: number;
  fechavencimiento: string;
  tipoProducto: 'MATERIA_PRIMA' | 'PRODUCTO_TERMINADO';
}

export interface Branch {
  idsucursal: number;
  nombre: string;
  direccion: string;
}

export interface RecipeDetail {
  ingredient: { idproducto: number };
  quantityRequired: number;
}

export interface Recipe {
  idreceta?: number;
  nombre: string;
  descripcion: string;
  product: { idproducto: number };
  details: RecipeDetail[];
}

export interface ProductionOrder {
  idorden: number;
  product: { idproducto: number };
  quantity: number;
  branch: { idsucursal: number };
  estado: 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO';
  fechaCreacion: string;
  fechaInicio?: string;
  fechaFin?: string;
}

export interface Account {
  idcuenta: number;
  username: string;
  alias?: string;
  direccion?: string;
  pais?: string;
  fechaNacimiento?: string;
  rol: 'USER' | 'ADMIN';
}

export interface LoginUserData {
  token: string;
  email: string;
  rol: 'USER' | 'ADMIN';
  estado: string;
  fechaRegistro: string;
  alias?: string;
  direccion?: string;
  pais?: string;
  fechaNacimiento?: string;
  telefono?: string | null;
}

export interface LoginResponse {
  loginData: LoginUserData;
}

export interface Review {
  idreview: number;
  nombre?: string;
  email?: string;
  cuerpo: string;
  puntuacion: number;
  fecha: string;
  tipo: 'GUEST' | 'MEMBER';
}

export interface PurchaseItem {
  idProducto: number;
  quantity: number;
  instructions?: string;
}

export interface Purchase {
  idcompra: number;
  montoProcesado: number;
  productos: PurchaseItem[];
  cityDelivery: string;
  addressDelivery: string;
  fecha: string;
  username: string;
}

export interface Notification {
  id: number;
  cuerpo: string;
  fecha: string;
  leido: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class NovaService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = 'http://localhost:8080';

  mockMode = signal<boolean>(true);

  token = signal<string | null>(localStorage.getItem('token'));
  currentUser = signal<Account | null>(null);
  isAuthenticated = computed(() => this.token() !== null);
  isAdmin = computed(() => this.currentUser()?.rol === 'ADMIN');

  private mockProducts = signal<Product[]>([
    {
      idproducto: 1,
      codproducto: 'RAW001',
      nombre: 'Sensor CMOS Full Frame 45MP',
      categoria: 'Sensores',
      stock: 50,
      preciocompra: 150.0,
      precioventa: 0.0,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'MATERIA_PRIMA',
    },
    {
      idproducto: 2,
      codproducto: 'RAW002',
      nombre: 'Grupo Óptico Prime 50mm f/1.2',
      categoria: 'Lentes',
      stock: 80,
      preciocompra: 200.0,
      precioventa: 0.0,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'MATERIA_PRIMA',
    },
    {
      idproducto: 3,
      codproducto: 'RAW003',
      nombre: 'Procesador Nova Engine X1',
      categoria: 'Procesadores',
      stock: 40,
      preciocompra: 85.0,
      precioventa: 0.0,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'MATERIA_PRIMA',
    },
    {
      idproducto: 4,
      codproducto: 'RAW004',
      nombre: 'Chasis de Aleación de Magnesio',
      categoria: 'Chasis',
      stock: 35,
      preciocompra: 120.0,
      precioventa: 0.0,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'MATERIA_PRIMA',
    },
    {
      idproducto: 5,
      codproducto: 'RAW005',
      nombre: 'Pantalla Táctil Articulada LCD 3.2\"',
      categoria: 'Pantallas',
      stock: 60,
      preciocompra: 45.0,
      precioventa: 0.0,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'MATERIA_PRIMA',
    },
    {
      idproducto: 6,
      codproducto: 'RAW006',
      nombre: 'Batería Inteligente NP-W235',
      categoria: 'Baterías',
      stock: 120,
      preciocompra: 20.0,
      precioventa: 0.0,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'MATERIA_PRIMA',
    },
    {
      idproducto: 7,
      codproducto: 'RAW007',
      nombre: 'Módulo Obturador Mecánico',
      categoria: 'Obturadores',
      stock: 55,
      preciocompra: 60.0,
      precioventa: 0.0,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'MATERIA_PRIMA',
    },

    {
      idproducto: 8,
      codproducto: 'PROD001',
      nombre: 'Nova Alpha I Pro DSLR',
      categoria: 'DSLR',
      stock: 8,
      preciocompra: 635.0,
      precioventa: 1299.99,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'PRODUCTO_TERMINADO',
    },
    {
      idproducto: 9,
      codproducto: 'PROD002',
      nombre: 'Nova Prism X Mirrorless',
      categoria: 'Mirrorless',
      stock: 5,
      preciocompra: 600.0,
      precioventa: 1899.99,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'PRODUCTO_TERMINADO',
    },
    {
      idproducto: 10,
      codproducto: 'PROD003',
      nombre: 'Nova Veloce 4K Action Cam',
      categoria: 'Deportiva',
      stock: 25,
      preciocompra: 150.0,
      precioventa: 399.99,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'PRODUCTO_TERMINADO',
    },
    {
      idproducto: 11,
      codproducto: 'PROD004',
      nombre: 'Nova Oculus Smart Dome',
      categoria: 'Seguridad',
      stock: 40,
      preciocompra: 50.0,
      precioventa: 149.99,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'PRODUCTO_TERMINADO',
    },
    {
      idproducto: 12,
      codproducto: 'PROD005',
      nombre: 'Nova CineMax 8K Cine Rig',
      categoria: 'Cine',
      stock: 2,
      preciocompra: 1800.0,
      precioventa: 4999.99,
      fechavencimiento: '2027-12-31T00:00:00',
      tipoProducto: 'PRODUCTO_TERMINADO',
    },
  ]);

  private mockBranches = signal<Branch[]>([
    {
      idsucursal: 1,
      nombre: 'Planta de Ensamblaje Central (Lima)',
      direccion: 'Av. Industrial 1200, Ate, Lima',
    },
    {
      idsucursal: 2,
      nombre: 'Planta Tecnológica Norte (Trujillo)',
      direccion: 'Zona Industrial Moche, Trujillo',
    },
    {
      idsucursal: 3,
      nombre: 'Centro Logístico Sur (Arequipa)',
      direccion: 'Vía Evitamiento Km 5, Arequipa',
    },
  ]);

  private mockRecipes = signal<Recipe[]>([
    {
      idreceta: 1,
      nombre: 'Receta Estándar Alpha I DSLR',
      descripcion: 'Ensamblaje premium para cámara DSLR Nova Alpha I',
      product: { idproducto: 8 },
      details: [
        { ingredient: { idproducto: 1 }, quantityRequired: 1 },
        { ingredient: { idproducto: 2 }, quantityRequired: 1 },
        { ingredient: { idproducto: 3 }, quantityRequired: 1 },
        { ingredient: { idproducto: 4 }, quantityRequired: 1 },
        { ingredient: { idproducto: 5 }, quantityRequired: 1 },
        { ingredient: { idproducto: 6 }, quantityRequired: 1 },
        { ingredient: { idproducto: 7 }, quantityRequired: 1 },
      ],
    },
    {
      idreceta: 2,
      nombre: 'Receta Estándar Prism X Mirrorless',
      descripcion: 'Ensamblaje sin espejo de alta velocidad y procesador de IA',
      product: { idproducto: 9 },
      details: [
        { ingredient: { idproducto: 1 }, quantityRequired: 1 },
        { ingredient: { idproducto: 3 }, quantityRequired: 1 },
        { ingredient: { idproducto: 4 }, quantityRequired: 1 },
        { ingredient: { idproducto: 5 }, quantityRequired: 1 },
        { ingredient: { idproducto: 6 }, quantityRequired: 1 },
      ],
    },
  ]);

  private mockProductionOrders = signal<ProductionOrder[]>([
    {
      idorden: 1,
      product: { idproducto: 8 },
      quantity: 5,
      branch: { idsucursal: 1 },
      estado: 'PENDIENTE',
      fechaCreacion: new Date(Date.now() - 86400000).toISOString(),
    },
    {
      idorden: 2,
      product: { idproducto: 9 },
      quantity: 3,
      branch: { idsucursal: 2 },
      estado: 'EN_PROCESO',
      fechaCreacion: new Date(Date.now() - 43200000).toISOString(),
      fechaInicio: new Date(Date.now() - 36000000).toISOString(),
    },
    {
      idorden: 3,
      product: { idproducto: 10 },
      quantity: 10,
      branch: { idsucursal: 1 },
      estado: 'COMPLETADO',
      fechaCreacion: new Date(Date.now() - 172800000).toISOString(),
      fechaInicio: new Date(Date.now() - 150000000).toISOString(),
      fechaFin: new Date(Date.now() - 120000000).toISOString(),
    },
  ]);

  private mockAccounts = signal<Account[]>([
    {
      idcuenta: 1,
      username: 'gaylin773@gmail.com',
      alias: 'Aylin (Jefe de Planta)',
      direccion: 'Av. Principal 123',
      pais: 'Perú',
      fechaNacimiento: '1990-05-12',
      rol: 'ADMIN',
    },
    {
      idcuenta: 2,
      username: 'testuser@gmail.com',
      alias: 'Juan Pérez',
      direccion: 'Calle Las Flores 456',
      pais: 'Perú',
      fechaNacimiento: '1995-08-20',
      rol: 'USER',
    },
    {
      idcuenta: 3,
      username: 'angelolma2080@gmail.com',
      alias: 'Angelo L.',
      direccion: 'Av. Aviación 789',
      pais: 'Perú',
      fechaNacimiento: '1988-11-03',
      rol: 'USER',
    },
  ]);

  private mockReviews = signal<Review[]>([
    {
      idreview: 1,
      nombre: 'Angelo L.',
      email: 'angelolma2080@gmail.com',
      cuerpo: 'Excelente cámara Mirrorless, la definición del sensor CMOS 45MP es fantástica.',
      puntuacion: 5,
      fecha: new Date(Date.now() - 86400000 * 3).toISOString(),
      tipo: 'MEMBER',
    },
    {
      idreview: 2,
      nombre: 'Carlos G.',
      email: 'guest1@gmail.com',
      cuerpo:
        'Muy buen soporte del equipo técnico, compré una cámara de seguridad Oculus y la configuración fue muy rápida.',
      puntuacion: 4,
      fecha: new Date(Date.now() - 86400000 * 2).toISOString(),
      tipo: 'GUEST',
    },
    {
      idreview: 3,
      nombre: 'Aylin (Jefe de Planta)',
      email: 'gaylin773@gmail.com',
      cuerpo:
        'Lote L-PROD001-01 aprobado. Densidad y respuesta del sensor conformes con los estándares de control óptico.',
      puntuacion: 5,
      fecha: new Date(Date.now() - 86400000).toISOString(),
      tipo: 'MEMBER',
    },
  ]);

  private mockPurchases = signal<Purchase[]>([
    {
      idcompra: 1,
      montoProcesado: 1449.98,
      productos: [
        { idProducto: 8, quantity: 1, instructions: 'Ensamblado con protector de pantalla' },
        { idProducto: 11, quantity: 1 },
      ],
      cityDelivery: 'Lima',
      addressDelivery: 'Ancon',
      fecha: new Date(Date.now() - 86400000).toISOString(),
      username: 'testuser@gmail.com',
    },
  ]);

  private mockNotifications = signal<Notification[]>([
    {
      id: 1,
      cuerpo:
        'Alerta: El stock de Sensor CMOS 45MP ha bajado del mínimo recomendado (50 unidades).',
      fecha: new Date(Date.now() - 7200000).toISOString(),
      leido: false,
    },
    {
      id: 2,
      cuerpo: 'Orden de Producción #3 Completada con éxito en Planta de Ensamblaje Central (Lima).',
      fecha: new Date(Date.now() - 120000000).toISOString(),
      leido: true,
    },
  ]);

  private passwords: Record<string, string> = {};

  constructor() {
    this.loadFromStorage();
  }

  private loadFromStorage(): void {
    try {
      const savedPasswords = localStorage.getItem('nova_passwords');
      if (savedPasswords) {
        this.passwords = JSON.parse(savedPasswords);
      } else {
        this.passwords = {
          'gaylin773@gmail.com': 'prueba123',
          'testuser@gmail.com': 'prueba123',
          'angelolma2080@gmail.com': 'prueba123',
        };
        localStorage.setItem('nova_passwords', JSON.stringify(this.passwords));
      }

      this.loadCollection('nova_products', this.mockProducts);
      this.loadCollection('nova_branches', this.mockBranches);
      this.loadCollection('nova_recipes', this.mockRecipes);
      this.loadCollection('nova_production_orders', this.mockProductionOrders);
      this.loadCollection('nova_accounts', this.mockAccounts);
      this.loadCollection('nova_reviews', this.mockReviews);
      this.loadCollection('nova_purchases', this.mockPurchases);
      this.loadCollection('nova_notifications', this.mockNotifications);

      const savedUser = localStorage.getItem('nova_current_user');
      if (savedUser && this.token()) {
        this.currentUser.set(JSON.parse(savedUser));
      } else if (!this.token()) {
        localStorage.removeItem('nova_current_user');
      }
    } catch (e) {
      console.warn('Nova: Error al restaurar datos desde localStorage:', e);
    }
  }

  private loadCollection(key: string, sig: { set: (value: any) => void }): void {
    const saved = localStorage.getItem(key);
    if (saved) {
      sig.set(JSON.parse(saved));
    }
  }

  private persistAll(): void {
    try {
      localStorage.setItem('nova_passwords', JSON.stringify(this.passwords));
      localStorage.setItem('nova_products', JSON.stringify(this.mockProducts()));
      localStorage.setItem('nova_branches', JSON.stringify(this.mockBranches()));
      localStorage.setItem('nova_recipes', JSON.stringify(this.mockRecipes()));
      localStorage.setItem('nova_production_orders', JSON.stringify(this.mockProductionOrders()));
      localStorage.setItem('nova_accounts', JSON.stringify(this.mockAccounts()));
      localStorage.setItem('nova_reviews', JSON.stringify(this.mockReviews()));
      localStorage.setItem('nova_purchases', JSON.stringify(this.mockPurchases()));
      localStorage.setItem('nova_notifications', JSON.stringify(this.mockNotifications()));
    } catch (e) {
      console.warn('Nova: Error al persistir datos:', e);
    }
  }

  private persistUser(): void {
    const user = this.currentUser();
    if (user) {
      localStorage.setItem('nova_current_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('nova_current_user');
    }
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${this.token() || ''}`,
    });
  }

  login(username: string, password: string): Observable<LoginResponse> {
    if (this.mockMode()) {
      const user = this.mockAccounts().find((a) => a.username === username);
      if (user && this.passwords[username] === password) {
        const mockToken = 'mock-jwt-token-for-' + user.username;
        this.token.set(mockToken);
        localStorage.setItem('token', mockToken);
        this.currentUser.set(user);
        this.persistUser();
        this.addNotification(`Sesión iniciada como ${user.alias || user.username}`);
        return of({
          loginData: {
            token: mockToken,
            email: user.username,
            rol: user.rol,
            estado: 'ACTIVO',
            fechaRegistro: new Date().toISOString(),
            alias: user.alias,
            direccion: user.direccion,
            pais: user.pais,
            fechaNacimiento: user.fechaNacimiento,
            telefono: null
          }
        });
      }
      return throwError(() => new Error('Credenciales inválidas. Verifica tu email y contraseña.'));
    }

    return this.http.post<LoginResponse>(`${this.apiBaseUrl}/auth/login`, { username, password }).pipe(
      tap((res) => {
        const data = res.loginData;

        this.token.set(data.token);
        localStorage.setItem('token', data.token);

        const account: Account = {
          idcuenta: 0,
          username: data.email,
          alias: data.alias || data.email.split('@')[0],
          direccion: data.direccion,
          pais: data.pais,
          fechaNacimiento: data.fechaNacimiento,
          rol: data.rol
        };

        this.currentUser.set(account);
        this.addNotification(`Sesión iniciada como ${account.alias}`);
      })
    );
  }

  register(username: string, password: 'prueba123' | string): Observable<any> {
    if (this.mockMode()) {
      const exists = this.mockAccounts().find((a) => a.username === username);
      if (exists) {
        return throwError(() => new Error('El usuario ya existe.'));
      }
      const newAcc: Account = {
        idcuenta: this.mockAccounts().length + 1,
        username,
        alias: username.split('@')[0],
        rol: 'USER',
      };
      this.mockAccounts.update((prev) => [...prev, newAcc]);
      this.passwords[username] = password;
      this.persistAll();
      return of({ message: 'Usuario registrado exitosamente', user: newAcc });
    }

    return this.http.post<any>(`${this.apiBaseUrl}/auth/register`, { username, password });
  }

  logout(): Observable<any> {
    const handleLogout = () => {
      this.token.set(null);
      this.currentUser.set(null);
      localStorage.removeItem('token');
      this.persistUser();
    };

    if (this.mockMode()) {
      handleLogout();
      return of({ message: 'Sesión cerrada' });
    }

    return this.http
      .post<any>(`${this.apiBaseUrl}/auth/logout`, {}, { headers: this.getHeaders() })
      .pipe(
        tap(handleLogout),
        catchError((err) => {
          handleLogout();
          return throwError(() => err);
        }),
      );
  }

  recuperarContrasena(email: string): Observable<any> {
    if (this.mockMode()) {
      return of({ message: 'Token de recuperación enviado a ' + email });
    }
    return this.http.post<any>(`${this.apiBaseUrl}/auth/recuperar`, { email });
  }

  cambiarContrasena(
    email: string,
    token: string,
    nuevaPassword: 'prueba123' | string,
  ): Observable<any> {
    if (this.mockMode()) {
      this.passwords[email] = nuevaPassword;
      this.persistAll();
      return of({ message: 'Contraseña cambiada exitosamente para ' + email });
    }
    return this.http.post<any>(`${this.apiBaseUrl}/auth/cambiar-password`, {
      email,
      token,
      nuevaPassword,
    });
  }

  protectedTest(): Observable<any> {
    if (this.mockMode()) {
      return of({
        message: 'Acceso autorizado en modo simulación para ' + this.currentUser()?.username,
      });
    }
    return this.http.get<any>(`${this.apiBaseUrl}/auth/protectedTest`, {
      headers: this.getHeaders(),
    });
  }

  getProducts(): Observable<Product[]> {
    if (this.mockMode()) {
      return of(this.mockProducts());
    }
    return this.http.get<Product[]>(`${this.apiBaseUrl}/products/getAllProducts`);
  }

  addProduct(prod: Omit<Product, 'idproducto'>): Observable<Product> {
    if (this.mockMode()) {
      const newProd: Product = {
        ...prod,
        idproducto: Math.max(...this.mockProducts().map((p) => p.idproducto), 0) + 1,
      };
      this.mockProducts.update((prev) => [...prev, newProd]);
      this.addNotification(`Producto agregado: ${newProd.nombre} (${newProd.codproducto})`);
      return of(newProd);
    }

    return this.http.post<Product>(`${this.apiBaseUrl}/products/agregar`, prod, {
      headers: this.getHeaders(),
    });
  }

  deleteProduct(id: number): Observable<any> {
    if (this.mockMode()) {
      this.mockProducts.update((prev) => prev.filter((p) => p.idproducto !== id));
      this.addNotification(`Producto ID #${id} eliminado del catálogo`);
      return of({ message: 'Producto eliminado' });
    }

    return this.http.delete<any>(`${this.apiBaseUrl}/products/eliminar/${id}`, {
      headers: this.getHeaders(),
    });
  }

  modifyProduct(id: number, patch: Partial<Product>): Observable<Product> {
    if (this.mockMode()) {
      let updated: Product | null = null;
      this.mockProducts.update((prev) =>
        prev.map((p) => {
          if (p.idproducto === id) {
            updated = { ...p, ...patch };
            return updated;
          }
          return p;
        }),
      );
      if (!updated) return throwError(() => new Error('Producto no encontrado'));
      this.addNotification(`Producto ID #${id} modificado: ${patch.nombre || ''}`);
      return of(updated as any);
    }

    return this.http.patch<Product>(`${this.apiBaseUrl}/products/modificar/${id}`, patch, {
      headers: this.getHeaders(),
    });
  }

  getProductReport(): Observable<string> {
    if (this.mockMode()) {
      const report = `REPORTE DE INVENTARIO NOVA CAMERAS\nGenerado: ${new Date().toISOString()}\nTotal Componentes: ${this.mockProducts().filter((p) => p.tipoProducto === 'MATERIA_PRIMA').length}\nTotal Cámaras: ${this.mockProducts().filter((p) => p.tipoProducto === 'PRODUCTO_TERMINADO').length}\nValorización de Inventario: $${this.mockProducts()
        .reduce(
          (sum, p) =>
            sum + p.stock * (p.tipoProducto === 'MATERIA_PRIMA' ? p.preciocompra : p.precioventa),
          0,
        )
        .toFixed(2)}`;
      return of(report);
    }
    return this.http.get(`${this.apiBaseUrl}/products/getReport`, {
      headers: this.getHeaders(),
      responseType: 'text',
    });
  }

  getBranches(): Observable<Branch[]> {
    if (this.mockMode()) {
      return of(this.mockBranches());
    }
    return this.http.get<Branch[]>(`${this.apiBaseUrl}/sucursales/listar`);
  }

  addBranch(branch: Omit<Branch, 'idsucursal'>): Observable<Branch> {
    if (this.mockMode()) {
      const newB: Branch = {
        ...branch,
        idsucursal: Math.max(...this.mockBranches().map((b) => b.idsucursal), 0) + 1,
      };
      this.mockBranches.update((prev) => [...prev, newB]);
      this.addNotification(`Sucursal agregada: ${newB.nombre}`);
      return of(newB);
    }
    return this.http.post<Branch>(`${this.apiBaseUrl}/sucursales/agregar`, branch, {
      headers: this.getHeaders(),
    });
  }

  deleteBranch(id: number): Observable<any> {
    if (this.mockMode()) {
      this.mockBranches.update((prev) => prev.filter((b) => b.idsucursal !== id));
      this.addNotification(`Sucursal ID #${id} eliminada`);
      return of({ message: 'Sucursal eliminada' });
    }
    return this.http.delete<any>(`${this.apiBaseUrl}/sucursales/eliminar/${id}`, {
      headers: this.getHeaders(),
    });
  }

  modifyBranch(id: number, patch: Partial<Branch>): Observable<Branch> {
    if (this.mockMode()) {
      let updated: Branch | null = null;
      this.mockBranches.update((prev) =>
        prev.map((b) => {
          if (b.idsucursal === id) {
            updated = { ...b, ...patch };
            return updated;
          }
          return b;
        }),
      );
      if (!updated) return throwError(() => new Error('Sucursal no encontrada'));
      this.persistAll();
      return of(updated as any);
    }
    return this.http.patch<Branch>(`${this.apiBaseUrl}/sucursales/modificar/${id}`, patch, {
      headers: this.getHeaders(),
    });
  }

  getBranchReport(): Observable<string> {
    if (this.mockMode()) {
      return of(
        `REPORTE DE SUCURSALES Y PLANTAS\nGenerado: ${new Date().toISOString()}\nTotal Plantas Operativas: ${this.mockBranches().length}`,
      );
    }
    return this.http.get(`${this.apiBaseUrl}/sucursales/getReport`, {
      headers: this.getHeaders(),
      responseType: 'text',
    });
  }

  getAccounts(): Observable<Account[]> {
    if (this.mockMode()) {
      return of(this.mockAccounts());
    }
    return this.http.get<Account[]>(`${this.apiBaseUrl}/accounts/listar`, {
      headers: this.getHeaders(),
    });
  }

  updateProfile(
    alias: string,
    direccion: string,
    pais: string,
    fechaNacimiento: string,
  ): Observable<Account> {
    if (this.mockMode()) {
      let updated: Account | null = null;
      this.mockAccounts.update((prev) =>
        prev.map((a) => {
          if (a.username === this.currentUser()?.username) {
            updated = { ...a, alias, direccion, pais, fechaNacimiento };
            return updated;
          }
          return a;
        }),
      );
      if (updated) {
        this.currentUser.set(updated);
        this.persistAll();
        this.persistUser();
        return of(updated as any);
      }
      return throwError(() => new Error('Usuario no autenticado'));
    }

    return this.http
      .put<Account>(
        `${this.apiBaseUrl}/accounts/update-profile`,
        { alias, direccion, pais, fechaNacimiento },
        { headers: this.getHeaders() },
      )
      .pipe(
        tap((updatedUser) => {
          this.currentUser.set(updatedUser);
        }),
      );
  }

  changeRole(idcuenta: number, rol: 'ADMIN' | 'USER'): Observable<any> {
    if (this.mockMode()) {
      this.mockAccounts.update((prev) =>
        prev.map((a) => {
          if (a.idcuenta === idcuenta) {
            const updated = { ...a, rol };
            if (this.currentUser()?.idcuenta === idcuenta) {
              this.currentUser.set(updated);
            }
            return updated;
          }
          return a;
        }),
      );
      this.persistUser();
      this.addNotification(`Rol de usuario ID #${idcuenta} cambiado a ${rol}`);
      return of({ message: 'Rol actualizado exitosamente' });
    }

    return this.http.patch<any>(
      `${this.apiBaseUrl}/accounts/changeRole`,
      { idcuenta, rol },
      { headers: this.getHeaders() },
    );
  }

  exportAccounts(): Observable<any> {
    if (this.mockMode()) {
      return of({ message: 'Excel de cuentas exportado con éxito a Descargas', downloadUrl: '#' });
    }
    return this.http.get<any>(`${this.apiBaseUrl}/accounts/export`, { headers: this.getHeaders() });
  }

  getAccountsReport(): Observable<string> {
    if (this.mockMode()) {
      return of(
        `REPORTE DE CUENTAS DE USUARIO\nGenerado: ${new Date().toISOString()}\nTotal Cuentas: ${this.mockAccounts().length}\nAdministradores: ${this.mockAccounts().filter((a) => a.rol === 'ADMIN').length}`,
      );
    }
    return this.http.get(`${this.apiBaseUrl}/accounts/getReport`, {
      headers: this.getHeaders(),
      responseType: 'text',
    });
  }

  getReviews(): Observable<Review[]> {
    if (this.mockMode()) {
      return of(this.mockReviews());
    }
    return this.http.get<Review[]>(`${this.apiBaseUrl}/reviews/getReviews`);
  }

  addReviewGuest(
    nombre: string,
    email: string,
    cuerpo: string,
    puntuacion: number,
  ): Observable<Review> {
    const newRev: Review = {
      idreview: Math.max(...this.mockReviews().map((r) => r.idreview), 0) + 1,
      nombre,
      email,
      cuerpo,
      puntuacion,
      fecha: new Date().toISOString(),
      tipo: 'GUEST',
    };

    if (this.mockMode()) {
      this.mockReviews.update((prev) => [newRev, ...prev]);
      this.addNotification(`Nueva reseña de invitado: ${nombre}`);
      return of(newRev);
    }

    return this.http.post<Review>(`${this.apiBaseUrl}/reviews/addReviewGuest`, {
      nombre,
      email,
      cuerpo,
      puntuacion,
    });
  }

  addReview(cuerpo: string, puntuacion: number): Observable<Review> {
    const user = this.currentUser();
    const newRev: Review = {
      idreview: Math.max(...this.mockReviews().map((r) => r.idreview), 0) + 1,
      nombre: user?.alias || user?.username || 'Usuario Autenticado',
      email: user?.username || '',
      cuerpo,
      puntuacion,
      fecha: new Date().toISOString(),
      tipo: 'MEMBER',
    };

    if (this.mockMode()) {
      this.mockReviews.update((prev) => [newRev, ...prev]);
      this.addNotification(`Nueva reseña de cliente: ${newRev.nombre}`);
      return of(newRev);
    }

    return this.http.post<Review>(
      `${this.apiBaseUrl}/reviews/addReview`,
      { cuerpo, puntuacion },
      { headers: this.getHeaders() },
    );
  }

  getPurchases(): Observable<Purchase[]> {
    if (this.mockMode()) {
      const user = this.currentUser();
      if (!user) return of([]);
      return of(this.mockPurchases().filter((p) => p.username === user.username));
    }
    return this.http.get<Purchase[]>(`${this.apiBaseUrl}/purchases/getPurchases`, {
      headers: this.getHeaders(),
    });
  }

  newPurchase(
    purchaseData: Omit<Purchase, 'idcompra' | 'fecha' | 'username'>,
  ): Observable<Purchase> {
    const user = this.currentUser();
    const newP: Purchase = {
      ...purchaseData,
      idcompra: Math.max(...this.mockPurchases().map((p) => p.idcompra), 0) + 1,
      fecha: new Date().toISOString(),
      username: user?.username || 'gaylin773@gmail.com',
    };

    if (this.mockMode()) {
      let stockSuccess = true;
      for (const item of purchaseData.productos) {
        const prod = this.mockProducts().find((p) => p.idproducto === item.idProducto);
        if (!prod || prod.stock < item.quantity) {
          stockSuccess = false;
        }
      }

      if (!stockSuccess) {
        return throwError(() => new Error('Stock insuficiente para concretar la compra.'));
      }

      this.mockProducts.update((prev) =>
        prev.map((p) => {
          const buyItem = purchaseData.productos.find((item) => item.idProducto === p.idproducto);
          if (buyItem) {
            return { ...p, stock: p.stock - buyItem.quantity };
          }
          return p;
        }),
      );

      this.mockPurchases.update((prev) => [newP, ...prev]);
      this.addNotification(`Compra realizada con éxito ($${purchaseData.montoProcesado})`);
      return of(newP);
    }

    return this.http.post<Purchase>(`${this.apiBaseUrl}/purchases/newPurchase`, purchaseData, {
      headers: this.getHeaders(),
    });
  }

  getRecipes(): Observable<Recipe[]> {
    if (this.mockMode()) {
      return of(this.mockRecipes());
    }
    return this.http.get<Recipe[]>(`${this.apiBaseUrl}/recipes/getAll`, {
      headers: this.getHeaders(),
    });
  }

  createRecipe(recipe: Recipe): Observable<Recipe> {
    if (this.mockMode()) {
      const newRec: Recipe = {
        ...recipe,
        idreceta: Math.max(...this.mockRecipes().map((r) => r.idreceta || 0), 0) + 1,
      };
      this.mockRecipes.update((prev) => [...prev, newRec]);
      this.addNotification(`Receta de fabricación creada: ${newRec.nombre}`);
      return of(newRec);
    }
    return this.http.post<Recipe>(`${this.apiBaseUrl}/recipes/create`, recipe, {
      headers: this.getHeaders(),
    });
  }

  getProductionOrders(): Observable<ProductionOrder[]> {
    if (this.mockMode()) {
      return of(this.mockProductionOrders());
    }
    return this.http.get<ProductionOrder[]>(`${this.apiBaseUrl}/production-orders/getAll`, {
      headers: this.getHeaders(),
    });
  }

  newProductionOrder(
    productId: number,
    quantity: number,
    branchId: number,
  ): Observable<ProductionOrder> {
    const newOrd: ProductionOrder = {
      idorden: Math.max(...this.mockProductionOrders().map((o) => o.idorden), 0) + 1,
      product: { idproducto: productId },
      quantity,
      branch: { idsucursal: branchId },
      estado: 'PENDIENTE',
      fechaCreacion: new Date().toISOString(),
    };

    if (this.mockMode()) {
      this.mockProductionOrders.update((prev) => [...prev, newOrd]);
      this.addNotification(
        `Nueva Orden de Producción #${newOrd.idorden} creada (Estado: PENDIENTE)`,
      );
      return of(newOrd);
    }

    return this.http.post<ProductionOrder>(
      `${this.apiBaseUrl}/production-orders/newOrder`,
      { product: { idproducto: productId }, quantity, branch: { idsucursal: branchId } },
      { headers: this.getHeaders() },
    );
  }

  startProductionOrder(id: number): Observable<any> {
    if (this.mockMode()) {
      const order = this.mockProductionOrders().find((o) => o.idorden === id);
      if (!order) return throwError(() => new Error('Orden no encontrada'));
      if (order.estado !== 'PENDIENTE')
        return throwError(() => new Error('La orden ya se inició o está completada.'));

      const recipe = this.mockRecipes().find(
        (r) => r.product.idproducto === order.product.idproducto,
      );
      if (!recipe) {
        return throwError(
          () =>
            new Error(
              'No existe una receta (BOM) registrada para este modelo de cámara. No se puede iniciar la fabricación.',
            ),
        );
      }

      let canProduce = true;
      const neededMaterials: { id: number; qty: number }[] = [];

      for (const ingredient of recipe.details) {
        const product = this.mockProducts().find(
          (p) => p.idproducto === ingredient.ingredient.idproducto,
        );
        const qtyNeeded = ingredient.quantityRequired * order.quantity;
        if (!product || product.stock < qtyNeeded) {
          canProduce = false;
        }
        neededMaterials.push({ id: ingredient.ingredient.idproducto, qty: qtyNeeded });
      }

      if (!canProduce) {
        return throwError(
          () =>
            new Error(
              'Componentes insuficientes en inventario para iniciar el ensamblaje de esta orden.',
            ),
        );
      }

      this.mockProducts.update((prev) =>
        prev.map((p) => {
          const needed = neededMaterials.find((n) => n.id === p.idproducto);
          if (needed) {
            return { ...p, stock: p.stock - needed.qty };
          }
          return p;
        }),
      );

      this.mockProductionOrders.update((prev) =>
        prev.map((o) =>
          o.idorden === id
            ? { ...o, estado: 'EN_PROCESO', fechaInicio: new Date().toISOString() }
            : o,
        ),
      );

      this.addNotification(
        `Ensamblaje Iniciado para Orden #${id}. Componentes ópticos y electrónicos consumidos del stock.`,
      );
      return of({ message: 'Orden de producción iniciada. Estado: EN_PROCESO' });
    }

    return this.http.patch<any>(
      `${this.apiBaseUrl}/production-orders/start/${id}`,
      {},
      { headers: this.getHeaders() },
    );
  }

  completeProductionOrder(id: number): Observable<any> {
    if (this.mockMode()) {
      const order = this.mockProductionOrders().find((o) => o.idorden === id);
      if (!order) return throwError(() => new Error('Orden no encontrada'));
      if (order.estado !== 'EN_PROCESO')
        return throwError(
          () => new Error('La orden debe estar en proceso para poder completarse.'),
        );

      this.mockProducts.update((prev) =>
        prev.map((p) => {
          if (p.idproducto === order.product.idproducto) {
            return { ...p, stock: p.stock + order.quantity };
          }
          return p;
        }),
      );

      this.mockProductionOrders.update((prev) =>
        prev.map((o) =>
          o.idorden === id ? { ...o, estado: 'COMPLETADO', fechaFin: new Date().toISOString() } : o,
        ),
      );

      this.addNotification(
        `Orden de Producción #${id} COMPLETADA. ${order.quantity} cámaras añadidas al stock disponible.`,
      );
      return of({ message: 'Orden de producción completada con éxito. Estado: COMPLETADO' });
    }

    return this.http.patch<any>(
      `${this.apiBaseUrl}/production-orders/complete/${id}`,
      {},
      { headers: this.getHeaders() },
    );
  }

  getProductionOrdersReport(): Observable<string> {
    if (this.mockMode()) {
      return of(
        `REPORTE DE PRODUCCION DE CAMARAS\nGenerado: ${new Date().toISOString()}\nTotal Órdenes: ${this.mockProductionOrders().length}\nCompletadas: ${this.mockProductionOrders().filter((o) => o.estado === 'COMPLETADO').length}\nEn Proceso: ${this.mockProductionOrders().filter((o) => o.estado === 'EN_PROCESO').length}`,
      );
    }
    return this.http.get(`${this.apiBaseUrl}/production-orders/getReport`, {
      headers: this.getHeaders(),
      responseType: 'text',
    });
  }

  getProductionOrdersPdfReport(): Observable<Blob> {
    if (this.mockMode()) {
      const dummyPdf = new Blob(['%PDF-1.4\n%Mock PDF content'], { type: 'application/pdf' });
      return of(dummyPdf);
    }
    return this.http.get(`${this.apiBaseUrl}/production-orders/getReport`, {
      headers: this.getHeaders(),
      responseType: 'blob',
    });
  }

  consultarIA(
    prompt: string,
    mode: 'recommendations' | 'Support',
  ): Observable<{ response: string }> {
    if (this.mockMode()) {
      let response = '';
      if (mode === 'Support') {
        response = `[Copiloto de Soporte Técnico] Hola, sobre tu consulta "${prompt}": El número de soporte de Nova Camera Manufacturing es +51 987 654 321. Nuestras plantas operan de Lunes a Viernes de 07:00 a 19:00. Las cámaras producidas cuentan con 2 años de garantía de fábrica.`;
      } else {
        if (prompt.toLowerCase().includes('recet') || prompt.toLowerCase().includes('insumo')) {
          response = `[Copiloto de Planta] Actualmente tenemos 2 recetas registradas: 
1. **Receta Alpha I DSLR** (ID 1) requiere: 1 Sensor 45MP, 1 Lente Prime 50mm, 1 Procesador Engine X1, 1 Chasis, 1 Pantalla, 1 Batería, 1 Obturador.
2. **Receta Prism X Mirrorless** (ID 2) requiere: 1 Sensor 45MP, 1 Procesador Engine X1, 1 Chasis, 1 Pantalla, 1 Batería.
Stock actual de sensores: ${this.mockProducts().find((p) => p.idproducto === 1)?.stock || 0} unidades.`;
        } else {
          response = `[Copiloto Nova AI] Analizando el sistema de cámaras... Basado en tu consulta "${prompt}", sugiero iniciar una orden de producción para la cámara "Nova Prism X Mirrorless" en la Sucursal 1 (Lima), ya que posee mayor margen de ganancia ($1299.99 sobre un costo de $600.00) y el stock actual de materias primas es conforme para fabricar hasta 35 unidades.`;
        }
      }
      return of({ response });
    }

    return this.http.post<{ response: string }>(`${this.apiBaseUrl}/IA/consulta`, { prompt, mode });
  }

  getNotificationsList(): Observable<Notification[]> {
    if (this.mockMode()) {
      return of(this.mockNotifications());
    }
    return this.http.get<Notification[]>(`${this.apiBaseUrl}/notificaciones/listar`, {
      headers: this.getHeaders(),
    });
  }

  addNotification(cuerpo: string) {
    const newN: Notification = {
      id: Math.max(...this.mockNotifications().map((n) => n.id), 0) + 1,
      cuerpo,
      fecha: new Date().toISOString(),
      leido: false,
    };
    this.mockNotifications.update((prev) => [newN, ...prev]);
    this.persistAll();
  }

  markNotificationsAsRead(): void {
    if (this.mockMode()) {
      this.mockNotifications.update((prev) => prev.map((n) => ({ ...n, leido: true })));
      this.persistAll();
    }
  }
}
