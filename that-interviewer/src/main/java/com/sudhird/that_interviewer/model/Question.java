package com.sudhird.that_interviewer.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Question {
    private String question;
    private String idealAnswer;
    private List<String> requiredConcepts;
    private List<String> advancedConcepts;
    private String difficulty;
    private String experience;
    private String role;
    private Integer id;
}
