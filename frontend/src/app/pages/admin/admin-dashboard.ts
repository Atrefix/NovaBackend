import { Component, inject, signal, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { NovaService, Product, Branch, Recipe, ProductionOrder, Account, Notification } from '../../services/nova.service';
import { fadeInUp, fadeIn, slideInOut } from '../../shared/animations';

@Component({
  selector: 'app-admin-dashboard',
  imports: [FormsModule, CommonModule],
  templateUrl: './admin-dashboard.html',
  animations: [fadeInUp, fadeIn, slideInOut]
})
export class AdminDashboard implements OnInit {
  readonly nova = inject(NovaService);
  private readonly router = inject(Router);

  currentTab = signal<string>('overview');

  products = signal<Product[]>([]);
  branches = signal<Branch[]>([]);
  recipes = signal<Recipe[]>([]);
  orders = signal<ProductionOrder[]>([]);
  accounts = signal<Account[]>([]);
  notifications = signal<Notification[]>([]);

  showProductModal = signal<boolean>(false);
  editingProductId = signal<number | null>(null);
  productForm = {
    codproducto: '',
    nombre: '',
    categoria: '',
    stock: 0,
    preciocompra: 0.0,
    precioventa: 0.0,
    fechavencimiento: '',
    tipoProducto: 'MATERIA_PRIMA' as 'MATERIA_PRIMA' | 'PRODUCTO_TERMINADO'
  };

  showBranchModal = signal<boolean>(false);
  editingBranchId = signal<number | null>(null);
  branchForm = {
    nombre: '',
    direccion: ''
  };

  recipeName = signal<string>('');
  recipeDesc = signal<string>('');
  recipeProductTarget = signal<number>(0);
  recipeDetails = signal<{ idproducto: number; quantity: number }[]>([]);
  recipeSuccess = signal<boolean>(false);

  orderProductTarget = signal<number>(0);
  orderQuantity = signal<number>(5);
  orderBranchTarget = signal<number>(0);
  orderSuccess = signal<boolean>(false);

  reportContent = signal<string | null>(null);
  reportTitle = signal<string | null>(null);

  chatInput = signal<string>('');
  chatMessages = signal<{ sender: 'user' | 'ai'; text: string; time: Date }[]>([
    { sender: 'ai', text: 'Hola, soy tu copiloto de producción. ¿Qué información deseas analizar hoy?', time: new Date() }
  ]);
  aiLoading = signal<boolean>(false);

  infoMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.loadAllData();
  }

  loadAllData() {
    this.nova.getProducts().subscribe(res => this.products.set(res));
    this.nova.getBranches().subscribe(res => this.branches.set(res));
    this.nova.getRecipes().subscribe(res => this.recipes.set(res));
    this.nova.getProductionOrders().subscribe(res => this.orders.set(res));
    this.nova.getAccounts().subscribe(res => this.accounts.set(res));
    this.nova.getNotificationsList().subscribe(res => this.notifications.set(res));
  }

  changeTab(tab: string) {
    this.currentTab.set(tab);
    this.clearMessages();
    this.reportContent.set(null);
  }

  clearMessages() {
    this.infoMessage.set(null);
    this.errorMessage.set(null);
    this.recipeSuccess.set(false);
    this.orderSuccess.set(false);
  }

  openAddProduct() {
    this.editingProductId.set(null);
    this.productForm = {
      codproducto: '',
      nombre: '',
      categoria: '',
      stock: 0,
      preciocompra: 0.0,
      precioventa: 0.0,
      fechavencimiento: new Date().toISOString().substring(0, 16),
      tipoProducto: 'MATERIA_PRIMA'
    };
    this.showProductModal.set(true);
  }

  openEditProduct(prod: Product) {
    this.editingProductId.set(prod.idproducto);
    this.productForm = {
      codproducto: prod.codproducto,
      nombre: prod.nombre,
      categoria: prod.categoria,
      stock: prod.stock,
      preciocompra: prod.preciocompra,
      precioventa: prod.precioventa,
      fechavencimiento: prod.fechavencimiento.substring(0, 16),
      tipoProducto: prod.tipoProducto
    };
    this.showProductModal.set(true);
  }

  saveProduct() {
    const formattedDate = new Date(this.productForm.fechavencimiento).toISOString();
    const payload = { ...this.productForm, fechavencimiento: formattedDate };

    if (this.editingProductId()) {
      this.nova.modifyProduct(this.editingProductId()!, payload).subscribe({
        next: () => {
          this.showProductModal.set(false);
          this.loadAllData();
          this.infoMessage.set('Producto modificado con éxito.');
        },
        error: (err) => alert(err.message)
      });
    } else {
      this.nova.addProduct(payload).subscribe({
        next: () => {
          this.showProductModal.set(false);
          this.loadAllData();
          this.infoMessage.set('Producto agregado al inventario.');
        },
        error: (err) => alert(err.message)
      });
    }
  }

  deleteProduct(id: number) {
    if (confirm('¿Estás seguro de eliminar este producto?')) {
      this.nova.deleteProduct(id).subscribe({
        next: () => {
          this.loadAllData();
          this.infoMessage.set('Producto eliminado.');
        }
      });
    }
  }

  openAddBranch() {
    this.editingBranchId.set(null);
    this.branchForm = { nombre: '', direccion: '' };
    this.showBranchModal.set(true);
  }

  openEditBranch(b: Branch) {
    this.editingBranchId.set(b.idsucursal);
    this.branchForm = { nombre: b.nombre, direccion: b.direccion };
    this.showBranchModal.set(true);
  }

  saveBranch() {
    if (this.editingBranchId()) {
      this.nova.modifyBranch(this.editingBranchId()!, this.branchForm).subscribe({
        next: () => {
          this.showBranchModal.set(false);
          this.loadAllData();
          this.infoMessage.set('Sucursal modificada.');
        }
      });
    } else {
      this.nova.addBranch(this.branchForm).subscribe({
        next: () => {
          this.showBranchModal.set(false);
          this.loadAllData();
          this.infoMessage.set('Sucursal agregada con éxito.');
        }
      });
    }
  }

  deleteBranch(id: number) {
    if (confirm('¿Estás seguro de eliminar esta sucursal?')) {
      this.nova.deleteBranch(id).subscribe({
        next: () => {
          this.loadAllData();
          this.infoMessage.set('Sucursal eliminada.');
        }
      });
    }
  }

  addRecipeDetailRow() {
    const rawMaterials = this.products().filter(p => p.tipoProducto === 'MATERIA_PRIMA');
    if (rawMaterials.length > 0) {
      this.recipeDetails.update(prev => [...prev, { idproducto: rawMaterials[0].idproducto, quantity: 1 }]);
    } else {
      alert('Primero debes registrar materias primas.');
    }
  }

  removeRecipeDetailRow(idx: number) {
    this.recipeDetails.update(prev => prev.filter((_, i) => i !== idx));
  }

  saveRecipe() {
    if (!this.recipeName() || this.recipeProductTarget() === 0 || this.recipeDetails().length === 0) {
      alert('Por favor, ingresa el nombre, el producto objetivo y al menos un insumo.');
      return;
    }

    const payload: Recipe = {
      nombre: this.recipeName(),
      descripcion: this.recipeDesc(),
      product: { idproducto: +this.recipeProductTarget() },
      details: this.recipeDetails().map(d => ({
        ingredient: { idproducto: +d.idproducto },
        quantityRequired: d.quantity
      }))
    };

    this.nova.createRecipe(payload).subscribe({
      next: () => {
        this.recipeSuccess.set(true);
        this.recipeName.set('');
        this.recipeDesc.set('');
        this.recipeDetails.set([]);
        this.recipeProductTarget.set(0);
        this.loadAllData();
      }
    });
  }

  createOrder() {
    if (this.orderProductTarget() === 0 || this.orderBranchTarget() === 0 || this.orderQuantity() <= 0) {
      alert('Datos de orden inválidos.');
      return;
    }

    this.nova.newProductionOrder(
      +this.orderProductTarget(),
      this.orderQuantity(),
      +this.orderBranchTarget()
    ).subscribe({
      next: () => {
        this.orderSuccess.set(true);
        this.orderProductTarget.set(0);
        this.orderBranchTarget.set(0);
        this.loadAllData();
      }
    });
  }

  startOrder(id: number) {
    this.nova.startProductionOrder(id).subscribe({
      next: () => {
        this.loadAllData();
        this.infoMessage.set(`Ensamblaje de Orden #${id} iniciado. Stock de insumos rebajado.`);
      },
      error: (err) => {
        alert(err.message || 'Componentes insuficientes en inventario.');
      }
    });
  }

  completeOrder(id: number) {
    this.nova.completeProductionOrder(id).subscribe({
      next: () => {
        this.loadAllData();
        this.infoMessage.set(`Orden #${id} completada. Cámaras añadidas al catálogo.`);
      }
    });
  }

  getPendingOrders() {
    return this.orders().filter(o => o.estado === 'PENDIENTE');
  }

  getProcessingOrders() {
    return this.orders().filter(o => o.estado === 'EN_PROCESO');
  }

  getCompletedOrders() {
    return this.orders().filter(o => o.estado === 'COMPLETADO');
  }

  getProductById(id: number): Product | undefined {
    return this.products().find(p => p.idproducto === id);
  }

  getBranchById(id: number): Branch | undefined {
    return this.branches().find(b => b.idsucursal === id);
  }

  toggleUserRole(acc: Account) {
    const nextRole = acc.rol === 'ADMIN' ? 'USER' : 'ADMIN';
    this.nova.changeRole(acc.idcuenta, nextRole).subscribe({
      next: () => {
        this.loadAllData();
        this.infoMessage.set(`Rol de ${acc.username} actualizado a ${nextRole}.`);
      }
    });
  }

  exportAccountsToExcel() {
    this.nova.exportAccounts().subscribe(res => {
      this.infoMessage.set(res.message || 'Archivo exportado.');
    });
  }

  loadReport(type: 'products' | 'branches' | 'accounts' | 'production') {
    let request;
    let title = '';

    switch (type) {
      case 'products':
        request = this.nova.getProductReport();
        title = 'Reporte de Productos y Componentes';
        break;
      case 'branches':
        request = this.nova.getBranchReport();
        title = 'Reporte de Plantas y Sucursales';
        break;
      case 'accounts':
        request = this.nova.getAccountsReport();
        title = 'Reporte de Cuentas de Usuario';
        break;
      case 'production':
        request = this.nova.getProductionOrdersReport();
        title = 'Reporte de Órdenes de Ensamblaje';
        break;
    }

    request.subscribe(content => {
      this.reportTitle.set(title);
      this.reportContent.set(content);
    });
  }

  clearNotifications() {
    this.nova.markNotificationsAsRead();
    this.loadAllData();
  }

  sendCopilotQuery() {
    const prompt = this.chatInput().trim();
    if (!prompt) return;

    this.chatMessages.update(msgs => [...msgs, { sender: 'user', text: prompt, time: new Date() }]);
    this.chatInput.set('');
    this.aiLoading.set(true);

    this.nova.consultarIA(prompt, 'recommendations').subscribe({
      next: (res) => {
        this.aiLoading.set(false);
        this.chatMessages.update(msgs => [...msgs, { sender: 'ai', text: res.response, time: new Date() }]);
      },
      error: () => {
        this.aiLoading.set(false);
        this.chatMessages.update(msgs => [...msgs, { sender: 'ai', text: 'Error al contactar al copiloto AI de producción.', time: new Date() }]);
      }
    });
  }

  logout() {
    this.nova.logout().subscribe(() => {
      this.router.navigate(['/']);
    });
  }
}
