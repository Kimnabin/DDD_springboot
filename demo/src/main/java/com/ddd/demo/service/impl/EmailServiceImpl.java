package com.ddd.demo.service.impl;

import com.ddd.demo.entity.Email;
import com.ddd.demo.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public String sendEmail(Email email) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email.getToEmail());
        message.setSubject(email.getSubject());
        message.setText(email.getBody());
        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to " + email.getToEmail());
            return "Email sent successfully";
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            return "Error sending email: " + e.getMessage();
        }

    }

    @Override
    public String sendHtmlEmail(Email email) {
        try {
            // Create a MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true);
            helper.setTo(email.getToEmail());
            helper.setSubject(email.getSubject());
            helper.setText(email.getBody(), true); // true indicates HTML content

            // Send the email
            mailSender.send(message);
            System.out.println("HTML email sent to: " + email.getToEmail());
            return "HTML email sent successfully";
        } catch (Exception e) {
            System.err.println("Error sending HTML email: " + e.getMessage());
            return "Error sending HTML email: " + e.getMessage();
        }
    }

    @Override
    public String sendEmailWithAttachment(Email email) {
        return "";
    }

//    @Override
//    public String sendEmailWithAttachment(Email email) {
//        try {
//            // Create a MimeMessage
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//            helper.setTo(email.getToEmail());
//            helper.setSubject(email.getSubject());
//            helper.setText(email.getBody(), true); // true indicates HTML content
//
//            // Add attachment if present
//            if (email.getAttachment() != null) {
//                helper.addAttachment(email.getAttachment().getOriginalFilename(), email.getAttachment());
//            }
//
//            // Send the email
//            mailSender.send(message);
//            System.out.println("Email with attachment sent to: " + email.getToEmail());
//            return "Email with attachment sent successfully";
//        } catch (Exception e) {
//            System.err.println("Error sending email with attachment: " + e.getMessage());
//            return "Error sending email with attachment: " + e.getMessage();
//        }
//    }
}
