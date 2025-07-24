package com.ddd.demo;

import com.ddd.demo.util.EmailSenderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

@SpringBootTest
public class SendEmailTest {

    @Autowired
    private EmailSenderUtil emailSenderUtil;
    @Test
    void sendEmailTest() {
        String to = "ducvucong01@gmail.com";
        String subject = "Test Email";
        String body = "This is a test email sent from the Spring Boot application.";
        emailSenderUtil.sendEmail(to, subject, body);
        // You can add assertions here to verify the email was sent successfully
        System.out.println("Email sent to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }

    @Test
    void sendHtmlEmailTest() throws IOException {
        String to = "ducvucong01@gmail.com";
        String subject = "Test HTML Email";

        // HTML content for the email body
        Resource resource = new ClassPathResource("/templates/OTP.html");
        String htmlBody = new String(resource.getInputStream().readAllBytes());


        String otpCode = "121001";
        htmlBody = htmlBody.replace("{{otpCode}}", otpCode);

        emailSenderUtil.sendHtmlEmail(to, subject, htmlBody);
    }
}
