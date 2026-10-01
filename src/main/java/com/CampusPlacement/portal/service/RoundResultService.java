package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.Application;
import com.CampusPlacement.portal.model.Application.Status;
import com.CampusPlacement.portal.model.InterviewRound;
import com.CampusPlacement.portal.model.RoundResult;
import com.CampusPlacement.portal.model.RoundResult.ResultStatus;
import com.CampusPlacement.portal.repository.ApplicationRepository;
import com.CampusPlacement.portal.repository.InterviewRoundRepository;
import com.CampusPlacement.portal.repository.RoundResultRepository;
import com.CampusPlacement.portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class RoundResultService {

    @Autowired private RoundResultRepository    resultRepo;
    @Autowired private InterviewRoundRepository roundRepo;
    @Autowired private ApplicationRepository    appRepo;
    @Autowired private StudentRepository        studentRepo;

    /** Enrich results with round metadata */
    private void enrich(List<RoundResult> results) {
        for (RoundResult rr : results) {
            roundRepo.findById(rr.getRoundId()).ifPresent(r -> {
                rr.setRoundName(r.getRoundName());
                rr.setRoundNumber(r.getRoundNumber());
                rr.setRoundTypeIcon(r.getRoundType().getIcon());
                rr.setRoundTypeName(r.getRoundType().getDisplayName());
            });
        }
    }

    /**
     * Initialize round results when company shortlists a student.
     * Round 1 → PENDING, rest → NOT_STARTED
     */
    @Transactional
    public void initializeRounds(int applicationId, int jobId) {
        List<InterviewRound> rounds = roundRepo.findByJobIdOrderByRoundNumber(jobId);
        if (rounds.isEmpty()) return;

        for (int i = 0; i < rounds.size(); i++) {
            InterviewRound r = rounds.get(i);
            // Only create if not already exists
            if (resultRepo.findByApplicationIdAndRoundId(applicationId, r.getRoundId()).isEmpty()) {
                ResultStatus status = (i == 0) ? ResultStatus.PENDING : ResultStatus.NOT_STARTED;
                RoundResult rr = new RoundResult(applicationId, r.getRoundId(), status);
                resultRepo.save(rr);
            }
        }

        // Set application to IN_PROGRESS
        appRepo.findById(applicationId).ifPresent(app -> {
            app.setStatus(Status.IN_PROGRESS);
            appRepo.save(app);
        });
    }

    /**
     * Mark current round as PASSED.
     * Auto-advances next round to PENDING.
     * If no more rounds → application SELECTED, student placed.
     */
    @Transactional
    public String passRound(int applicationId, int roundId, String remarks) {
        Optional<RoundResult> opt = resultRepo.findByApplicationIdAndRoundId(applicationId, roundId);
        if (opt.isEmpty()) return "not_found";

        RoundResult current = opt.get();
        current.setStatus(ResultStatus.PASSED);
        current.setRemarks(remarks != null ? remarks : "");
        resultRepo.save(current);

        // Find all rounds for this job to determine next
        Optional<Application> appOpt = appRepo.findById(applicationId);
        if (appOpt.isEmpty()) return "app_not_found";
        int jobId = appOpt.get().getJobId();

        List<InterviewRound> allRounds = roundRepo.findByJobIdOrderByRoundNumber(jobId);
        List<RoundResult> allResults   = resultRepo.findByApplicationIdOrderByResultId(applicationId);

        // Find the round number of current
        int currentRoundNum = allRounds.stream()
            .filter(r -> r.getRoundId() == roundId)
            .mapToInt(InterviewRound::getRoundNumber).findFirst().orElse(-1);

        // Find next round
        Optional<InterviewRound> nextRound = allRounds.stream()
            .filter(r -> r.getRoundNumber() == currentRoundNum + 1)
            .findFirst();

        if (nextRound.isPresent()) {
            // Advance next round to PENDING
            resultRepo.findByApplicationIdAndRoundId(applicationId, nextRound.get().getRoundId())
                .ifPresent(nr -> {
                    nr.setStatus(ResultStatus.PENDING);
                    resultRepo.save(nr);
                });
            return "advanced";
        } else {
            // All rounds passed — SELECT the student
            appOpt.get().setStatus(Status.SELECTED);
            appRepo.save(appOpt.get());
            studentRepo.findById(appOpt.get().getStudentId()).ifPresent(s -> {
                s.setPlaced(true);
                studentRepo.save(s);
            });
            return "selected";
        }
    }

    /**
     * Mark current round as FAILED → application REJECTED.
     */
    @Transactional
    public String failRound(int applicationId, int roundId, String remarks) {
        Optional<RoundResult> opt = resultRepo.findByApplicationIdAndRoundId(applicationId, roundId);
        if (opt.isEmpty()) return "not_found";

        RoundResult current = opt.get();
        current.setStatus(ResultStatus.FAILED);
        current.setRemarks(remarks != null ? remarks : "");
        resultRepo.save(current);

        // Reject the application
        appRepo.findById(applicationId).ifPresent(app -> {
            app.setStatus(Status.REJECTED);
            appRepo.save(app);
        });

        return "rejected";
    }

    /** Get all round results for an application (enriched with round metadata) */
    public List<RoundResult> getProgressForApplication(int applicationId) {
        List<RoundResult> results = resultRepo.findByApplicationIdOrderByResultId(applicationId);
        enrich(results);
        return results;
    }

    /** Get current PENDING round result for an application */
    public Optional<RoundResult> getCurrentRound(int applicationId) {
        List<RoundResult> pending = resultRepo.findByApplicationIdAndStatus(applicationId, ResultStatus.PENDING);
        if (pending.isEmpty()) return Optional.empty();
        Optional<RoundResult> result = Optional.of(pending.get(0));
        result.ifPresent(rr -> roundRepo.findById(rr.getRoundId()).ifPresent(r -> {
            rr.setRoundName(r.getRoundName());
            rr.setRoundNumber(r.getRoundNumber());
            rr.setRoundTypeIcon(r.getRoundType().getIcon());
        }));
        return result;
    }
}
