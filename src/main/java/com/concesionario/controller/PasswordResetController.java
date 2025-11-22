package com.concesionario.controller;

import com.concesionario.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.initiatePasswordReset(email);
            System.out.println("Email de recuperación enviado a: " + email);

            // Redirección directa a login con mensaje flash
            redirectAttributes.addFlashAttribute("success",
                    "Se ha enviado un correo con instrucciones para restablecer tu contraseña.");

            return "redirect:/login";

        } catch (Exception e) {
            // En caso de error, volver al formulario con mensaje de error
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        try {
            passwordResetService.validateToken(token);
            model.addAttribute("token", token);
            return "auth/reset-password";
        } catch (Exception e) {
            model.addAttribute("error", "El enlace de recuperación es inválido o ha expirado");
            return "auth/reset-password";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam String token,
            @RequestParam String newPassword,
            Model model) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            System.out.println("Contraseña restablecida exitosamente para token: " + token);

            // Usar close-window para reset-password
            model.addAttribute("closeWindow", true);
            model.addAttribute("redirectUrl", "/login");
            model.addAttribute("success", "¡Contraseña restablecida exitosamente! Esta ventana se cerrará automáticamente.");

            return "auth/close-window";

        } catch (Exception e) {
            System.out.println("Error al restablecer contraseña: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "auth/reset-password";
        }
    }

    // Vista especial para cerrar ventanas (solo para reset-password)
    @GetMapping("/close-window")
    public String closeWindow(@RequestParam(required = false) String redirectUrl,
                              @RequestParam(required = false) String message,
                              Model model) {
        if (redirectUrl != null) {
            model.addAttribute("redirectUrl", redirectUrl);
        }
        if (message != null) {
            model.addAttribute("message", message);
        }
        model.addAttribute("closeWindow", true);
        return "auth/close-window";
    }
}