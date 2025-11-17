package com.java.TMDTPicnic.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendResetCode(String toEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Password reset confirmation code");

        String htmlContent = """
            <div style="font-family: Arial, sans-serif; font-size: 14px;">
                <p>Your code will expire in <strong>10 minutes</strong>.</p>
                <p>Your verification code is:</p>
                <p style="font-size: 24px; font-weight: bold; color: #2d3748;">%s</p>
            </div>
        """.formatted(code);

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
