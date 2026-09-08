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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired private StudentService          studentService;
    @Autowired private JobService              jobService;
    @Autowired private ApplicationService      appService;
    @Autowired private AiRecommendationService aiService;

    // ── Helper: get logged-in student from session ───────────────────────────
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
        String redir = checkLogin(session);
        if (redir != null) return redir;
        Student student = getStudent(session);
        model.addAttribute("student", student);
        model.addAttribute("eligibleJobs", jobService.getEligibleJobs(student.getCgpa()).size());
        model.addAttribute("myApps", appService.getByStudent(student.getStudentId()).size());
        return "student/dashboard";
    }

    // ── Browse Jobs ──────────────────────────────────────────────────────────
    @GetMapping("/jobs")
    public String jobs(HttpSession session, Model model) {
        String redir = checkLogin(session);
        if (redir != null) return redir;
        Student student = getStudent(session);
        List<Job> jobs = jobService.getEligibleJobs(student.getCgpa());
        model.addAttribute("student", student);
        model.addAttribute("jobs", jobs);
        return "student/jobs";
    }

    // ── Apply for Job ────────────────────────────────────────────────────────
    @PostMapping("/apply/{jobId}")
    public String applyJob(@PathVariable int jobId,
                           HttpSession session,
                           RedirectAttributes ra) {
        String redir = checkLogin(session);
        if (redir != null) return redir;
        Student student = getStudent(session);
        String result = appService.apply(student.getStudentId(), jobId);
        if ("success".equals(result))
            ra.addFlashAttribute("success", "Applied successfully!");
        else if ("already_applied".equals(result))
            ra.addFlashAttribute("error", "You have already applied for this job!");
        else
            ra.addFlashAttribute("error", "Application failed. Please try again.");
        return "redirect:/student/jobs";
    }

    // ── My Applications ──────────────────────────────────────────────────────
    @GetMapping("/applications")
    public String applications(HttpSession session, Model model) {
        String redir = checkLogin(session);
        if (redir != null) return redir;
        Student student = getStudent(session);
        List<Application> apps = appService.getByStudent(student.getStudentId());
        model.addAttribute("student", student);
        model.addAttribute("apps", apps);
        return "student/applications";
    }

    // ── Withdraw Application ─────────────────────────────────────────────────
    @PostMapping("/withdraw/{appId}")
    public String withdraw(@PathVariable int appId,
                           HttpSession session,
                           RedirectAttributes ra) {
        String redir = checkLogin(session);
        if (redir != null) return redir;
        Student student = getStudent(session);
        boolean ok = appService.withdraw(appId, student.getStudentId());
        if (ok) ra.addFlashAttribute("success", "Application withdrawn.");
        else    ra.addFlashAttribute("error", "Cannot withdraw this application.");
        return "redirect:/student/applications";
    }

    // ── AI Recommendations ───────────────────────────────────────────────────
    @GetMapping("/recommendations")
    public String recommendations(HttpSession session, Model model) {
        String redir = checkLogin(session);
        if (redir != null) return redir;
        Student student = getStudent(session);
        List<RecommendationResult> recs = aiService.getTopRecommendations(student, 5);
        model.addAttribute("student", student);
        model.addAttribute("recommendations", recs);
        return "student/recommendations";
    }

    // ── Profile View ─────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String redir = checkLogin(session);
        if (redir != null) return redir;
        model.addAttribute("student", getStudent(session));
        return "student/profile";
    }

    // ── Profile Update ───────────────────────────────────────────────────────
    @PostMapping("/profile")
    public String updateProfile(@RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String skills,
                                @RequestParam String preferredRole,
                                HttpSession session,
                                RedirectAttributes ra) {
        String redir = checkLogin(session);
        if (redir != null) return redir;
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
}
