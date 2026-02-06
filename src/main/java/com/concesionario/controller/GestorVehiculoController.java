package com.concesionario.controller;

import com.concesionario.model.Vehiculo;
import com.concesionario.service.VehiculoService;
import com.concesionario.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gestor")
public class GestorVehiculoController {

    @Autowired
    private VehiculoService vehiculoService;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    // ==================== MÉTODOS PARA VEHÍCULOS ====================

    @PostMapping("/guardar-vehiculo")
    public String guardarVehiculoNormal(
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam int año,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam int pasajeros,
            @RequestParam String descripcion,
            @RequestParam String colores,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam(value = "galeria1", required = false) MultipartFile galeria1,
            @RequestParam(value = "galeria2", required = false) MultipartFile galeria2,
            @RequestParam(value = "galeria3", required = false) MultipartFile galeria3,
            @RequestParam MultipartFile imagen,
            RedirectAttributes redirectAttributes) throws IOException {

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setAño(año);
        vehiculo.setPrecio(precio);
        vehiculo.setCategoria(categoria);
        vehiculo.setMotor(motor);
        vehiculo.setTransmision(transmision);
        vehiculo.setCombustible(combustible);
        vehiculo.setPasajeros(pasajeros);
        vehiculo.setDescripcion(descripcion);
        vehiculo.setDestacado(false);

        // Subir modelo 3D si existe
        if (modelo3d != null && !modelo3d.isEmpty()) {
            System.out.println("📦 [GestorVehiculoController] Modelo 3D detectado: " + modelo3d.getOriginalFilename());
            try {
                String urlModelo = supabaseStorageService.uploadFile(modelo3d);
                System.out.println("✅ [GestorVehiculoController] Modelo 3D subido a: " + urlModelo);
                vehiculo.setUrlModelo3d(urlModelo);
            } catch (Exception e) {
                System.err.println("❌ [GestorVehiculoController] Error subiendo modelo 3D: " + e.getMessage());
            }
        }

        // Procesar los colores
        if (colores != null && !colores.isEmpty()) {
            List<String> listaColores = Arrays.stream(colores.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            vehiculo.setColores(listaColores);
        }

        // Procesar galería
        List<MultipartFile> galeria = new ArrayList<>();
        if (galeria1 != null && !galeria1.isEmpty()) galeria.add(galeria1);
        if (galeria2 != null && !galeria2.isEmpty()) galeria.add(galeria2);
        if (galeria3 != null && !galeria3.isEmpty()) galeria.add(galeria3);

        vehiculoService.crearVehiculoNormal(vehiculo, imagen, galeria);
        redirectAttributes.addFlashAttribute("success", "Vehículo guardado exitosamente");
        return "redirect:/perfil_gestor";
    }

    @GetMapping("/obtener-vehiculo/{id}")
    @ResponseBody
    public Vehiculo obtenerVehiculoParaEdicion(@PathVariable String id) {
        Vehiculo vehiculo = vehiculoService.obtenerPorId(id);
        if (vehiculo.getColores() == null) {
            vehiculo.setColores(new ArrayList<>());
        }
        return vehiculo;
    }

    @PostMapping("/editar-vehiculo/{id}")
    public String editarVehiculo(
            @PathVariable String id,
            @ModelAttribute Vehiculo vehiculo,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam Integer pasajeros,
            @RequestParam String colores,
            @RequestParam String descripcion,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam(value = "galeria1", required = false) MultipartFile galeria1,
            @RequestParam(value = "galeria2", required = false) MultipartFile galeria2,
            @RequestParam(value = "galeria3", required = false) MultipartFile galeria3,
            RedirectAttributes redirectAttributes) {

        try {
            Vehiculo vehiculoExistente = vehiculoService.obtenerPorId(id);

            // Actualizar imagen si se proporciona
            if (imagen != null && !imagen.isEmpty()) {
                vehiculoService.actualizarImagenVehiculo(vehiculoExistente, imagen);
            }

            // Actualizar campos básicos
            vehiculoExistente.setMarca(vehiculo.getMarca());
            vehiculoExistente.setModelo(vehiculo.getModelo());
            vehiculoExistente.setAño(vehiculo.getAño());
            vehiculoExistente.setPrecio(vehiculo.getPrecio());
            vehiculoExistente.setCategoria(vehiculo.getCategoria());
            vehiculoExistente.setMotor(motor);
            vehiculoExistente.setTransmision(transmision);
            vehiculoExistente.setCombustible(combustible);
            vehiculoExistente.setPasajeros(pasajeros);
            vehiculoExistente.setDescripcion(descripcion);

            // Actualizar modelo 3D si se proporciona
            if (modelo3d != null && !modelo3d.isEmpty()) {
                System.out.println("📦 [GestorVehiculoController] Actualizando Modelo 3D...");
                try {
                    String urlModelo = supabaseStorageService.uploadFile(modelo3d);
                    vehiculoExistente.setUrlModelo3d(urlModelo);
                } catch (Exception e) {
                    System.err.println("❌ [GestorVehiculoController] Error actualizando modelo 3D: " + e.getMessage());
                }
            }

            // Procesar colores
            if (colores != null && !colores.isEmpty()) {
                List<String> listaColores = Arrays.stream(colores.split(","))
                        .map(String::trim)
                        .filter(color -> !color.isEmpty())
                        .collect(Collectors.toList());
                vehiculoExistente.setColores(listaColores);
            } else {
                vehiculoExistente.setColores(new ArrayList<>());
            }

            // Procesar galería (añadir nuevas imágenes)
            List<MultipartFile> nuevasImagenes = new ArrayList<>();
            if (galeria1 != null && !galeria1.isEmpty()) nuevasImagenes.add(galeria1);
            if (galeria2 != null && !galeria2.isEmpty()) nuevasImagenes.add(galeria2);
            if (galeria3 != null && !galeria3.isEmpty()) nuevasImagenes.add(galeria3);
            
            if (!nuevasImagenes.isEmpty()) {
                vehiculoService.agregarImagenesGaleria(vehiculoExistente, nuevasImagenes);
            }

            vehiculoService.guardarVehiculo(vehiculoExistente);
            redirectAttributes.addFlashAttribute("success", "Vehículo actualizado exitosamente");
            return "redirect:/perfil_gestor";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar el vehículo: " + e.getMessage());
            return "redirect:/perfil_gestor";
        }
    }

    @GetMapping("/eliminar-vehiculo/{id}")
    public String eliminarVehiculo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        vehiculoService.eliminarVehiculo(id);
        redirectAttributes.addFlashAttribute("success", "Vehículo eliminado exitosamente");
        return "redirect:/perfil_gestor";
    }

    // ==================== MÉTODOS PARA ANUNCIOS ====================

    @PostMapping("/guardar-anuncio")
    public String guardarAnuncio(
            @RequestParam String marca,
            @RequestParam String modelo,
            @RequestParam int año,
            @RequestParam double precio,
            @RequestParam String categoria,
            @RequestParam String motor,
            @RequestParam String transmision,
            @RequestParam String combustible,
            @RequestParam int pasajeros,
            @RequestParam String descripcion,
            @RequestParam String colores,
            @RequestParam(value = "modelo3d", required = false) MultipartFile modelo3d,
            @RequestParam MultipartFile imagen,
            RedirectAttributes redirectAttributes) throws IOException {

        try {
            Vehiculo anuncio = new Vehiculo();
            anuncio.setMarca(marca);
            anuncio.setModelo(modelo);
            anuncio.setAño(año);
            anuncio.setPrecio(precio);
            anuncio.setCategoria(categoria);
            anuncio.setMotor(motor);
            anuncio.setTransmision(transmision);
            anuncio.setCombustible(combustible);
            anuncio.setPasajeros(pasajeros);
            anuncio.setDescripcion(descripcion);
            anuncio.setDestacado(true); // Es un anuncio

            // Subir modelo 3D si existe
            if (modelo3d != null && !modelo3d.isEmpty()) {
                try {
                    String urlModelo = supabaseStorageService.uploadFile(modelo3d);
                    anuncio.setUrlModelo3d(urlModelo);
                } catch (Exception e) {
                    System.err.println("❌ [GestorVehiculoController] Error subiendo modelo 3D anuncio: " + e.getMessage());
                }
            }

            // Procesar colores
            if (colores != null && !colores.isEmpty()) {
                List<String> listaColores = Arrays.stream(colores.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
                anuncio.setColores(listaColores);
            }

            vehiculoService.crearAnuncio(anuncio, imagen);
            redirectAttributes.addFlashAttribute("success", "Anuncio creado exitosamente");
            return "redirect:/perfil_gestor";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el anuncio: " + e.getMessage());
            return "redirect:/perfil_gestor";
        }
    }

    @GetMapping("/eliminar-anuncio/{id}")
    public String eliminarAnuncio(@PathVariable String id, RedirectAttributes redirectAttributes) {
        vehiculoService.eliminarVehiculo(id);
        redirectAttributes.addFlashAttribute("success", "Anuncio eliminado exitosamente");
        return "redirect:/perfil_gestor";
    }
}
