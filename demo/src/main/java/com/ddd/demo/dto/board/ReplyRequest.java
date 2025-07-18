package com.ddd.demo.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyRequest {

    @NotNull(message = "Board ID is required")
    private Long boardId;

    @NotBlank(message = "Reply text is required")
    @Size(max = 1000, message = "Reply text must not exceed 1000 characters")
    private String replyText;

    @NotBlank(message = "Replier name is required")
    @Size(max = 50, message = "Replier name must not exceed 50 characters")
    private String replier;

    private Long parentReplyId; // For nested replies
}