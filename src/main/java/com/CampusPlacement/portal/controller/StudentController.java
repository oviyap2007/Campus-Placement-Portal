package com.CampusPlacement.portal.controller;

import com.CampusPlacement.portal.ai.RecommendationResult;
import com.CampusPlacement.portal.model.Application;
import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.model.Student;
import com.CampusPlacement.portal.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    // Folder to save uploaded files (inside static so they are served directly)
    private static final String UPLOAD_DIR =
        "C:/Users/Oviya/Downloads/CampusplacementPortal/CampusplacementPortal/src/main/resources/static/uploads/";

    @Autowired private StudentService          studentService;
    @Autowired private JobService              jobService;
    @Autowired private ApplicationService      appService;
    @Autowired private AiRecommendationService aiService;
    @Autowired private RoundResultService      roundResultService;

    private Student getStudent(HttpSession session) {
        return (Student) session.getAttribute("student");
    }

    private String checkLogin(HttpSession session) {
        if (getStudent(session) == null) return "redirect:/login";
        return null;
    }

    // ── Dashboard ────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String redir = checkLogin(session); if (redir != null) return redir;
        Student student = getStudent(session);

        // Basic stats
        List<Job> eligibleJobsList = jobService.getEligibleJobs(student.getCgpa());
        List<Application> myAppsList = appService.getByStudent(student.getStudentId());
        model.addAttribute("student",      student);
        model.addAttribute("eligibleJobs", eligibleJobsList.size());
        model.addAttribute("myApps",       myAppsList.size());

        // ── AI Career Match Score ────────────────────────────────────────────
        List<RecommendationResult> recs = aiService.getTopRecommendations(student, 5);
        int careerMatchScore = 0;
        if (!recs.isEmpty()) {
            double avg = recs.stream().mapToDouble(RecommendationResult::getScore).average().orElse(0);
            careerMatchScore = (int) Math.round(avg * 100);
        }
        model.addAttribute("careerMatchScore", careerMatchScore);
        model.addAttribute("careerMatchLabel",
            careerMatchScore >= 85 ? "Excellent Match" :
            careerMatchScore >= 70 ? "Good Match" :
            careerMatchScore >= 50 ? "Fair Match" : "Building Profile");

        // ── Top 3 Recommended Career Paths ──────────────────────────────────
        List<Map<String, Object>> topPaths = recs.stream().limit(3).map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("title",   r.getJob().getTitle());
            m.put("company", r.getJob().getCompanyName() != null ? r.getJob().getCompanyName() : "");
            m.put("score",   r.getScorePercent());
            m.put("label",   r.getMatchLabel());
            m.put("badge",   r.getMatchBadgeClass());
            return m;
        }).collect(Collectors.toList());
        model.addAttribute("topPaths", topPaths);

        // ── Strengths Analysis bars (0-100) ──────────────────────────────────
        // Skills Match: how many skills student has vs avg required
        String[] studentSkills = student.getSkillArray();
        int skillMatchScore = Math.min(100, studentSkills.length * 14);   // 7 skills = 98%

        // CGPA Score: normalized (6.0 = 60%, 10.0 = 100%)
        int cgpaScore = (int) Math.min(100, (student.getCgpa() / 10.0) * 100);

        // Application Activity: based on applications sent
        int activityScore = Math.min(100, myAppsList.size() * 20);

        // Profile Completeness
        int completeness = 0;
        if (student.getName()   != null && !student.getName().isBlank())          completeness += 20;
        if (student.getEmail()  != null && !student.getEmail().isBlank())          completeness += 15;
        if (student.getPhone()  != null && !student.getPhone().isBlank())          completeness += 10;
        if (student.getSkills() != null && !student.getSkills().isBlank())         completeness += 25;
        if (student.getPreferredRole() != null && !student.getPreferredRole().isBlank()) completeness += 15;
        if (student.getProfilePhoto()  != null && !student.getProfilePhoto().isBlank()) completeness += 15;

        model.addAttribute("skillMatchScore", skillMatchScore);
        model.addAttribute("cgpaScore",       cgpaScore);
        model.addAttribute("activityScore",   activityScore);
        model.addAttribute("completeness",    completeness);

        // ── Domain Breakdown (interest map) ─────────────────────────────────
        Map<String, Long> domainRaw = eligibleJobsList.stream()
            .filter(j -> j.getDomain() != null && !j.getDomain().isBlank())
            .collect(Collectors.groupingBy(Job::getDomain, Collectors.counting()));

        long domainTotal = domainRaw.values().stream().mapToLong(Long::longValue).sum();
        List<Map<String, Object>> domainBreakdown = domainRaw.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(e -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("domain", e.getKey());
                m.put("count",  e.getValue());
                m.put("pct",    domainTotal > 0 ? Math.round(e.getValue() * 100.0 / domainTotal) : 0);
                return m;
            }).collect(Collectors.toList());
        model.addAttribute("domainBreakdown", domainBreakdown);

        // ── Smart Next Steps checklist ────────────────────────────────────────
        List<Map<String, Object>> nextSteps = new ArrayList<>();
        nextSteps.add(step("Update your skills & profile",
            student.getSkills() != null && !student.getSkills().isBlank(), "/student/profile"));
        nextSteps.add(step("Browse eligible jobs",
            !eligibleJobsList.isEmpty(), "/student/jobs"));
        nextSteps.add(step("Apply to at least one job",
            !myAppsList.isEmpty(), "/student/jobs"));
        nextSteps.add(step("Check your AI recommendations",
            !recs.isEmpty(), "/student/recommendations"));
        nextSteps.add(step("Complete your profile (100%)",
            completeness >= 100, "/student/profile"));
        model.addAttribute("nextSteps", nextSteps);

        return "student/dashboard";
    }

    private Map<String, Object> step(String label, boolean done, String link) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("label", label);
        m.put("done",  done);
        m.put("link",  link);
        return m;
    }

    // ── Browse Jobs ──────────────────────────────────────────────────────────
    @GetMapping("/jobs")
    public String jobs(HttpSession session, Model model) {
        String redir = checkLogin(session); if (redir != null) return redir;
        Student student = getStudent(session);
        model.addAttribute("student", student);
        model.addAttribute("jobs", jobService.getEligibleJobs(student.getCgpa()));
        return "student/jobs";
    }

    // ── Apply for Job ────────────────────────────────────────────────────────
    @PostMapping("/apply/{jobId}")
    public String applyJob(@PathVariable int jobId, HttpSession session, RedirectAttributes ra) {
        String redir = checkLogin(session); if (redir != null) return redir;
        Student student = getStudent(session);
        String result = appService.apply(student.getStudentId(), jobId);
        if ("success".equals(result))            ra.addFlashAttribute("success", "Applied successfully!");
        else if ("already_applied".equals(result)) ra.addFlashAttribute("error", "You already applied for this job!");
        else                                       ra.addFlashAttribute("error", "Application failed. Try again.");
        return "redirect:/student/jobs";
    }

    // ── My Applications ──────────────────────────────────────────────────────
    @GetMapping("/applications")
    public String applications(HttpSession session, Model model) {
        String redir = checkLogin(session); if (redir != null) return redir;
        Student student = getStudent(session);
        List<Application> apps = appService.getByStudent(student.getStudentId());

        // Build apps with round progress
        List<Map<String, Object>> appsWithProgress = apps.stream().map(app -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("app",          app);
            m.put("roundResults", roundResultService.getProgressForApplication(app.getApplicationId()));
            m.put("currentRound", roundResultService.getCurrentRound(app.getApplicationId()).orElse(null));
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("student",          student);
        model.addAttribute("appsWithProgress", appsWithProgress);
        return "student/applications";
    }

    // ── Withdraw Application ─────────────────────────────────────────────────
    @PostMapping("/withdraw/{appId}")
    public String withdraw(@PathVariable int appId, HttpSession session, RedirectAttributes ra) {
        String redir = checkLogin(session); if (redir != null) return redir;
        Student student = getStudent(session);
        boolean ok = appService.withdraw(appId, student.getStudentId());
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Application withdrawn." : "Cannot withdraw this application.");
        return "redirect:/student/applications";
    }

    // ── AI Recommendations ───────────────────────────────────────────────────
    @GetMapping("/recommendations")
    public String recommendations(HttpSession session, Model model) {
        String redir = checkLogin(session); if (redir != null) return redir;
        Student student = getStudent(session);
        model.addAttribute("student", student);
        model.addAttribute("recommendations", aiService.getTopRecommendations(student, 5));
        return "student/recommendations";
    }

    // ── Profile View ─────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String redir = checkLogin(session); if (redir != null) return redir;
        model.addAttribute("student", getStudent(session));
        return "student/profile";
    }

    // ── Profile Update (text fields) ─────────────────────────────────────────
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String skills,
                                @RequestParam String preferredRole,
                                HttpSession session,
                                RedirectAttributes ra) {
        String redir = checkLogin(session); if (redir != null) return redir;
        Student student = getStudent(session);
        student.setEmail(email.trim());
        student.setPhone(phone.trim());
        student.setSkills(skills.trim());
        student.setPreferredRole(preferredRole.trim());
        Student saved = studentService.save(student);
        session.setAttribute("student", saved);
        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/student/profile";
    }

    // ── Upload Profile Photo ─────────────────────────────────────────────────
    @PostMapping("/upload-photo")
    public String uploadPhoto(@RequestParam("photo") MultipartFile photo,
                              HttpSession session,
                              RedirectAttributes ra) {
        String redir = checkLogin(session); if (redir != null) return redir;
        if (photo.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a photo to upload.");
            return "redirect:/student/profile";
        }
        String contentType = photo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            ra.addFlashAttribute("error", "Only image files (JPG, PNG) are allowed!");
            return "redirect:/student/profile";
        }
        try {
            Student student = getStudent(session);
            String ext      = getExtension(photo.getOriginalFilename());
            String filename = "photo_" + student.getStudentId() + "_"
                            + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
            saveFile(photo, UPLOAD_DIR + "photos/", filename);
            student.setProfilePhoto(filename);
            Student saved = studentService.save(student);
            session.setAttribute("student", saved);
            ra.addFlashAttribute("success", "Profile photo updated!");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/student/profile";
    }

    // ── Upload Resume ────────────────────────────────────────────────────────
    @PostMapping("/upload-resume")
    public String uploadResume(@RequestParam("resume") MultipartFile resume,
                               HttpSession session,
                               RedirectAttributes ra) {
        String redir = checkLogin(session); if (redir != null) return redir;
        if (resume.isEmpty()) {
            ra.addFlashAttribute("error", "Please select a PDF file to upload.");
            return "redirect:/student/profile";
        }
        String contentType = resume.getContentType();
        if (!"application/pdf".equals(contentType)) {
            ra.addFlashAttribute("error", "Only PDF files are allowed for resume!");
            return "redirect:/student/profile";
        }
        try {
            Student student = getStudent(session);
            String filename = "resume_" + student.getStudentId() + "_"
                            + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
            saveFile(resume, UPLOAD_DIR + "resumes/", filename);
            student.setResumePath(filename);
            Student saved = studentService.save(student);
            session.setAttribute("student", saved);
            ra.addFlashAttribute("success", "Resume uploaded successfully!");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/student/profile";
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void saveFile(MultipartFile file, String dir, String filename) throws IOException {
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdirs();
        Path path = Paths.get(dir + filename);
        Files.write(path, file.getBytes());
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
