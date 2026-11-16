
package com.mockProject.group6.controller;

import com.mockProject.group6.entity.User;
import com.mockProject.group6.service.CourseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final CourseService courseService;

    public HomeController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");

        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
        }

        model.addAttribute("courses", courseService.catalog(null, null));

        return "home";
    }
}
