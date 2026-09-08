package com.CampusPlacement.portal.ai;

import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.model.Student;

public class SimilarityCalculator {

    private static final String[] ALL_SKILLS = {
        "java", "python", "sql", "spring boot", "rest api", "html", "css",
        "javascript", "react", "c", "c++", "dsa", "git", "ml", "tensorflow",
        "numpy", "tableau", "excel", "embedded systems", "vlsi", "bootstrap"
    };

    public double[] buildStudentVector(Student student) {
        double[] v = new double[ALL_SKILLS.length + 2];
        String skills = student.getSkills() != null
                ? student.getSkills().toLowerCase() : "";
        for (int i = 0; i < ALL_SKILLS.length; i++)
            v[i] = skills.contains(ALL_SKILLS[i]) ? 1.0 : 0.0;
        v[ALL_SKILLS.length]     = student.getCgpa() / 10.0;
        v[ALL_SKILLS.length + 1] = (student.getPreferredRole() != null
                && !student.getPreferredRole().trim().isEmpty()) ? 1.0 : 0.5;
        return v;
    }

    public double[] buildJobVector(Job job) {
        double[] v = new double[ALL_SKILLS.length + 2];
        String skills = job.getRequiredSkills() != null
                ? job.getRequiredSkills().toLowerCase() : "";
        for (int i = 0; i < ALL_SKILLS.length; i++)
            v[i] = skills.contains(ALL_SKILLS[i]) ? 1.0 : 0.0;
        v[ALL_SKILLS.length]     = job.getMinCgpa() / 10.0;
        v[ALL_SKILLS.length + 1] = (job.getJobRole() != null
                && !job.getJobRole().trim().isEmpty()) ? 1.0 : 0.5;
        return v;
    }

    public double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public double calculateFinalScore(double cosine, double skill,
                                      double cgpa, double role, double domain) {
        return (cosine * 0.30) + (skill * 0.35) + (cgpa * 0.15)
             + (role  * 0.12) + (domain * 0.08);
    }
}
