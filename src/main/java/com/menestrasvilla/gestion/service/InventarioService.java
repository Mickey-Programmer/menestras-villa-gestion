package com.menestrasvilla.gestion.service;

import com.menestrasvilla.gestion.entity.Categorias;
import com.menestrasvilla.gestion.entity.Producto;
import com.menestrasvilla.gestion.repository.CategoriaRepository;
import com.menestrasvilla.gestion.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<Categorias> listarCategorias() { return categoriaRepository.findAll(); }
    public Categorias guardarCategoria(Categorias categoria) { return categoriaRepository.save(categoria); }

    public List<Producto> listarProductos() { return productoRepository.findAll(); }
    public Producto guardarProducto(Producto producto) { return productoRepository.save(producto); }
    public Producto obtenerProducto(Long id) { 
        return productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no existe")); 
    }

    @Transactional
    public void actualizarStock(Long productoId, BigDecimal cantidad, String unidadMedida, boolean esVenta) {
        Producto producto = obtenerProducto(productoId);
        
        BigDecimal factorConversiones = unidadMedida.equalsIgnoreCase("SACO") ? producto.getKgPorSaco() : BigDecimal.ONE;
        BigDecimal cantidadEnKg = cantidad.multiply(factorConversiones);

        if (esVenta) {
            producto.setStockActualKg(producto.getStockActualKg().subtract(cantidadEnKg));
        } else {
            producto.setStockActualKg(producto.getStockActualKg().add(cantidadEnKg));
        }
        productoRepository.save(producto);
    }
}