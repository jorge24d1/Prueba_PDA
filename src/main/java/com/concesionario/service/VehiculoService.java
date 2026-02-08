package com.concesionario.service;

import com.concesionario.model.Vehiculo;
import com.concesionario.repository.VehiculoRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VehiculoService {
    private static final Logger log = LoggerFactory.getLogger(VehiculoService.class);

    private final VehiculoRepository vehiculoRepository;
    private final Cloudinary cloudinary;

    // ELIMINAR esta línea ya que no usaremos uploadDir local
    // @Value("${upload.dir}")
    // private String uploadDir;

    public VehiculoService(VehiculoRepository vehiculoRepository, Cloudinary cloudinary) {
        this.vehiculoRepository = vehiculoRepository;
        this.cloudinary = cloudinary;
    }

    // Métodos para todos los vehículos
    public List<Vehiculo> obtenerTodos() {
        return vehiculoRepository.findAll().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Métodos para vehículos normales (no destacados)
    public List<Vehiculo> obtenerVehiculosNormales() {
        return vehiculoRepository.findByDestacadoFalse();
    }

    public void crearVehiculoNormal(Vehiculo vehiculo, MultipartFile imagen, List<MultipartFile> galeriaImagenes) throws IOException {
        String rutaImagen = guardarImagenEnCloudinary(imagen);
        vehiculo.setImagenUrl(rutaImagen);
        
        // Procesar galería
        if (galeriaImagenes != null && !galeriaImagenes.isEmpty()) {
            List<String> urlsGaleria = new ArrayList<>();
            for (MultipartFile img : galeriaImagenes) {
                if (img != null && !img.isEmpty()) {
                    urlsGaleria.add(guardarImagenEnCloudinary(img));
                }
            }
            vehiculo.setGaleria(urlsGaleria);
        }

        vehiculo.setDestacado(false); // Asegura que no sea destacado
        vehiculoRepository.save(vehiculo);
    }

    // Métodos para anuncios destacados
    public List<Vehiculo> obtenerDestacados() {
        try {
            List<Vehiculo> destacados = vehiculoRepository.findByDestacadoTrue();
            return destacados != null ? destacados : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error al obtener vehiculos destacados", e);
            return Collections.emptyList();
        }
    }

    public void crearAnuncio(Vehiculo vehiculo, MultipartFile imagen) throws IOException {
        String rutaImagen = guardarImagenEnCloudinary(imagen);
        vehiculo.setImagenUrl(rutaImagen);
        vehiculo.setDestacado(true); // Asegura que sea destacado
        vehiculoRepository.save(vehiculo);
    }

    public void crearAnuncioCompleto(Vehiculo vehiculo, MultipartFile imagen) throws IOException {
        String rutaImagen = guardarImagenEnCloudinary(imagen);
        vehiculo.setImagenUrl(rutaImagen);
        vehiculo.setDestacado(true); // Asegura que sea destacado
        vehiculoRepository.save(vehiculo);
    }

    // Métodos comunes
    public Vehiculo obtenerPorId(String id) {
        return vehiculoRepository.findById(id).orElse(null);
    }

    public void guardarVehiculo(Vehiculo vehiculo) {
        vehiculoRepository.save(vehiculo);
    }

    public void eliminarVehiculo(String id) {
        vehiculoRepository.deleteById(id);
    }

    public List<Vehiculo> obtenerPorCategoria(String categoria) {
        return vehiculoRepository.findByCategoria(categoria);
    }

    public long contarTodosVehiculos() {
        return vehiculoRepository.count();
    }

    public long contarVehiculosNormales() {
        return vehiculoRepository.countByDestacadoFalse();
    }

    public long contarAnuncios() {
        return vehiculoRepository.countByDestacadoTrue();
    }

    // ==========================================
    // MÉTODO DE BÚSQUEDA AVANZADA PARA N8N / API
    // ==========================================
    public List<Vehiculo> buscarVehiculos(String marca, String modelo, 
                                          Integer anioMin, Integer anioMax, 
                                          Double precioMin, Double precioMax, 
                                          String categoria) {
        
        // 1. Obtener todos (o usar Criteria si fuera más complejo)
        List<Vehiculo> todos = obtenerTodos();

        // 2. Filtrar con Stream
        return todos.stream()
                .filter(v -> {
                    // Filtro Marca
                    if (marca != null && !marca.isEmpty() && 
                        (v.getMarca() == null || !v.getMarca().toLowerCase().contains(marca.toLowerCase()))) {
                        return false;
                    }
                    // Filtro Modelo
                    if (modelo != null && !modelo.isEmpty() && 
                        (v.getModelo() == null || !v.getModelo().toLowerCase().contains(modelo.toLowerCase()))) {
                        return false;
                    }
                    // Filtro Año Min
                    if (anioMin != null && v.getAño() < anioMin) return false;
                    // Filtro Año Max
                    if (anioMax != null && v.getAño() > anioMax) return false;
                    // Filtro Precio Min
                    if (precioMin != null && v.getPrecio() < precioMin) return false;
                    // Filtro Precio Max
                    if (precioMax != null && v.getPrecio() > precioMax) return false;
                    // Filtro Categoria
                    if (categoria != null && !categoria.isEmpty() && 
                        (v.getCategoria() == null || !v.getCategoria().equalsIgnoreCase(categoria))) {
                        return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }


    private String guardarImagenEnCloudinary(MultipartFile imagen) throws IOException {
        try {

            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", "auto_plus/vehiculos");


            Map<String, Object> uploadResult = cloudinary.uploader()
                    .upload(imagen.getBytes(), uploadOptions);

            String imageUrl = uploadResult.get("url").toString();
            String publicId = uploadResult.get("public_id").toString();

            System.out.println("✅ Imagen subida EXITOSAMENTE");
            System.out.println("📁 URL: " + imageUrl);
            System.out.println("📂 Public ID: " + publicId);
            System.out.println("📊 Respuesta completa: " + uploadResult);

            return imageUrl;

        } catch (Exception e) {
            System.err.println(" Error: " + e.getMessage());
            throw new IOException("Error al subir la imagen: " + e.getMessage(), e);
        }
    }
    public void actualizarImagenVehiculo(Vehiculo vehiculo, MultipartFile imagen) throws IOException {
        String rutaImagen = guardarImagenEnCloudinary(imagen);
        vehiculo.setImagenUrl(rutaImagen);
    }

    public void agregarImagenesGaleria(Vehiculo vehiculo, List<MultipartFile> nuevasImagenes) throws IOException {
        if (nuevasImagenes == null || nuevasImagenes.isEmpty()) return;

        List<String> galeriaActual = vehiculo.getGaleria();
        if (galeriaActual == null) {
            galeriaActual = new ArrayList<>();
        }

        for (MultipartFile img : nuevasImagenes) {
            if (img != null && !img.isEmpty()) {
                galeriaActual.add(guardarImagenEnCloudinary(img));
            }
        }
        vehiculo.setGaleria(galeriaActual);
        // Nota: no guardamos aquí, el controlador llama a guardarVehiculo después
    }
}