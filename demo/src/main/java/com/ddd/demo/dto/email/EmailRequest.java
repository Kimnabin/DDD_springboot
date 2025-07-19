package com.ddd.demo.dto.email;

import com.ddd.demo.common.validation.ValidEnum;
import jakarta.validation.constraints.*;
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
    @Size(max = 100, message = "Cannot send to more than 100 recipients at once")
    private List<@Email(message = "Each recipient must have a valid email") String> recipients;

    @NotBlank(message = "Subject is required")
    @Size(min = 1, max = 200, message = "Subject must be between 1 and 200 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    @Size(min = 1, max = 10000, message = "Email body must be between 1 and 10,000 characters")
    private String body;

    @Builder.Default
    private Boolean isHtml = true;

    @Size(max = 50, message = "Cannot have more than 50 CC recipients")
    private List<@Email(message = "Each CC recipient must have a valid email") String> cc;

    @Size(max = 50, message = "Cannot have more than 50 BCC recipients")
    private List<@Email(message = "Each BCC recipient must have a valid email") String> bcc;

    private Map<@NotBlank String, Object> templateVariables;

    @Size(max = 100, message = "Template name must not exceed 100 characters")
    private String templateName;

    @ValidEnum(enumClass = EmailPriority.class, message = "Invalid email priority")
    private String priority = "NORMAL";

    public enum EmailPriority {
        LOW, NORMAL, HIGH, URGENT
    }
}