package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.*;
import com.menestrasvilla.gestion.repository.*;
import com.menestrasvilla.gestion.service.TicketPdfService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequestMapping("/pos")
@RequiredArgsConstructor
public class VentaController {

    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final CajaRepository cajaRepository;
    private final TicketPdfService ticketPdfService;
    private final ConfiguracionRepository configuracionRepository;

    @GetMapping
    public String mostrarPOS(@AuthenticationPrincipal UserDetails userDetails, Model model, RedirectAttributes redirect) {
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<Caja> cajaActiva = cajaRepository.findFirstByUsuarioAndEstadoOrderByFechaAperturaDesc(usuario, "ABIERTA");

        if (cajaActiva.isEmpty()) {
            redirect.addFlashAttribute("error", "¡ATENCIÓN! Debe abrir una caja antes de acceder al Punto de Venta.");
            return "redirect:/caja/estado";
        }

        model.addAttribute("productos", productoRepository.findProductosParaVenta());
        return "ventas/pos";
    }

    @PostMapping("/guardar")
    @Transactional
    public String procesarVenta(@RequestParam Long[] productoIds,
                               @RequestParam String[] unidades,
                               @RequestParam BigDecimal[] cantidades,
                               @RequestParam BigDecimal totalBruto,
                               @RequestParam String metodoPago,
                               @RequestParam String tipoComprobante,
                               @RequestParam(required = false, defaultValue = "") String clienteNombre,
                               @RequestParam(required = false, defaultValue = "") String clienteDoc,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Caja caja = cajaRepository.findFirstByUsuarioAndEstadoOrderByFechaAperturaDesc(usuario, "ABIERTA")
                    .orElseThrow(() -> new RuntimeException("Debe abrir caja antes de realizar una venta"));

            Venta venta = new Venta();
            venta.setUsuario(usuario);
            venta.setCaja(caja);
            venta.setTotalBruto(totalBruto);
            venta.setTotalNeto(totalBruto); 
            venta.setFecha(LocalDateTime.now());
            venta.setMetodoPago(metodoPago);
            venta.setTipoComprobante(tipoComprobante);
            venta.setClienteNombre(clienteNombre);
            venta.setClienteDoc(clienteDoc);
            
            venta.setDetalles(new ArrayList<>());

            for (int i = 0; i < productoIds.length; i++) {
                Producto producto = productoRepository.findById(productoIds[i])
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                DetalleVenta detalle = new DetalleVenta();
                detalle.setVenta(venta);
                detalle.setProducto(producto);
                detalle.setUnidadMedida(unidades[i]);
                detalle.setCantidadUnidades(cantidades[i]);
                
                BigDecimal cantidadKg = unidades[i].equals("SACO") 
                    ? producto.getKgPorSaco().multiply(cantidades[i]) 
                    : cantidades[i];
                
                if (producto.getStockActualKg().compareTo(cantidadKg) < 0) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() + 
                                               ". Stock actual: " + producto.getStockActualKg() + " kg, " +
                                               "Intentó vender: " + cantidadKg + " kg.");
                }
                
                BigDecimal subtotal = unidades[i].equals("SACO") 
                    ? producto.getPrecioSaco().multiply(cantidades[i]) 
                    : producto.getPrecioKilo().multiply(cantidades[i]);
                
                detalle.setSubtotal(subtotal);
                venta.getDetalles().add(detalle);

                producto.setStockActualKg(producto.getStockActualKg().subtract(cantidadKg));
                productoRepository.save(producto);

                MovimientoInventario mov = new MovimientoInventario();
                mov.setProducto(producto);
                mov.setTipoMovimiento("SALIDA");
                mov.setCantidad(cantidadKg.doubleValue()); 
                mov.setUnidadMedida("KILOS");
                mov.setMotivo("Venta POS - Tipo: " + tipoComprobante);
                mov.setFechaHora(LocalDateTime.now());
                mov.setUsuario(usuario);
                
                movimientoRepository.save(mov);
            }

            ventaRepository.save(venta);
            
            return "redirect:/pos/ticket/" + venta.getId();
            
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error en POS: " + e.getMessage());
            return "redirect:/pos";
        }
    }

    @GetMapping("/ticket/{id}")
    public String mostrarTicket(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            Venta venta = ventaRepository.findByIdConDetalles(id)
                    .orElseThrow(() -> new RuntimeException("El comprobante solicitado no existe."));
            
            Configuracion configuracion = configuracionRepository.findById(1L).orElse(null);
            
            model.addAttribute("venta", venta);
            model.addAttribute("configuracion", configuracion);
            
            return "ventas/ticket";
            
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al cargar el ticket: " + e.getMessage());
            return "redirect:/pos";
        }
    }

    @GetMapping("/ticket/{id}/pdf")
    public void descargarTicketPdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Venta venta = ventaRepository.findByIdConDetalles(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        Configuracion configuracion = configuracionRepository.findById(1L).orElse(null);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=Ticket_Menestras_Villa_" + id + ".pdf");
        
        ticketPdfService.generarTicketPdf(venta, configuracion, response);
    }
    
    @GetMapping("/historial")
    public String verMisVentas(@AuthenticationPrincipal UserDetails userDetails, 
                               @RequestParam(defaultValue = "0") int page, 
                               Model model) {
        
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Pageable pageable = PageRequest.of(page, 10);
        Page<Venta> misVentas = ventaRepository.findByUsuarioOrderByFechaDesc(usuario, pageable);
        
        model.addAttribute("ventas", misVentas.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", misVentas.getTotalPages());
        
        return "ventas/historial";
    }
}