package com.ddd.demo.service;

import com.ddd.demo.entity.Email;

public interface EmailService {

    String sendEmail(Email email);
    String sendHtmlEmail(Email email);
    String sendEmailWithAttachment(Email email);


}



