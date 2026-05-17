package com.cursoIntegrador.novaBackend.Service.DAO;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cursoIntegrador.novaBackend.Model.Entity.Branch;
import com.cursoIntegrador.novaBackend.Repository.BranchRepository;
import com.cursoIntegrador.novaBackend.Service.ReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchService {

    @Autowired
    private BranchRepository branchrepo;

    @Autowired
    private ReportService reportService;

    public List<Branch> listarSucursales() {
        return branchrepo.findAll();
    }

    public Branch guardarSucursal(Branch branch) {
        return branchrepo.save(branch);
    }

    public void eliminarSucursal(Integer id) {
        if (!branchrepo.existsById(id)) {
            throw new IllegalArgumentException("La sucursal con ID " + id + " no existe");
        }
        branchrepo.deleteById(id);
    }

    public Branch modificarSucursalParcial(Integer id, Branch branchActualizada) {
        Branch existente = branchrepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal con ID " + id + " no encontrada"));

        BeanUtils.copyProperties(branchActualizada, existente, getNullPropertyNames(branchActualizada));

        return branchrepo.save(existente);
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
            return reportService.generateExampleReport(this.listarSucursales(), "SUCURSALES");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
