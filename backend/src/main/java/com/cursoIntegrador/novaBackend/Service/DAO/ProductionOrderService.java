package com.cursoIntegrador.novaBackend.Service.DAO;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cursoIntegrador.novaBackend.Model.Entity.BillOfMaterials;
import com.cursoIntegrador.novaBackend.Model.Entity.BOMDetail;
import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Model.Entity.Product;
import com.cursoIntegrador.novaBackend.Model.Entity.ProductType;
import com.cursoIntegrador.novaBackend.Model.Entity.ProductionOrder;
import com.cursoIntegrador.novaBackend.Model.Entity.ProductionOrderStatus;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Repository.AccountRepository;
import com.cursoIntegrador.novaBackend.Repository.BillOfMaterialsRepository;
import com.cursoIntegrador.novaBackend.Repository.BranchRepository;
import com.cursoIntegrador.novaBackend.Repository.ProductRepository;
import com.cursoIntegrador.novaBackend.Repository.ProductionOrderRepository;
import com.cursoIntegrador.novaBackend.Service.ReportService;

@Service
public class ProductionOrderService {

    @Autowired
    private ProductionOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BillOfMaterialsRepository bomRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ReportService reportService;

    public List<ProductionOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public byte[] getReport() {
        try {
            return reportService.generateExampleReport(getAllOrders(), "ORDENES DE PRODUCCION");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public ProductionOrder getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden de producción no encontrada con ID: " + id));
    }

    public ProductionOrder createOrder(CustomUserDetails userDetails, ProductionOrder order) {
        Cuenta supervisor = accountRepository.findByEmail(userDetails.getUsername());

        Product product = productRepository.findById(order.getProduct().getIdproducto())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto no encontrado: " + order.getProduct().getIdproducto()));

        if (product.getTipoProducto() == ProductType.MATERIA_PRIMA) {
            throw new IllegalArgumentException("No se puede fabricar un producto clasificado como MATERIA_PRIMA");
        }

        // Verificar que exista una receta para fabricar este producto
        bomRepository.findByProduct_Idproducto(product.getIdproducto())
                .orElseThrow(() -> new IllegalArgumentException("No se puede crear una orden de producción para '"
                        + product.getNombre() + "' porque no tiene una receta (BOM) registrada."));

        var branch = branchRepository.findById(order.getBranch().getIdsucursal())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Sucursal no encontrada: " + order.getBranch().getIdsucursal()));

        order.setSupervisor(supervisor);
        order.setProduct(product);
        order.setBranch(branch);
        order.setStatus(ProductionOrderStatus.PENDIENTE);
        order.setDateCreated(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public ProductionOrder startOrder(Long id) {
        ProductionOrder order = getOrderById(id);

        if (order.getStatus() != ProductionOrderStatus.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden iniciar órdenes en estado PENDIENTE. Estado actual: " + order.getStatus());
        }

        order.setStatus(ProductionOrderStatus.EN_PROCESO);
        order.setDateStarted(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public ProductionOrder completeOrder(Long id) {
        ProductionOrder order = getOrderById(id);

        if (order.getStatus() != ProductionOrderStatus.EN_PROCESO) {
            throw new IllegalStateException(
                    "Solo se pueden completar órdenes en estado EN_PROCESO. Estado actual: " + order.getStatus());
        }

        // Obtener receta del producto
        BillOfMaterials bom = bomRepository.findByProduct_Idproducto(order.getProduct().getIdproducto())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Receta no encontrada para el producto: " + order.getProduct().getNombre()));

        // Verificar stock de ingredientes
        for (BOMDetail detail : bom.getDetails()) {
            Product ingredient = detail.getIngredient();
            int requiredQty = detail.getQuantityRequired() * order.getQuantity();

            if (ingredient.getStock() < requiredQty) {
                throw new IllegalStateException("Stock insuficiente para el ingrediente: " + ingredient.getNombre()
                        + ". Requerido: " + requiredQty + ", Disponible en stock: " + ingredient.getStock());
            }
        }

        // Consumir ingredientes
        for (BOMDetail detail : bom.getDetails()) {
            Product ingredient = detail.getIngredient();
            int requiredQty = detail.getQuantityRequired() * order.getQuantity();
            ingredient.setStock(ingredient.getStock() - requiredQty);
            productRepository.save(ingredient);
        }

        // Sumar al stock del producto terminado fabricado
        Product product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productRepository.save(product);

        order.setStatus(ProductionOrderStatus.COMPLETADO);
        order.setDateCompleted(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Transactional
    public ProductionOrder rejectOrder(Long id) {
        ProductionOrder order = getOrderById(id);

        if (order.getStatus() == ProductionOrderStatus.COMPLETADO
                || order.getStatus() == ProductionOrderStatus.RECHAZADO) {
            throw new IllegalStateException("No se puede cancelar una orden en estado " + order.getStatus());
        }

        order.setStatus(ProductionOrderStatus.RECHAZADO);
        order.setDateCompleted(LocalDateTime.now()); // marca fin

        return orderRepository.save(order);
    }
}
