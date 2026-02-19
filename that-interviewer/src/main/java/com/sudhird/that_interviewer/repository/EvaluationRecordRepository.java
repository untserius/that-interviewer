package com.sudhird.that_interviewer.repository;

import com.sudhird.that_interviewer.model.EvaluationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRecordRepository extends JpaRepository<EvaluationRecord, Long> {

    List<EvaluationRecord> findByQuestionId(Integer questionId);


    List<EvaluationRecord> findBySessionId(String sessionId);
    List<EvaluationRecord> findByExperience(String experience);
}
