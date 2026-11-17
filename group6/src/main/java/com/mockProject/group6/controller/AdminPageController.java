package com.mockProject.group6.controller;

import com.mockProject.group6.entity.User;
import com.mockProject.group6.service.AdminAccess;
import com.mockProject.group6.service.AdminApiException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    private final AdminAccess adminAccess;

    public AdminPageController(AdminAccess adminAccess) {
        this.adminAccess = adminAccess;
    }

    @GetMapping("/admin")
    public String dashboard(HttpSession session, Model model) {
        try {
            User user = adminAccess.requireAdmin(session);
            model.addAttribute("currentUser", user);
            return "admin";
        } catch (AdminApiException exception) {
            return exception.getStatus() == 401 ? "redirect:/login" : "redirect:/";
        }
    }
}
