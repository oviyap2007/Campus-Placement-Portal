package com.CampusPlacement.portal.repository;

import com.CampusPlacement.portal.model.InterviewRound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewRoundRepository extends JpaRepository<InterviewRound, Integer> {
    List<InterviewRound> findByJobIdOrderByRoundNumber(int jobId);
    void deleteByJobId(int jobId);
    boolean existsByJobId(int jobId);
}
