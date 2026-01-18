package com.concesionario.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        System.err.println("=== ERROR: ARCHIVO DEMASIADO GRANDE ===");
        System.err.println("URL: " + request.getRequestURL());
        
        redirectAttributes.addFlashAttribute("error", "El archivo es demasiado grande. Por favor, sube un archivo menor a 500MB.");
        
        // Intentar redirigir a la misma página si es posible, o al dashboard por defecto
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/admin/Dashboard");
    }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception e, HttpServletRequest request, Model model) {

        // Información detallada del error
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("exception", e.getClass().getSimpleName());
        model.addAttribute("status", 500);
        model.addAttribute("path", request.getRequestURL());
        model.addAttribute("timestamp", java.time.LocalDateTime.now());
        model.addAttribute("stackTrace", getStackTrace(e));

        // Log para debugging
        System.err.println("=== ERROR CAPTURADO ===");
        System.err.println("URL: " + request.getRequestURL());
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();

        return "error";
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}