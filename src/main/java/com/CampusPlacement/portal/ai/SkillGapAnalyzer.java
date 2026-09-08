package com.CampusPlacement.portal.ai;

import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.model.Student;
import java.util.ArrayList;
import java.util.List;

public class SkillGapAnalyzer {

    public List<String> getMissingSkills(Student student, Job job) {
        List<String> missing = new ArrayList<>();
        String[] required = job.getRequiredSkillArray();
        String studentSkills = student.getSkills() != null
                ? student.getSkills().toLowerCase() : "";
        for (String skill : required) {
            if (!skill.trim().isEmpty()
                    && !studentSkills.contains(skill.trim().toLowerCase())) {
                missing.add(skill.trim());
            }
        }
        return missing;
    }

    public List<String> getMatchedSkills(Student student, Job job) {
        List<String> matched = new ArrayList<>();
        String[] required = job.getRequiredSkillArray();
        String studentSkills = student.getSkills() != null
                ? student.getSkills().toLowerCase() : "";
        for (String skill : required) {
            if (!skill.trim().isEmpty()
                    && studentSkills.contains(skill.trim().toLowerCase())) {
                matched.add(skill.trim());
            }
        }
        return matched;
    }

    public double getSkillMatchScore(Student student, Job job) {
        String[] required = job.getRequiredSkillArray();
        if (required.length == 0) return 1.0;
        return (double) getMatchedSkills(student, job).size() / required.length;
    }

    public double getCgpaMatchScore(Student student, Job job) {
        if (student.getCgpa() >= job.getMinCgpa()) return 1.0;
        if (job.getMinCgpa() == 0) return 1.0;
        return student.getCgpa() / job.getMinCgpa();
    }

    public double getRoleMatchScore(Student student, Job job) {
        String studentRole = student.getPreferredRole();
        String jobRole = job.getJobRole();
        if (studentRole == null || studentRole.trim().isEmpty()) return 0.5;
        if (jobRole == null || jobRole.trim().isEmpty()) return 0.5;
        studentRole = studentRole.trim().toLowerCase();
        jobRole     = jobRole.trim().toLowerCase();
        if (studentRole.equals(jobRole)) return 1.0;
        for (String sw : studentRole.split("\\s+"))
            for (String jw : jobRole.split("\\s+"))
                if (sw.equals(jw)) return 0.7;
        return 0.2;
    }

    public double getDomainMatchScore(Student student, Job job) {
        String domain = job.getDomain();
        if (domain == null || domain.trim().isEmpty()) return 0.5;
        String skills = student.getSkills() != null
                ? student.getSkills().toLowerCase() : "";
        domain = domain.toLowerCase();
        if ((domain.contains("software") || domain.contains("web"))
                && (skills.contains("java") || skills.contains("python") || skills.contains("html")))
            return 0.9;
        if ((domain.contains("data") || domain.contains("analytics"))
                && (skills.contains("python") || skills.contains("sql") || skills.contains("ml")))
            return 0.9;
        if ((domain.contains("ai") || domain.contains("artificial"))
                && (skills.contains("ml") || skills.contains("tensorflow") || skills.contains("python")))
            return 0.9;
        if ((domain.contains("embedded") || domain.contains("hardware"))
                && (skills.contains("c") || skills.contains("embedded")))
            return 0.9;
        return 0.4;
    }
}
