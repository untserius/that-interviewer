package com.sudhird.that_interviewer.service;

import com.sudhird.that_interviewer.model.Question;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class QuestionCatalogService {

    private final ObjectMapper objectMapper;
    private List<Question> catalog = Collections.emptyList();
    private final Random random = new Random();

    public QuestionCatalogService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void loadCatalog() {
        try {
            InputStream is = new ClassPathResource("java_backend_questions.json").getInputStream();
            List<Question> questions = objectMapper.readValue(is, new TypeReference<>() {});

            // Assign stable IDs based on position
            for (int i = 0; i < questions.size(); i++) {
                questions.get(i).setId(i);
            }

            this.catalog = Collections.unmodifiableList(questions);
            log.info("Loaded {} questions from catalog", catalog.size());

        } catch (IOException e) {
            log.error("Failed to load questions.json", e);
            throw new IllegalStateException("Cannot start without question catalog", e);
        }
    }

    /**
     * Returns a random question, optionally filtered by experience bracket.
     * If no match is found for the given experience, returns from the full catalog.
     */
    public Optional<Question> getRandomQuestion(String experience) {
        List<Question> pool = catalog;

        if (experience != null && !experience.isBlank()) {
            List<Question> filtered = catalog.stream()
                    .filter(q -> experience.equalsIgnoreCase(q.getExperience()))
                    .toList();

            if (!filtered.isEmpty()) {
                pool = filtered;
            } else {
                log.warn("No questions found for experience='{}', falling back to full catalog", experience);
            }
        }

        if (pool.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(pool.get(random.nextInt(pool.size())));
    }

    public Optional<Question> getById(int id) {
        if (id < 0 || id >= catalog.size()) return Optional.empty();
        return Optional.of(catalog.get(id));
    }

    public List<Question> getAll() {
        return catalog;
    }
}
