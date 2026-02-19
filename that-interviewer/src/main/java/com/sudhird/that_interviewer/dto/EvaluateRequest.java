package com.sudhird.that_interviewer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EvaluateRequest(
        @NotNull(message = "questionId is required")
        Integer questionId,

        @NotBlank(message = "answer must not be blank")
        String answer
) {}
