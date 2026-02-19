package com.sudhird.that_interviewer.service;

import com.sudhird.that_interviewer.dto.ScoringResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeywordScoringService {

    public ScoringResult score(String userAnswer, List<String> requiredConcepts, List<String> advancedConcepts) {
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

        // Phase 2 final score — will be replaced by weighted formula in Phase 3
        double finalScore = requiredScore;

        return new ScoringResult(
                round(finalScore),
                round(requiredScore),
                round(advancedBonus),
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

