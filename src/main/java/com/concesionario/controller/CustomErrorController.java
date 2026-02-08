package com.concesionario.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Obtener información del error
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("jakarta.servlet.error.exception");
        String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");

        if (statusCode == null) {
            statusCode = 500;
        }

        // Agregar información al modelo
        model.addAttribute("status", statusCode);
        model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "Error interno del servidor");

        if (exception != null) {
            model.addAttribute("exception", exception.getMessage());
        } else {
            model.addAttribute("exception", "No hay información adicional disponible");
        }

        model.addAttribute("path", request.getAttribute("jakarta.servlet.error.request_uri"));
        model.addAttribute("timestamp", java.time.LocalDateTime.now());

        return "error"; // Tu template error.html
    }
}