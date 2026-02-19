package com.sudhird.that_interviewer.dto;

import java.util.List;
import java.util.Optional;

public record ScoringResult(
        double finalScore,
        double requiredConceptScore,
        double advancedBonus,
        Optional<Double> similarityScore,
        List<String> matchedRequired,
        List<String> matchedAdvanced,
        int totalRequired,
        int totalAdvanced
) {
    /** Returns a zero score for skipped answers — no embedding call, no keyword matching. */
    public static ScoringResult zero(List<String> requiredConcepts, List<String> advancedConcepts) {
        return new ScoringResult(
                0.0, 0.0, 0.0,
                Optional.empty(),
                List.of(),
                List.of(),
                requiredConcepts == null ? 0 : requiredConcepts.size(),
                advancedConcepts == null ? 0 : advancedConcepts.size()
        );
    }
}
