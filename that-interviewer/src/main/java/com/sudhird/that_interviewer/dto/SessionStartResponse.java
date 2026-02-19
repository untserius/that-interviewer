package com.sudhird.that_interviewer.dto;

import com.sudhird.that_interviewer.model.Question;

import java.util.List;

public record SessionStartResponse(
        String sessionId,
        String role,
        String experience,
        int totalQuestions,
        List<SessionQuestion> questions
) {
    public record SessionQuestion(
            int questionId,
            String question,
            List<String> requiredConcepts,
            List<String> advancedConcepts,
            String difficulty
    ) {
        public static SessionQuestion from(Question q) {
            return new SessionQuestion(
                    q.getId(),
                    q.getQuestion(),
                    q.getRequiredConcepts(),
                    q.getAdvancedConcepts(),
                    q.getDifficulty()
            );
        }
    }
}
