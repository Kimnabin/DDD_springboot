package com.ddd.demo.controller.email;

import com.ddd.demo.entity.Email;
import com.ddd.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendTextEmail")
    public String sendTextEmail(@RequestBody Email email) {
        return emailService.sendEmail(email);
    }

    @PostMapping("/sendHtmlEmail")
    public String sendHtmlEmail(@RequestBody Email email) {
        return emailService.sendHtmlEmail(email);
    }
}
