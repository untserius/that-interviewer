package com.sudhird.that_interviewer.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

import java.time.Instant;

/**
 * Persisted record of a user's answer submission.
 */
@Entity
@Table(name = "evaluation_records")
@Getter
@Setter
@NoArgsConstructor
public class EvaluationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "experience")
    private String experience;

    // ── Phase 2: scoring columns ──────────────────────────────────────────────

    /** Final blended score 0.0–1.0 */
    @Column(name = "final_score")
    private Double finalScore;

    /** matchedRequired / totalRequired */
    @Column(name = "required_concept_score")
    private Double requiredConceptScore;

    /** matchedAdvanced / totalAdvanced */
    @Column(name = "advanced_bonus")
    private Double advancedBonus;

    /** Comma-separated matched required concepts (readable in DB) */
    @Column(name = "matched_required", columnDefinition = "TEXT")
    private String matchedRequired;

    /** Comma-separated matched advanced concepts */
    @Column(name = "matched_advanced", columnDefinition = "TEXT")
    private String matchedAdvanced;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = Instant.now();
    }
}
