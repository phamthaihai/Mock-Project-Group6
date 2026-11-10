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
import com.mockProject.group6.dto.ChangePasswordDTO;
import com.mockProject.group6.dto.ProfileDTO;
import com.mockProject.group6.entity.User;
import com.mockProject.group6.service.UserService;

@Controller
public class ChangePasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "redirect:/user-profile?tab=password";
    }

    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordDTO") ChangePasswordDTO dto,
                                 BindingResult result,
                                 Model model,
                                 HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("currentUser", user);
            model.addAttribute("profileDTO", new ProfileDTO(
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatar()
            ));
            model.addAttribute("activeTab", "password");
            return "user-profile";
        }

        try {
            userService.changePassword(
                    user.getId(),
                    dto.getOldPassword(),
                    dto.getNewPassword(),
                    dto.getConfirmPassword()
            );

            session.invalidate();
            return "redirect:/login?msg=passwordChanged";
        } catch (RuntimeException e) {
            model.addAttribute("currentUser", user);
            model.addAttribute("profileDTO", new ProfileDTO(
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatar()
            ));
            model.addAttribute("activeTab", "password");
            model.addAttribute("passwordError", e.getMessage());
            return "user-profile";
        }
    }
}