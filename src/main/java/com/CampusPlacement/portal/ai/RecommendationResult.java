package com.CampusPlacement.portal.ai;

import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.model.Student;
import java.util.List;

public class RecommendationResult implements Comparable<RecommendationResult> {

    private Job job;
    private double score;
    private List<String> reasons;
    private List<String> skillGaps;

    public RecommendationResult(Job job, double score,
                                List<String> reasons, List<String> skillGaps) {
        this.job       = job;
        this.score     = score;
        this.reasons   = reasons;
        this.skillGaps = skillGaps;
    }

    public Job getJob()                { return job; }
    public double getScore()           { return score; }
    public List<String> getReasons()   { return reasons; }
    public List<String> getSkillGaps() { return skillGaps; }

    public int getScorePercent()       { return (int) Math.round(score * 100); }

    public String getMatchLabel() {
        if (score >= 0.85) return "Excellent Match";
        if (score >= 0.70) return "Good Match";
        if (score >= 0.50) return "Moderate Match";
        return "Low Match";
    }

    public String getMatchBadgeClass() {
        if (score >= 0.85) return "badge-excellent";
        if (score >= 0.70) return "badge-good";
        if (score >= 0.50) return "badge-moderate";
        return "badge-low";
    }

    @Override
    public int compareTo(RecommendationResult other) {
        return Double.compare(other.score, this.score); // highest first
    }
}
