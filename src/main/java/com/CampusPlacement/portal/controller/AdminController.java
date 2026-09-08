package com.CampusPlacement.portal.controller;

import com.CampusPlacement.portal.model.*;
import com.CampusPlacement.portal.model.Application.Status;
import com.CampusPlacement.portal.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private StudentService     studentService;
    @Autowired private CompanyService     companyService;
    @Autowired private JobService         jobService;
    @Autowired private ApplicationService appService;

    private String checkAdmin(HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/login";
        return null;
    }

    // ── Dashboard ────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;
        model.addAttribute("admin",         session.getAttribute("admin"));
        model.addAttribute("totalStudents", studentService.countTotal());
        model.addAttribute("placedStudents",studentService.countPlaced());
        model.addAttribute("totalCompanies",companyService.count());
        model.addAttribute("activeJobs",    jobService.countActive());
        model.addAttribute("totalApps",     appService.countTotal());
        return "admin/dashboard";
    }

    // ══════════════════ STUDENT MANAGEMENT ════════════════════════════════════

    @GetMapping("/students")
    public String students(HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("admin",    session.getAttribute("admin"));
        return "admin/students";
    }

    @PostMapping("/students/add")
    public String addStudent(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String department,
                             @RequestParam double cgpa,
                             @RequestParam int passYear,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam(defaultValue = "") String skills,
                             @RequestParam(defaultValue = "") String preferredRole,
                             HttpSession session,
                             RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        if (studentService.usernameExists(username)) {
            ra.addFlashAttribute("error", "Username already taken!");
            return "redirect:/admin/students";
        }
        Student s = new Student(name, email, phone, department, cgpa,
                                passYear, username, password, skills, preferredRole);
        studentService.save(s);
        ra.addFlashAttribute("success", "Student registered successfully!");
        return "redirect:/admin/students";
    }

    @PostMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        studentService.delete(id);
        ra.addFlashAttribute("success", "Student deleted.");
        return "redirect:/admin/students";
    }

    // ══════════════════ COMPANY MANAGEMENT ════════════════════════════════════

    @GetMapping("/companies")
    public String companies(HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;
        model.addAttribute("companies", companyService.getAll());
        model.addAttribute("admin",     session.getAttribute("admin"));
        return "admin/companies";
    }

    @PostMapping("/companies/add")
    public String addCompany(@RequestParam String name,
                             @RequestParam String industry,
                             @RequestParam String location,
                             @RequestParam String contactEmail,
                             @RequestParam String contactPhone,
                             HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        Company c = new Company(name, industry, location, contactEmail, contactPhone);
        companyService.save(c);
        ra.addFlashAttribute("success", "Company added!");
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/delete/{id}")
    public String deleteCompany(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        companyService.delete(id);
        ra.addFlashAttribute("success", "Company deleted.");
        return "redirect:/admin/companies";
    }

    // ══════════════════ JOB MANAGEMENT ════════════════════════════════════════

    @GetMapping("/jobs")
    public String jobs(HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;
        model.addAttribute("jobs",      jobService.getAll());
        model.addAttribute("companies", companyService.getAll());
        model.addAttribute("admin",     session.getAttribute("admin"));
        return "admin/jobs";
    }

    @PostMapping("/jobs/add")
    public String addJob(@RequestParam int companyId,
                         @RequestParam String title,
                         @RequestParam String description,
                         @RequestParam double salary,
                         @RequestParam double minCgpa,
                         @RequestParam String deadline,
                         @RequestParam(defaultValue = "") String requiredSkills,
                         @RequestParam(defaultValue = "") String domain,
                         @RequestParam(defaultValue = "") String jobRole,
                         HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        try {
            Job job = new Job(companyId, title, description, salary, minCgpa,
                              LocalDate.parse(deadline), requiredSkills, domain, jobRole);
            jobService.save(job);
            ra.addFlashAttribute("success", "Job added!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid date format. Use YYYY-MM-DD.");
        }
        return "redirect:/admin/jobs";
    }

    @PostMapping("/jobs/toggle/{id}")
    public String toggleJob(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        jobService.toggleStatus(id);
        ra.addFlashAttribute("success", "Job status updated.");
        return "redirect:/admin/jobs";
    }

    @PostMapping("/jobs/delete/{id}")
    public String deleteJob(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        jobService.delete(id);
        ra.addFlashAttribute("success", "Job deleted.");
        return "redirect:/admin/jobs";
    }

    // ══════════════════ APPLICATION MANAGEMENT ═════════════════════════════════

    @GetMapping("/applications")
    public String applications(HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;
        List<Application> apps = appService.getAll();
        model.addAttribute("apps",  apps);
        model.addAttribute("admin", session.getAttribute("admin"));
        return "admin/applications";
    }

    @PostMapping("/applications/shortlist/{id}")
    public String shortlist(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        appService.updateStatus(id, Status.SHORTLISTED);
        ra.addFlashAttribute("success", "Student shortlisted!");
        return "redirect:/admin/applications";
    }

    @PostMapping("/applications/select/{id}")
    public String select(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        appService.updateStatus(id, Status.SELECTED);
        ra.addFlashAttribute("success", "Student selected and marked as placed!");
        return "redirect:/admin/applications";
    }

    @PostMapping("/applications/reject/{id}")
    public String reject(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        appService.updateStatus(id, Status.REJECTED);
        ra.addFlashAttribute("success", "Student rejected.");
        return "redirect:/admin/applications";
    }

    // ══════════════════ REPORTS ════════════════════════════════════════════════

    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;
        long total   = studentService.countTotal();
        long placed  = studentService.countPlaced();
        model.addAttribute("admin",          session.getAttribute("admin"));
        model.addAttribute("totalStudents",  total);
        model.addAttribute("placedStudents", placed);
        model.addAttribute("unplacedStudents", total - placed);
        model.addAttribute("placementRate",
                total > 0 ? String.format("%.1f", placed * 100.0 / total) : "0.0");
        model.addAttribute("totalCompanies", companyService.count());
        model.addAttribute("activeJobs",     jobService.countActive());
        model.addAttribute("totalApps",      appService.countTotal());
        model.addAttribute("placedList",
                studentService.getAllStudents().stream().filter(Student::isPlaced).toList());
        return "admin/reports";
    }
}
