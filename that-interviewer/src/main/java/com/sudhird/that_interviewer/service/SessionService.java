package com.sudhird.that_interviewer.service;

import com.sudhird.that_interviewer.dto.SessionStartResponse;
import com.sudhird.that_interviewer.dto.SummaryResponse;
import com.sudhird.that_interviewer.model.EvaluationRecord;
import com.sudhird.that_interviewer.model.Question;
import com.sudhird.that_interviewer.repository.EvaluationRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final int SESSION_SIZE = 10;

    private final QuestionCatalogService catalogService;
    private final EvaluationRecordRepository evaluationRepository;

    private final Map<String, SessionMeta> sessions = new ConcurrentHashMap<>();

    public SessionStartResponse startSession(String role, String experience) {
        String sessionId = UUID.randomUUID().toString();
        List<Question> all = new ArrayList<>(catalogService.getAll());

        // Primary pool: match both role AND experience
        List<Question> primary = all.stream()
                .filter(q -> matchesRole(q, role) && matchesExperience(q, experience))
                .collect(Collectors.toList());

        log.info("Primary pool size for role='{}' experience='{}': {}", role, experience, primary.size());

        List<Question> selected = new ArrayList<>();
        Collections.shuffle(primary);
        selected.addAll(primary.subList(0, Math.min(SESSION_SIZE, primary.size())));

        // Pad with same-role, any experience
        if (selected.size() < SESSION_SIZE) {
            List<Question> sameRoleRemainder = all.stream()
                    .filter(q -> matchesRole(q, role) && !selected.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(sameRoleRemainder);
            int needed = SESSION_SIZE - selected.size();
            selected.addAll(sameRoleRemainder.subList(0, Math.min(needed, sameRoleRemainder.size())));
            log.info("Padded with {} same-role questions", selected.size() - primary.size());
        }

        // Pad with anything remaining if still not enough
        if (selected.size() < SESSION_SIZE) {
            List<Question> remainder = all.stream()
                    .filter(q -> !selected.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(remainder);
            int needed = SESSION_SIZE - selected.size();
            selected.addAll(remainder.subList(0, Math.min(needed, remainder.size())));
            log.info("Final pad to {} questions", selected.size());
        }

        List<Integer> questionIds = selected.stream().map(Question::getId).toList();
        sessions.put(sessionId, new SessionMeta(sessionId, role, experience, questionIds));

        List<SessionStartResponse.SessionQuestion> sessionQuestions = selected.stream()
                .map(SessionStartResponse.SessionQuestion::from)
                .toList();

        return new SessionStartResponse(sessionId, role, experience, selected.size(), sessionQuestions);
    }

    public Optional<SummaryResponse> getSummary(String sessionId) {
        SessionMeta meta = sessions.get(sessionId);
        if (meta == null) return Optional.empty();

        List<EvaluationRecord> records = evaluationRepository.findBySessionId(sessionId);

        double totalScore = records.stream()
                .mapToDouble(r -> r.getFinalScore() == null ? 0.0 : r.getFinalScore())
                .average().orElse(0.0);
        double rounded = Math.round(totalScore * 100.0) / 100.0;

        List<SummaryResponse.QuestionSummary> results = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            EvaluationRecord r = records.get(i);
            Question q = catalogService.getById(r.getQuestionId()).orElse(null);
            results.add(new SummaryResponse.QuestionSummary(
                    i + 1, r.getQuestionId(), r.getQuestionText(), r.getUserAnswer(),
                    q != null ? q.getIdealAnswer() : "N/A",
                    orZero(r.getFinalScore()), orZero(r.getRequiredConceptScore()),
                    orZero(r.getAdvancedBonus()), r.getSimilarityScore(),
                    splitCsv(r.getMatchedRequired()), splitCsv(r.getMatchedAdvanced()),
                    Boolean.TRUE.equals(r.getEmbeddingUsed())
            ));
        }

        return Optional.of(new SummaryResponse(
                meta.role(), meta.experience(), records.size(),
                rounded, SummaryResponse.toGrade(rounded), results
        ));
    }

    private boolean matchesRole(Question q, String role) {
        if (role == null || role.isBlank()) return true;
        return role.equalsIgnoreCase(q.getRole());
    }

    private boolean matchesExperience(Question q, String experience) {
        if (experience == null || experience.isBlank()) return true;
        return experience.equalsIgnoreCase(q.getExperience());
    }

    private double orZero(Double d) { return d == null ? 0.0 : d; }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private record SessionMeta(String sessionId, String role, String experience, List<Integer> questionIds) {}
}
