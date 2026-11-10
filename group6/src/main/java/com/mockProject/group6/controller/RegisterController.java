package com.mockProject.group6.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.mockProject.group6.dto.RegisterDTO;
import com.mockProject.group6.service.AuthService;

@Controller
public class RegisterController {

    private final AuthService authService;

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String showRegister(@ModelAttribute("msg") String msg,
                               @ModelAttribute("err") String err,
                               Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }

        if (msg != null && !msg.isBlank()) {
            model.addAttribute("msg", msg);
        }
        if (err != null && !err.isBlank()) {
            model.addAttribute("err", err);
        }

        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("registerDTO") RegisterDTO dto,
                             BindingResult br,
                             RedirectAttributes ra,
                             Model model) {
        if (br.hasErrors()) {
            return "register";
        }

        try {
            String baseUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .build()
                    .toUriString();

            authService.register(dto, baseUrl);
            ra.addFlashAttribute("msg", "Register success. Please check your email to verify before logging in.");
            return "redirect:/login";
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.toLowerCase().contains("email")) {
                br.rejectValue("email", "duplicate", "Email is already registered");
            } else if (message != null && message.toLowerCase().contains("name")) {
                br.rejectValue("fullname", "duplicate", "Full name is already registered");
            } else {
                model.addAttribute("err", "Registration could not be completed. Please try again.");
            }
            return "register";
        }
    }

    @GetMapping("/verify")
    public String verify(@RequestParam("token") String token, RedirectAttributes ra) {
        try {
            authService.verifyEmail(token);
            ra.addFlashAttribute("msg", "Email verified successfully. You can log in now.");
        } catch (Exception e) {
            ra.addFlashAttribute("err", e.getMessage());
        }

        return "redirect:/login";
    }
}