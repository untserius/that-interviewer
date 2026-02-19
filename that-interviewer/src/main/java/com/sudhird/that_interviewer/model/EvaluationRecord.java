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

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "experience")
    private String experience;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(name = "required_concept_score")
    private Double requiredConceptScore;

    @Column(name = "advanced_bonus")
    private Double advancedBonus;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "matched_required", columnDefinition = "TEXT")
    private String matchedRequired;

    @Column(name = "matched_advanced", columnDefinition = "TEXT")
    private String matchedAdvanced;

    @Column(name = "embedding_used")
    private Boolean embeddingUsed;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = Instant.now();
    }
}
