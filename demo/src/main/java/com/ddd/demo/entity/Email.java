package com.ddd.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Email {

    private String toEmail;

    private String subject;

    private String body;

    private String attachment;

}
