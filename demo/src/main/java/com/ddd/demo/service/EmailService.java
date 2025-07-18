package com.ddd.demo.service;

import com.ddd.demo.dto.email.EmailRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface EmailService {

    // Send simple text email
    void sendSimpleEmail(String to, String subject, String body);

    // Send HTML email
    void sendHtmlEmail(String to, String subject, String htmlBody);

    // Send email with attachment
    void sendEmailWithAttachment(String to, String subject, String body, MultipartFile attachment);

    // Send templated email
    void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables);

    // Send bulk emails
    void sendBulkEmails(EmailRequest emailRequest);

    // Send OTP email
    void sendOtpEmail(String to, String otp);

    // Send password reset email
    void sendPasswordResetEmail(String to, String resetToken);

    // Send welcome email
    void sendWelcomeEmail(String to, String username);

    // Send order confirmation email
    void sendOrderConfirmationEmail(String to, Long orderId, Map<String, Object> orderDetails);
}