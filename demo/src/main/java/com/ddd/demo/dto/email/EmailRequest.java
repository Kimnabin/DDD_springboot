package com.ddd.demo.dto.email;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotEmpty(message = "Recipients list cannot be empty")
    private List<@Email(message = "Each recipient must have a valid email") String> recipients;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private Boolean isHtml = true;

    private List<String> cc;

    private List<String> bcc;

    private Map<String, Object> templateVariables;

    private String templateName;

    private EmailPriority priority = EmailPriority.NORMAL;

    public enum EmailPriority {
        LOW, NORMAL, HIGH
    }
}