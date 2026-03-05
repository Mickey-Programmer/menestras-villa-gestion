package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.*;
import com.menestrasvilla.gestion.repository.*;
import com.menestrasvilla.gestion.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/compras")
@PreAuthorize("hasAuthority('ADMIN')")
public class CompraController {

    @Autowired private CompraRepository compraRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CompraService compraService;
    
    @Autowired private MovimientoInventarioRepository movimientoRepository; 

    @GetMapping
    public String listarCompras(Model model) {
        model.addAttribute("compras", compraRepository.findAll());
        return "compras/historial"; 
    }

    @GetMapping("/nueva")
    public String nuevaCompra(Model model) {
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("productos", productoRepository.findProductosParaVenta()); 
        return "compras/nueva-compra"; 
    }

    @PostMapping("/guardar")
    public String guardarCompra(
            @RequestParam Long proveedorId,
            @RequestParam BigDecimal totalCompra,
            @RequestParam("productoIds") List<Long> productoIds,
            @RequestParam("cantidades") List<BigDecimal> cantidades,
            @RequestParam("precios") List<BigDecimal> precios,
            Authentication authentication) {

        Usuario usuario = usuarioRepository.findByUsername(authentication.getName()).orElseThrow();
        Proveedor proveedor = proveedorRepository.findById(proveedorId).orElseThrow();

        Compra compra = new Compra();
        compra.setTotalCompra(totalCompra);
        compra.setProveedor(proveedor);

        List<DetalleCompra> detalles = new ArrayList<>();
        for (int i = 0; i < productoIds.size(); i++) {
            Producto p = productoRepository.findById(productoIds.get(i)).orElseThrow();
            DetalleCompra d = new DetalleCompra();
            d.setProducto(p);
            d.setCantidadKg(cantidades.get(i));
            d.setPrecioUnidadCompra(precios.get(i));
            detalles.add(d);
        }
        
        compra.setDetalles(detalles);
        
        compraService.registrarCompra(compra, usuario);

        for (int i = 0; i < productoIds.size(); i++) {
            Producto p = productoRepository.findById(productoIds.get(i)).orElseThrow();
            
            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(p);
            mov.setTipoMovimiento("ENTRADA");
            mov.setCantidad(cantidades.get(i).doubleValue()); 
            mov.setUnidadMedida("KILOS"); 
            mov.setMotivo("Compra a Proveedor: " + proveedor.getRazonSocial());
            mov.setFechaHora(LocalDateTime.now());
            mov.setUsuario(usuario);
            
            movimientoRepository.save(mov);
        }

        return "redirect:/compras?success=Compra registrada y Kardex actualizado";
    }
}