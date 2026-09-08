package com.CampusPlacement.portal.ai;

import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.model.Student;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RecommendationEngine {

    private final SimilarityCalculator calc    = new SimilarityCalculator();
    private final SkillGapAnalyzer     analyzer = new SkillGapAnalyzer();

    public List<RecommendationResult> recommend(Student student, List<Job> jobs) {
        List<RecommendationResult> results = new ArrayList<>();
        double[] studentVec = calc.buildStudentVector(student);

        for (Job job : jobs) {
            double[] jobVec     = calc.buildJobVector(job);
            double cosine       = calc.cosineSimilarity(studentVec, jobVec);
            double skillMatch   = analyzer.getSkillMatchScore(student, job);
            double cgpaMatch    = analyzer.getCgpaMatchScore(student, job);
            double roleMatch    = analyzer.getRoleMatchScore(student, job);
            double domainMatch  = analyzer.getDomainMatchScore(student, job);
            double finalScore   = calc.calculateFinalScore(cosine, skillMatch,
                                        cgpaMatch, roleMatch, domainMatch);

            List<String> reasons   = buildReasons(student, job, skillMatch, cgpaMatch, roleMatch);
            List<String> skillGaps = analyzer.getMissingSkills(student, job);

            results.add(new RecommendationResult(job, finalScore, reasons, skillGaps));
        }

        Collections.sort(results);
        return results;
    }

    public List<RecommendationResult> getTopN(Student student, List<Job> jobs, int n) {
        List<RecommendationResult> all = recommend(student, jobs);
        return all.subList(0, Math.min(n, all.size()));
    }

    private List<String> buildReasons(Student student, Job job,
                                      double skillMatch, double cgpaMatch, double roleMatch) {
        List<String> reasons = new ArrayList<>();
        List<String> matched = analyzer.getMatchedSkills(student, job);
        if (!matched.isEmpty())
            reasons.add("Your skills match: " + String.join(", ", matched));
        if (cgpaMatch >= 1.0)
            reasons.add("Your CGPA (" + student.getCgpa() + ") meets requirement (" + job.getMinCgpa() + ")");
        if (roleMatch >= 0.7)
            reasons.add("Your preferred role matches: " + job.getJobRole());
        if (job.getDomain() != null && !job.getDomain().isEmpty())
            reasons.add("Job is in domain: " + job.getDomain());
        if (reasons.isEmpty())
            reasons.add("This job matches your profile and you meet the CGPA requirement");
        return reasons;
    }
}
