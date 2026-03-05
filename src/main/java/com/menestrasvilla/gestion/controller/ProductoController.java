package com.menestrasvilla.gestion.controller;

import com.menestrasvilla.gestion.entity.MovimientoInventario;
import com.menestrasvilla.gestion.entity.Producto;
import com.menestrasvilla.gestion.entity.Usuario;
import com.menestrasvilla.gestion.repository.CategoriaRepository;
import com.menestrasvilla.gestion.repository.MovimientoInventarioRepository;
import com.menestrasvilla.gestion.repository.ProductoRepository;
import com.menestrasvilla.gestion.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@Valid @ModelAttribute("producto") Producto producto, 
                                 BindingResult result, 
                                 Model model, 
                                 RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "productos/formulario";
        }

        try {
            String nombreLimpio = producto.getNombre().trim();
            producto.setNombre(nombreLimpio);
            
            if (producto.getId() == null && productoRepository.existsByNombre(nombreLimpio)) {
                model.addAttribute("error", "Error: Ya existe un producto registrado con el nombre '" + nombreLimpio + "'.");
                model.addAttribute("categorias", categoriaRepository.findAll());
                return "productos/formulario";
            }

            if (producto.getId() == null) {
                producto.setActivo(true);
            }
            productoRepository.save(producto);
            redirect.addFlashAttribute("success", "Producto guardado con éxito.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error en la persistencia de datos.");
        }
        return "redirect:/productos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "productos/formulario";
    }

    @PutMapping("/editar/{id}")
    public String actualizarProducto(@PathVariable Long id, 
                                    @Valid @ModelAttribute("producto") Producto producto, 
                                    BindingResult result, 
                                    Model model, 
                                    RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "productos/formulario";
        }
        
        Producto productoExistente = productoRepository.findById(id).orElseThrow();
        
        String nombreLimpio = producto.getNombre().trim();
        producto.setNombre(nombreLimpio);
        
        if (!productoExistente.getNombre().equalsIgnoreCase(nombreLimpio) && productoRepository.existsByNombre(nombreLimpio)) {
            model.addAttribute("error", "Error: No puedes renombrarlo a '" + nombreLimpio + "' porque ya existe otro producto con ese nombre.");
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "productos/formulario";
        }

        producto.setActivo(productoExistente.isActivo());
        producto.setId(id);
        productoRepository.save(producto);
        redirect.addFlashAttribute("success", "Producto actualizado correctamente.");
        return "redirect:/productos";
    }

    @GetMapping("/estado/{id}")
    public String cambiarEstadoProducto(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            Producto producto = productoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
            
            producto.setActivo(!producto.isActivo()); 
            productoRepository.save(producto);
            
            String mensaje = producto.isActivo() ? "Producto habilitado para ventas." : "Producto inhabilitado. Ya no aparecerá en caja.";
            redirect.addFlashAttribute("success", mensaje);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al intentar cambiar el estado del producto.");
        }
        return "redirect:/productos";
    }
    
    @PostMapping("/ajuste")
    public String ajustarStock(@RequestParam Long idProducto, 
                               @RequestParam Double cantidadAjuste, 
                               @RequestParam String motivo,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            BigDecimal ajuste = BigDecimal.valueOf(cantidadAjuste);
            producto.setStockActualKg(producto.getStockActualKg().add(ajuste));
            productoRepository.save(producto);

            MovimientoInventario mov = new MovimientoInventario();
            mov.setProducto(producto);
            mov.setTipoMovimiento("AJUSTE");
            mov.setCantidad(cantidadAjuste);
            mov.setUnidadMedida("KILOS");
            mov.setMotivo(motivo);
            mov.setFechaHora(LocalDateTime.now());
            mov.setUsuario(usuario);
            movimientoRepository.save(mov);

            redirect.addFlashAttribute("success", "Ajuste de stock realizado correctamente para: " + producto.getNombre());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Error al ajustar el stock: " + e.getMessage());
        }
        
        return "redirect:/productos"; 
    }
}