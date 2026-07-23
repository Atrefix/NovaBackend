package com.cursoIntegrador.novaBackend.Service.DAO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.DTO.Purchase.PurchaseProductDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Purchase.PurchaseRequestDTO;
import com.cursoIntegrador.novaBackend.Model.DTO.Purchase.PurhcaseHistoryDTO;
import com.cursoIntegrador.novaBackend.Model.Entity.Cuenta;
import com.cursoIntegrador.novaBackend.Model.Entity.ProductType;
import com.cursoIntegrador.novaBackend.Model.Entity.Purchase;
import com.cursoIntegrador.novaBackend.Model.Entity.PurchaseDetails;
import com.cursoIntegrador.novaBackend.Model.Security.CustomUserDetails;
import com.cursoIntegrador.novaBackend.Repository.AccountRepository;
import com.cursoIntegrador.novaBackend.Repository.ProductRepository;
import com.cursoIntegrador.novaBackend.Repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ProductRepository productRepository;

    public PurchaseRequestDTO savePurchase(CustomUserDetails userDetails, PurchaseRequestDTO purchaseDTO) {

        Cuenta cuenta = accountRepository.findByEmail(userDetails.getUsername());

        List<PurchaseDetails> realDetails = new ArrayList<>();

        Purchase purchase = new Purchase();
        purchase.setCuenta(cuenta);
        purchase.setCityDelivery(purchaseDTO.getCityDelivery());
        purchase.setAddressDelivery(purchaseDTO.getAddressDelivery());
        purchase.setTotalAmount(purchaseDTO.getMontoProcesado());

        for (PurchaseProductDTO dtoProduct : purchaseDTO.getProductos()) {
            var product = productRepository.findById(dtoProduct.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + dtoProduct.getIdProducto()));

            if (product.getTipoProducto() != ProductType.MATERIA_PRIMA) {
                throw new IllegalArgumentException(
                        "Solo se pueden abastecer productos clasificados como MATERIA_PRIMA. El producto "
                                + product.getNombre() + " es de tipo " + product.getTipoProducto());
            }

            product.setStock(product.getStock() + dtoProduct.getQuantity());
            productRepository.save(product);

            PurchaseDetails detail = new PurchaseDetails();
            detail.setProduct(product);
            detail.setQuantity(dtoProduct.getQuantity());
            detail.setPurchase(purchase);
            detail.setInstructions(dtoProduct.getInstructions());

            realDetails.add(detail);
        }

        purchase.setDetails(realDetails);

        purchaseRepository.save(purchase);

        return purchaseDTO;
    }

    public List<PurhcaseHistoryDTO> getHistory(CustomUserDetails userDetails) {

        List<Purchase> purchases = purchaseRepository.findAllByCuentaIdcuenta(userDetails.getCuenta().getIdcuenta());
        List<PurhcaseHistoryDTO> dtos = new ArrayList<>();

        for (Purchase purchase : purchases) {
            dtos.add(new PurhcaseHistoryDTO(purchase));
        }

        return dtos;
    }

}
