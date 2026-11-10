package com.mockProject.group6.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mockProject.group6.entity.Role;
import com.mockProject.group6.repository.RoleRepository;

@Configuration
public class DataRole {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            seed(roleRepository, "ADMIN");
            seed(roleRepository, "MANAGER");
            seed(roleRepository, "MEMBER");
            seed(roleRepository, "USER");

            if (roleRepository.findByName("MARKETING").isEmpty()) {
                Role marketing = new Role();
                marketing.setName("MARKETING");
                marketing.setStatus(true);
                roleRepository.save(marketing);
            }

            System.out.println("Roles update successfully!");
        };
    }


    private void seed(RoleRepository repo, String name) {
        if (repo.findByName(name).isEmpty()) {
            Role r = new Role();
            r.setName(name);
            r.setStatus(true);
            repo.save(r);
        }
    }
}