package com.cursoIntegrador.novaBackend.Service.DAO;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.Entity.BillOfMaterials;
import com.cursoIntegrador.novaBackend.Model.Entity.BOMDetail;
import com.cursoIntegrador.novaBackend.Model.Entity.ProductType;
import com.cursoIntegrador.novaBackend.Repository.BillOfMaterialsRepository;
import com.cursoIntegrador.novaBackend.Repository.ProductRepository;

@Service
public class BillOfMaterialsService {

    @Autowired
    private BillOfMaterialsRepository bomRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<BillOfMaterials> getAllBOMs() {
        return bomRepository.findAll();
    }

    public BillOfMaterials getBOMById(Long id) {
        return bomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada con ID: " + id));
    }

    public BillOfMaterials getBOMByProductId(Integer productId) {
        return bomRepository.findByProduct_Idproducto(productId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada para el producto ID: " + productId));
    }

    public BillOfMaterials saveBOM(BillOfMaterials bom) {
        var product = productRepository.findById(bom.getProduct().getIdproducto())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + bom.getProduct().getIdproducto()));

        if (product.getTipoProducto() == ProductType.MATERIA_PRIMA) {
            throw new IllegalArgumentException("No se puede crear una receta para un producto que es clasificado como MATERIA_PRIMA");
        }

        bom.setProduct(product);

        if (bom.getDetails() != null) {
            for (BOMDetail detail : bom.getDetails()) {
                var ingredient = productRepository.findById(detail.getIngredient().getIdproducto())
                        .orElseThrow(() -> new IllegalArgumentException("Ingrediente no encontrado: " + detail.getIngredient().getIdproducto()));
                
                if (ingredient.getIdproducto().equals(product.getIdproducto())) {
                    throw new IllegalArgumentException("Un producto no puede ser ingrediente de sí mismo");
                }

                detail.setIngredient(ingredient);
                detail.setBillOfMaterials(bom);
            }
        }

        return bomRepository.save(bom);
    }

    public void deleteBOM(Long id) {
        if (!bomRepository.existsById(id)) {
            throw new IllegalArgumentException("Receta no encontrada con ID: " + id);
        }
        bomRepository.deleteById(id);
    }
}
