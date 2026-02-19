package com.sudhird.that_interviewer.dto;

import com.sudhird.that_interviewer.model.Question;

import java.util.List;

public record QuestionResponse(
        int id,
        String question,
        List<String> requiredConcepts,
        List<String> advancedConcepts,
        String difficulty,
        String experience
) {
    public static QuestionResponse from(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getQuestion(),
                q.getRequiredConcepts(),
                q.getAdvancedConcepts(),
                q.getDifficulty(),
                q.getExperience()
        );
    }
}
