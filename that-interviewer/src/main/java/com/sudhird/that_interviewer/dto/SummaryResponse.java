package com.sudhird.that_interviewer.dto;

import java.util.Arrays;
import java.util.List;

public record SummaryResponse(
        String role,
        String experience,
        int totalQuestions,
        double totalScore,          // average finalScore across all answers
        String grade,               // A / B / C / D / F
        List<QuestionSummary> results
) {
    public record QuestionSummary(
            int questionNumber,
            int questionId,
            String question,
            String userAnswer,
            String idealAnswer,
            double finalScore,
            double requiredConceptScore,
            double advancedBonus,
            Double similarityScore,
            List<String> matchedRequired,
            List<String> matchedAdvanced,
            boolean embeddingUsed
    ) {}

    public static String toGrade(double score) {
        if (score >= 0.85) return "A";
        if (score >= 0.70) return "B";
        if (score >= 0.55) return "C";
        if (score >= 0.40) return "D";
        return "F";
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
