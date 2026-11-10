package com.mockProject.group6.controller;

import com.mockProject.group6.entity.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }
        return "home";
    }
}
