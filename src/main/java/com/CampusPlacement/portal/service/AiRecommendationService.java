package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.ai.RecommendationEngine;
import com.CampusPlacement.portal.ai.RecommendationResult;
import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiRecommendationService {

    @Autowired private RecommendationEngine engine;
    @Autowired private JobService           jobService;

    public List<RecommendationResult> getTopRecommendations(Student student, int n) {
        List<Job> activeJobs = jobService.getActiveJobs();
        return engine.getTopN(student, activeJobs, n);
    }
}
