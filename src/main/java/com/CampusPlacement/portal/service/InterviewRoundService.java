package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.InterviewRound;
import com.CampusPlacement.portal.repository.InterviewRoundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewRoundService {

    @Autowired private InterviewRoundRepository roundRepo;

    /** Get all rounds for a job ordered by round number */
    public List<InterviewRound> getRoundsForJob(int jobId) {
        return roundRepo.findByJobIdOrderByRoundNumber(jobId);
    }

    /** Check whether a job has any rounds defined */
    public boolean hasRounds(int jobId) {
        return roundRepo.existsByJobId(jobId);
    }

    /** Save a single round */
    public InterviewRound save(InterviewRound round) {
        return roundRepo.save(round);
    }

    /** Replace all rounds for a job (used when company edits job rounds) */
    @Transactional
    public void replaceRounds(int jobId, List<InterviewRound> rounds) {
        roundRepo.deleteByJobId(jobId);
        for (int i = 0; i < rounds.size(); i++) {
            InterviewRound r = rounds.get(i);
            r.setJobId(jobId);
            r.setRoundNumber(i + 1);
            roundRepo.save(r);
        }
    }

    public Optional<InterviewRound> getById(int roundId) {
        return roundRepo.findById(roundId);
    }
}
