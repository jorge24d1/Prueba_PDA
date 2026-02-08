package com.concesionario.controller;

import com.concesionario.model.Usuario;
import com.concesionario.model.Administrador;
import com.concesionario.model.Rol;

import com.concesionario.service.UsuarioService;
import com.concesionario.service.AdministradorService;
import com.concesionario.service.ValidacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    @GetMapping("/error-page")
    public String errorPage() {
        return "error"; // Devuelve src/main/resources/templates/error.html
    }

    @Autowired
    private ValidacionService validacionService;

    private final UsuarioService usuarioService;
    private final AdministradorService administradorService;

    public AuthController(UsuarioService usuarioService, AdministradorService administradorService, ValidacionService validacionService) {
        this.usuarioService = usuarioService;
        this.administradorService = administradorService;
        this.validacionService = validacionService;
    }



    @GetMapping("/login")
    public String showLogin(@RequestParam(required = false) boolean error,
                            Model model) {
        if (error) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }

        return "usuario/login";
    }

    @GetMapping("/registro")
    public String showRegister() {
        return "usuario/loginup";
    }

    @PostMapping("/registro")
    public String registerUser(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String identificacion,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {


        Optional<String> errorValidacion = validacionService.validarCorreoEIdentificacion(email, identificacion);
        if (errorValidacion.isPresent()) {
            model.addAttribute("error", errorValidacion.get());
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("email", email);
            model.addAttribute("identificacion", identificacion);
            return "usuario/loginup";
        }

        try {
            usuarioService.registrarUsuario(
                    nombre,
                    apellido,
                    email,
                    identificacion,
                    password,
                    Rol.USUARIO
            );
            return "redirect:/login?success=Registro+exitoso";

        } catch (Exception e) {
            model.addAttribute("error", "Error durante el registro: " + e.getMessage());
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("email", email);
            model.addAttribute("identificacion", identificacion);
            return "usuario/loginup";
        }
    }

    @GetMapping("/registro-admin")
    public String mostrarRegistroAdmin() {
        return "usuario/registro-admin";
    }

    @PostMapping("/registro-admin")
    public String registrarAdmin(
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String identificacion,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {


        Optional<String> errorValidacion = validacionService.validarCorreoEIdentificacion(email, identificacion);
        if (errorValidacion.isPresent()) {
            model.addAttribute("error", errorValidacion.get());
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("email", email);
            model.addAttribute("identificacion", identificacion);
            return "usuario/registro-admin";
        }

        try {
            administradorService.registrarAdministrador(
                    nombre,
                    apellido,
                    identificacion,
                    email,
                    password
            );
            return "redirect:/login?adminRegistrado=true";

        } catch (Exception e) {
            model.addAttribute("error", "Error durante el registro: " + e.getMessage());
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellido", apellido);
            model.addAttribute("email", email);
            model.addAttribute("identificacion", identificacion);
            return "usuario/registro-admin";
        }
    }
}