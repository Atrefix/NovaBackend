package com.cursoIntegrador.novaBackend.Service.DAO;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.Entity.Product;
import com.cursoIntegrador.novaBackend.Repository.ProductRepository;
import com.cursoIntegrador.novaBackend.Service.ReportService;

import org.springframework.beans.BeanWrapperImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReportService reportService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product guardarProducto(Product product) {
        return productRepository.save(product);
    }

    public void eliminarProductoPorId(Integer idproducto) {
        if (!productRepository.existsById(idproducto)) {
            throw new IllegalArgumentException("El producto con ID " + idproducto + " no existe");
        }
        productRepository.deleteById(idproducto);
    }

    public Product modificarProducto(Integer id, Product p) {
        Product existente = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto con ID " + id + " no encontrado"));

        BeanUtils.copyProperties(p, existente, getNullPropertyNames(p));

        return productRepository.save(existente);
    }

    private String[] getNullPropertyNames(Object source) {
        final var src = new BeanWrapperImpl(source);
        return Stream.of(src.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(name -> src.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    public byte[] getReport() {
        try {
            return reportService.generateExampleReport(getAllProducts(), "PRODUCTOS");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

}
