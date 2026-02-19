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

    private final EvaluationRecordRepository repository;
    private final QuestionCatalogService catalogService;
    private final KeywordScoringService scoringService;

    @Transactional
    public EvaluationRecord saveAnswer(int questionId, String userAnswer) {
        Question question = catalogService.getById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + questionId));

        // ── Phase 2: score the answer ─────────────────────────────────────────
        ScoringResult score = scoringService.score(
                userAnswer,
                question.getRequiredConcepts(),
                question.getAdvancedConcepts()
        );

        EvaluationRecord record = new EvaluationRecord();
        record.setQuestionId(questionId);
        record.setQuestionText(question.getQuestion());
        record.setUserAnswer(userAnswer);
        record.setExperience(question.getExperience());

        record.setFinalScore(score.finalScore());
        record.setRequiredConceptScore(score.requiredConceptScore());
        record.setAdvancedBonus(score.advancedBonus());
        record.setMatchedRequired(join(score.matchedRequired()));
        record.setMatchedAdvanced(join(score.matchedAdvanced()));

        EvaluationRecord saved = repository.save(record);
        log.info("Saved evaluation id={} questionId={} finalScore={}",
                saved.getId(), questionId, score.finalScore());
        return saved;
    }

    public List<EvaluationRecord> getAll() {
        return repository.findAll();
    }

    public Optional<EvaluationRecord> getById(Long id) {
        return repository.findById(id);
    }

    private String join(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        return String.join(", ", items);
    }
}
