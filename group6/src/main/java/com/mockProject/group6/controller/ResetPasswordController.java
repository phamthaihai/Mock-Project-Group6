package com.mockProject.group6.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import com.mockProject.group6.dto.ForgotPasswordDTO;
import com.mockProject.group6.dto.ResetPasswordDTO;
import com.mockProject.group6.service.MailService;
import com.mockProject.group6.service.UserService;

@Controller
public class ResetPasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto,
                                       BindingResult result,
                                       Model model,
                                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "forgot-password";
        }
        try {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            userService.createPasswordResetToken(dto.getEmail()).ifPresent(token -> {
                String resetLink = UriComponentsBuilder.fromUriString(baseUrl)
                        .path("/reset-password")
                        .queryParam("token", token)
                        .build()
                        .toUriString();
                String body = "Use this link to reset your password within 10 minutes:\n" + resetLink;
                mailService.sendSimpleMail(dto.getEmail(), "Reset your SkillTrail password", body);
            });
        } catch (RuntimeException ignored) {
            // Keep the response identical whether the address exists or mail delivery fails.
        }
        ra.addFlashAttribute("msg", "If this email exists, a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam(value = "token", required = false) String token,
                                        Model model) {
        if (!userService.isPasswordResetTokenValid(token)) {
            model.addAttribute("tokenValid", false);
            model.addAttribute("error", "This reset link is invalid or has expired. Request a new link.");
            return "reset-password";
        }

        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setToken(token);
        model.addAttribute("resetPasswordDTO", dto);
        model.addAttribute("tokenValid", true);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
                                      BindingResult result,
                                      Model model,
                                      RedirectAttributes ra) {
        if (!userService.isPasswordResetTokenValid(dto.getToken())) {
            model.addAttribute("tokenValid", false);
            model.addAttribute("error", "This reset link is invalid or has expired. Request a new link.");
            return "reset-password";
        }
        if (result.hasErrors()) {
            model.addAttribute("tokenValid", true);
            return "reset-password";
        }
        try {
            userService.resetPassword(dto.getToken(), dto.getNewPassword());
            ra.addFlashAttribute("msg", "Password reset successfully. You can login now.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tokenValid", false);
            return "reset-password";
        }
    }
}
