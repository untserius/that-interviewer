package com.sudhird.that_interviewer.service;

import com.sudhird.that_interviewer.dto.ScoringResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KeywordScoringService {

    // Phase 3 weights
    private static final double WEIGHT_SIMILARITY = 0.6;
    private static final double WEIGHT_REQUIRED   = 0.3;
    private static final double WEIGHT_ADVANCED   = 0.1;

    /**
     * Score with semantic similarity from the embedding service.
     * Formula: similarity*0.6 + requiredScore*0.3 + advancedBonus*0.1
     */
    public ScoringResult score(
            String userAnswer,
            List<String> requiredConcepts,
            List<String> advancedConcepts,
            Optional<Double> similarityScore
    ) {
        String normalized = userAnswer.toLowerCase();

        List<String> matchedRequired = requiredConcepts.stream()
                .filter(c -> normalized.contains(c.toLowerCase()))
                .toList();

        List<String> matchedAdvanced = advancedConcepts == null ? List.of() :
                advancedConcepts.stream()
                        .filter(c -> normalized.contains(c.toLowerCase()))
                        .toList();

        double requiredScore = requiredConcepts.isEmpty() ? 0.0
                : (double) matchedRequired.size() / requiredConcepts.size();

        double advancedBonus = (advancedConcepts == null || advancedConcepts.isEmpty()) ? 0.0
                : (double) matchedAdvanced.size() / advancedConcepts.size();

        double finalScore = similarityScore
                .map(sim -> (sim * WEIGHT_SIMILARITY) + (requiredScore * WEIGHT_REQUIRED) + (advancedBonus * WEIGHT_ADVANCED))
                .orElse(requiredScore);  // Phase 2 fallback: keyword-only

        return new ScoringResult(
                round(finalScore),
                round(requiredScore),
                round(advancedBonus),
                similarityScore.map(this::round),
                matchedRequired,
                matchedAdvanced,
                requiredConcepts.size(),
                advancedConcepts == null ? 0 : advancedConcepts.size()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

