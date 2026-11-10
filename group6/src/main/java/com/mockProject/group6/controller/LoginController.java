package com.mockProject.group6.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.mockProject.group6.dto.LoginDTO;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.service.UserService;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showFormLogin(@ModelAttribute("msg") String msg,
                                @ModelAttribute("err") String err,
                                Model model,
                                HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser != null) {
            if (currentUser.getRole() != null) {
                String roleName = currentUser.getRole().getName();
                if ("ADMIN".equalsIgnoreCase(roleName)
                        || "MANAGER".equalsIgnoreCase(roleName)) {
                    return "redirect:/admin/dashboard";
                }
            }
            return "redirect:/";
        }

        if (!model.containsAttribute("loginDTO")) {
            model.addAttribute("loginDTO", new LoginDTO());
        }

        if (msg != null && !msg.isBlank()) {
            model.addAttribute("success", msg);
        }
        if (err != null && !err.isBlank()) {
            model.addAttribute("error", err);
        }

        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@Valid @ModelAttribute("loginDTO") LoginDTO loginDTO,
                            BindingResult result,
                            Model model,
                            HttpSession session,
                            RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "login";
        }

        User user = userService.login(loginDTO);

        if (user == null) {
            model.addAttribute("error", "Wrong password or account");
            return "login";
        }

        // Lưu user vào session
        session.setAttribute("user", user);
        ra.addFlashAttribute("msg", "Đăng nhập thành công");

        // Điều hướng theo role sau khi đăng nhập
        if (user.getRole() != null) {
            String roleName = user.getRole().getName();
            if ("ADMIN".equalsIgnoreCase(roleName)
                    || "MANAGER".equalsIgnoreCase(roleName)) {
                return "redirect:/admin/dashboard";
            }
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("msg", "Bạn đã đăng xuất");
        return "redirect:/login";
    }
}