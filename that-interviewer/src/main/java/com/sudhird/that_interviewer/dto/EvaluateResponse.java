package com.sudhird.that_interviewer.dto;

import com.sudhird.that_interviewer.model.EvaluationRecord;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record EvaluateResponse(
        long evaluationId,
        int questionId,
        String questionText,
        String userAnswer,
        String experience,
        Instant submittedAt,

        double finalScore,
        double requiredConceptScore,
        double advancedBonus,
        Double similarityScore,          // null when embedding service was down
        boolean embeddingUsed,           // tells client which scoring path was taken

        List<String> matchedRequired,
        List<String> matchedAdvanced,

        String status
) {
    public static EvaluateResponse from(EvaluationRecord r) {
        return new EvaluateResponse(
                r.getId(),
                r.getQuestionId(),
                r.getQuestionText(),
                r.getUserAnswer(),
                r.getExperience(),
                r.getSubmittedAt(),
                orZero(r.getFinalScore()),
                orZero(r.getRequiredConceptScore()),
                orZero(r.getAdvancedBonus()),
                r.getSimilarityScore(),
                Boolean.TRUE.equals(r.getEmbeddingUsed()),
                splitCsv(r.getMatchedRequired()),
                splitCsv(r.getMatchedAdvanced()),
                "evaluated"
        );
    }

    private static double orZero(Double d) {
        return d == null ? 0.0 : d;
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
