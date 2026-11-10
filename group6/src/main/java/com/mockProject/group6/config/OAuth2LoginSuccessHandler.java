package com.mockProject.group6.config;

import com.mockProject.group6.entity.User;
import com.mockProject.group6.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        // Lấy thông tin email, tên, ảnh từ provider (Google / Facebook)
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = null;
        
        if (oAuth2User.getAttributes().containsKey("picture")) {
            Object picObj = oAuth2User.getAttribute("picture");
            if (picObj instanceof String) {
                picture = (String) picObj;
            } else if (picObj instanceof java.util.Map) {
                // Xử lý riêng cho một số cấu trúc trả về ảnh của Facebook nếu có dạng nested map
                java.util.Map<?, ?> dataMap = (java.util.Map<?, ?>) picObj;
                Object data = dataMap.get("data");
                if (data instanceof java.util.Map) {
                    picture = (String) ((java.util.Map<?, ?>) data).get("url");
                }
            }
        }

        if (name == null) {
            name = oAuth2User.getAttribute("full_name");
        }

        // Gọi hàm xử lý check/tạo mới user và cập nhật profile
        User user = userService.processOAuth2User(email, name, picture);

        // Lưu thông tin user vào Session (phù hợp với cơ chế filter hiện tại của bạn)
        request.getSession().setAttribute("SESSION_USER", user);

        // Chuyển hướng người dùng về trang chủ hoặc dashboard sau khi đăng nhập thành công
        response.sendRedirect("/");
    }
}
