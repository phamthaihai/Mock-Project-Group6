package com.mockProject.group6.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender sender;
    private final String from;

    public MailService(JavaMailSender sender,
                       @Value("${skilltrail.mail.from:${spring.mail.username:no-reply@skilltrail.local}}") String from) {
        this.sender = sender;
        this.from = from;
    }

    /** Throws org.springframework.mail.MailException if the message could not be sent. */
    public void send(String to, String subject, String text) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(to);
        m.setSubject(subject);
        m.setText(text);
        sender.send(m);
    }

    public void sendVerificationCode(String to, String code) {
        send(to, "SkillTrail verification code", "Your SkillTrail verification code is " + code + ".\n"
                + "It expires in 10 minutes. If you did not sign up, you can ignore this email.");
    }

    public void sendResetCode(String to, String code) {
        send(to, "SkillTrail password reset code", "Your SkillTrail password reset code is " + code + ".\n"
                + "It expires in 10 minutes. If you did not ask for it, you can ignore this email.");
    }
}
