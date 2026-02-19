package com.sudhird.that_interviewer.controller;

import com.sudhird.that_interviewer.dto.SessionStartResponse;
import com.sudhird.that_interviewer.dto.SummaryResponse;
import com.sudhird.that_interviewer.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping("/start")
    public ResponseEntity<SessionStartResponse> startSession(
            @RequestParam String role,
            @RequestParam(required = false) String experience
    ) {
        return ResponseEntity.ok(sessionService.startSession(role, experience));
    }

    @GetMapping("/{sessionId}/summary")
    public ResponseEntity<SummaryResponse> getSummary(@PathVariable String sessionId) {
        return sessionService.getSummary(sessionId)
                .<ResponseEntity<SummaryResponse>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
