package com.sudhird.that_interviewer.controller;

import com.sudhird.that_interviewer.dto.QuestionResponse;
import com.sudhird.that_interviewer.service.QuestionCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionCatalogService catalogService;

    @GetMapping("/question")
    public ResponseEntity<?> getQuestion(
            @RequestParam(required = false) String experience
    ) {
        return catalogService.getRandomQuestion(experience)
                .<ResponseEntity<?>>map(q -> ResponseEntity.ok(QuestionResponse.from(q)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
