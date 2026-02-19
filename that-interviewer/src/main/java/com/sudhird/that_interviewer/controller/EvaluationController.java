package com.sudhird.that_interviewer.controller;

import com.sudhird.that_interviewer.dto.EvaluateRequest;
import com.sudhird.that_interviewer.dto.EvaluateResponse;
import com.sudhird.that_interviewer.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * Store a user's answer.
     *
     * Request body:
     * {
     *   "questionId": 4,
     *   "answer": "The lifecycle consists of..."
     * }
     */
    @PostMapping
    public ResponseEntity<EvaluateResponse> submitAnswer(
            @Valid @RequestBody EvaluateRequest request
    ) {
        var record = evaluationService.saveAnswer(request.questionId(), request.answer());
        return ResponseEntity.status(HttpStatus.CREATED).body(EvaluateResponse.from(record));
    }

    /** List all submitted answers */
    @GetMapping
    public List<EvaluateResponse> listAll() {
        return evaluationService.getAll().stream()
                .map(EvaluateResponse::from)
                .toList();
    }

    /** Fetch a single evaluation record by its DB id */
    @GetMapping("/{id}")
    public ResponseEntity<EvaluateResponse> getById(@PathVariable Long id) {
        return evaluationService.getById(id)
                .<ResponseEntity<EvaluateResponse>>map(r -> ResponseEntity.ok(EvaluateResponse.from(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
