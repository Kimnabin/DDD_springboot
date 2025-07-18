package com.ddd.demo.service.impl;

import com.ddd.demo.common.exception.BusinessException;
import com.ddd.demo.dto.email.EmailRequest;
import com.ddd.demo.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new BusinessException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new BusinessException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendEmailWithAttachment(String to, String subject, String body, MultipartFile attachment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            // Add attachment
            if (attachment != null && !attachment.isEmpty()) {
                helper.addAttachment(attachment.getOriginalFilename(),
                        new ByteArrayResource(attachment.getBytes()));
            }

            mailSender.send(message);
            log.info("Email with attachment sent successfully to: {}", to);
        } catch (MessagingException | IOException e) {
            log.error("Failed to send email with attachment to: {}", to, e);
            throw new BusinessException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // Create Thymeleaf context
            Context context = new Context();
            context.setVariables(variables);

            // Process template
            String htmlContent = templateEngine.process(templateName, context);

            // Send email
            sendHtmlEmail(to, subject, htmlContent);

            log.info("Templated email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send templated email to: {}", to, e);
            throw new BusinessException("Failed to send email: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendBulkEmails(EmailRequest emailRequest) {
        emailRequest.getRecipients().forEach(recipient -> {
            try {
                sendHtmlEmail(recipient, emailRequest.getSubject(), emailRequest.getBody());
            } catch (Exception e) {
                log.error("Failed to send bulk email to: {}", recipient, e);
            }
        });
    }

    @Override
    @Async
    public void sendOtpEmail(String to, String otp) {
        Map<String, Object> variables = Map.of(
                "otpCode", otp,
                "validity", "5 minutes"
        );

        sendTemplatedEmail(to, "OTP Verification", "email/otp", variables);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        Map<String, Object> variables = Map.of(
                "resetLink", "http://localhost:8080/reset-password?token=" + resetToken,
                "validity", "24 hours"
        );

        sendTemplatedEmail(to, "Password Reset Request", "email/password-reset", variables);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String username) {
        Map<String, Object> variables = Map.of(
                "username", username,
                "loginLink", "http://localhost:8080/login"
        );
        sendTemplatedEmail(to, "Welcome to Our Service", "email/welcome", variables);
        log.info("Welcome email sent successfully to: {}", to);
    }

    @Override
    public void sendOrderConfirmationEmail(String to, Long orderId, Map<String, Object> orderDetails) {

        Map<String, Object> variables = Map.of(
                "orderId", orderId,
                "orderDetails", orderDetails
        );

        sendTemplatedEmail(to, "Order Confirmation - #" + orderId, "email/order-confirmation", variables);
        log.info("Order confirmation email sent successfully to: {}", to);
    }
}