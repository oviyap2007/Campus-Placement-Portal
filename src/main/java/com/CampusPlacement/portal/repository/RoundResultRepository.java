package com.CampusPlacement.portal.repository;

import com.CampusPlacement.portal.model.RoundResult;
import com.CampusPlacement.portal.model.RoundResult.ResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoundResultRepository extends JpaRepository<RoundResult, Integer> {
    List<RoundResult> findByApplicationIdOrderByResultId(int applicationId);
    Optional<RoundResult> findByApplicationIdAndRoundId(int applicationId, int roundId);
    List<RoundResult> findByRoundId(int roundId);
    List<RoundResult> findByApplicationIdAndStatus(int applicationId, ResultStatus status);
}
