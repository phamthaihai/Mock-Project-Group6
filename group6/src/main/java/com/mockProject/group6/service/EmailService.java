package com.mockProject.group6.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;


    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    public void sendVerificationEmail(String to, String fullName, String verifyLink) {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("Email service is not configured. Set MAIL_USERNAME and MAIL_PASSWORD.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Verify your account");
        message.setText(
                "Hello " + fullName + ",\n\n" +
                        "Please click the link below to verify your account:\n\n" +
                        verifyLink + "\n\n" +
                        "This link will expire in 30 minutes."
        );

        mailSender.send(message);
    }
}