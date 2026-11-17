package com.mockProject.group6;

import com.mockProject.group6.entity.Role;
import com.mockProject.group6.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class Group6ApplicationTests {

	@Autowired
	private TemplateEngine templateEngine;

	@Test
	void contextLoads() {
	}

	@Test
	void rendersAdminTemplateWithSessionUser() {
		Role role = new Role();
		role.setName("ADMIN");
		User user = new User();
		user.setId(42);
		user.setFullName("Admin User");
		user.setRole(role);

		Context context = new Context();
		context.setVariable("currentUser", user);

		String html = templateEngine.process("admin", context);

		assertTrue(html.contains("userId: 42"));
		assertTrue(html.contains("fullName: \"Admin User\""));
		assertTrue(html.contains("localStorage.setItem('skilltrail-mode', 'live')"));
	}

}
