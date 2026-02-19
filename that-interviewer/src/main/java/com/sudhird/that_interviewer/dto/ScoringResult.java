package com.sudhird.that_interviewer.dto;

import java.util.List;

public record ScoringResult(
        double finalScore,           // 0.0 – 1.0  (Phase 2: == requiredScore)
        double requiredConceptScore, // matchedRequired / totalRequired
        double advancedBonus,        // matchedAdvanced / totalAdvanced
        List<String> matchedRequired,
        List<String> matchedAdvanced,
        int totalRequired,
        int totalAdvanced
) {}
