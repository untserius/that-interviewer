package com.sudhird.that_interviewer.service;


import com.sudhird.that_interviewer.dto.ScoringResult;
import com.sudhird.that_interviewer.model.EvaluationRecord;
import com.sudhird.that_interviewer.model.Question;
import com.sudhird.that_interviewer.repository.EvaluationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private static final String SKIPPED_MARKER = "— skipped —";
    private static final int MIN_ANSWER_LENGTH = 10;

    private final EvaluationRecordRepository repository;
    private final QuestionCatalogService catalogService;
    private final KeywordScoringService scoringService;
    private final EmbeddingClient embeddingClient;

    @Transactional
    public EvaluationRecord saveAnswer(int questionId, String userAnswer, String sessionId) {
        Question question = catalogService.getById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        boolean skipped = isSkipped(userAnswer);

        ScoringResult score;
        if (skipped) {
            // Force zero scores — no point calling the embedding service
            score = ScoringResult.zero(question.getRequiredConcepts(), question.getAdvancedConcepts());
        } else {
            Optional<Double> similarity = embeddingClient.getSimilarity(userAnswer, question.getIdealAnswer());
            score = scoringService.score(userAnswer, question.getRequiredConcepts(), question.getAdvancedConcepts(), similarity);
        }

        EvaluationRecord record = new EvaluationRecord();
        record.setSessionId(sessionId);
        record.setQuestionId(questionId);
        record.setQuestionText(question.getQuestion());
        record.setUserAnswer(userAnswer);
        record.setExperience(question.getExperience());
        record.setFinalScore(score.finalScore());
        record.setRequiredConceptScore(score.requiredConceptScore());
        record.setAdvancedBonus(score.advancedBonus());
        record.setSimilarityScore(score.similarityScore().orElse(null));
        record.setMatchedRequired(join(score.matchedRequired()));
        record.setMatchedAdvanced(join(score.matchedAdvanced()));
        record.setEmbeddingUsed(score.similarityScore().isPresent());

        EvaluationRecord saved = repository.save(record);
        log.info("Saved evaluation id={} sessionId={} skipped={} finalScore={}",
                saved.getId(), sessionId, skipped, score.finalScore());
        return saved;
    }

    private boolean isSkipped(String answer) {
        if (answer == null) return true;
        String trimmed = answer.trim();
        return trimmed.isEmpty()
                || trimmed.equals(SKIPPED_MARKER)
                || trimmed.length() < MIN_ANSWER_LENGTH;
    }

    public List<EvaluationRecord> getAll() { return repository.findAll(); }

    public Optional<EvaluationRecord> getById(Long id) { return repository.findById(id); }

    private String join(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        return String.join(", ", items);
    }
}

