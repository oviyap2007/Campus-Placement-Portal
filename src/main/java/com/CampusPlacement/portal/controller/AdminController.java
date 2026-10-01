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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private StudentService        studentService;
    @Autowired private CompanyService        companyService;
    @Autowired private JobService            jobService;
    @Autowired private ApplicationService    appService;
    @Autowired private CompanyAccountService accountService;
    @Autowired private RoundResultService    roundResultService;

    private String checkAdmin(HttpSession session) {
        if (session.getAttribute("admin") == null) return "redirect:/login";
        return null;
    }

    // ── Dashboard ────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;

        long total   = studentService.countTotal();
        long placed  = studentService.countPlaced();
        long companies = companyService.count();
        long active  = jobService.countActive();
        long totalApps = appService.countTotal();

        model.addAttribute("admin",          session.getAttribute("admin"));
        model.addAttribute("totalStudents",  total);
        model.addAttribute("placedStudents", placed);
        model.addAttribute("totalCompanies", companies);
        model.addAttribute("activeJobs",     active);
        model.addAttribute("totalApps",      totalApps);
        model.addAttribute("placementRate",
                total > 0 ? String.format("%.1f", placed * 100.0 / total) : "0.0");
        model.addAttribute("pendingCompanies", accountService.getPendingAccounts().size());

        // ── Upcoming Deadlines: next 5 active jobs sorted by deadline ──────
        LocalDate today = LocalDate.now();
        List<Job> allActiveJobs = jobService.getActiveJobs();
        List<Map<String, Object>> upcomingDeadlines = allActiveJobs.stream()
            .filter(j -> j.getDeadline() != null && !j.getDeadline().isBefore(today))
            .sorted(Comparator.comparing(Job::getDeadline))
            .limit(5)
            .map(j -> {
                long daysLeft = ChronoUnit.DAYS.between(today, j.getDeadline());
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("title",       j.getTitle());
                m.put("company",     j.getCompanyName() != null ? j.getCompanyName() : "Unknown");
                m.put("deadline",    j.getDeadline().toString());
                m.put("daysLeft",    daysLeft);
                m.put("urgent",      daysLeft <= 3);
                m.put("soon",        daysLeft > 3 && daysLeft <= 7);
                m.put("appCount",    appService.countByJob(j.getJobId()));
                return m;
            })
            .collect(Collectors.toList());
        model.addAttribute("upcomingDeadlines", upcomingDeadlines);

        // ── Top Recruiters: companies with most selected/placed students ───
        List<Application> selectedApps = appService.getByStatus(Status.SELECTED);
        Map<String, Long> recruiterMap = selectedApps.stream()
            .collect(Collectors.groupingBy(
                app -> app.getCompanyName() != null ? app.getCompanyName() : "Unknown",
                Collectors.counting()
            ));
        List<Map<String, Object>> topRecruiters = recruiterMap.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(e -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name",       e.getKey());
                m.put("placements", e.getValue());
                return m;
            })
            .collect(Collectors.toList());
        model.addAttribute("topRecruiters", topRecruiters);

        // ── Needs Attention: active jobs with zero applications ────────────
        List<Map<String, Object>> needsAttention = allActiveJobs.stream()
            .filter(j -> appService.countByJob(j.getJobId()) == 0)
            .limit(4)
            .map(j -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("title",   j.getTitle());
                m.put("company", j.getCompanyName() != null ? j.getCompanyName() : "Unknown");
                m.put("salary",  j.getFormattedSalary());
                return m;
            })
            .collect(Collectors.toList());
        model.addAttribute("needsAttention", needsAttention);

        return "admin/dashboard";
    }

    // ══════════════════ STUDENT MANAGEMENT ════════════════════════════════════

    @GetMapping("/students")
    public String students(@RequestParam(required = false) String filter,
                           HttpSession session, Model model) {
        String r = checkAdmin(session); if (r != null) return r;

        List<Student> allStudents = studentService.getAllStudents();

        // If filter=placed, show only placed students
        List<Student> displayStudents;
        String pageTitle;
        if ("placed".equals(filter)) {
            displayStudents = allStudents.stream()
                .filter(Student::isPlaced)
                .collect(Collectors.toList());
            pageTitle = "✅ Placed Students (" + displayStudents.size() + ")";
        } else {
            displayStudents = allStudents;
            pageTitle = "🎓 All Students (" + allStudents.size() + ")";
        }

        model.addAttribute("students",   displayStudents);
        model.addAttribute("pageTitle",  pageTitle);
        model.addAttribute("filter",     filter);
        model.addAttribute("admin",      session.getAttribute("admin"));
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
        List<com.CampusPlacement.portal.model.Company> companies = companyService.getAll();

        // Build set of company IDs that already have an account
        java.util.Set<Integer> withAccount = accountService.getApprovedAccounts().stream()
            .map(a -> a.getCompanyId())
            .collect(java.util.stream.Collectors.toSet());
        // Also include pending ones
        accountService.getPendingAccounts().stream()
            .map(a -> a.getCompanyId())
            .forEach(withAccount::add);

        model.addAttribute("companies",             companies);
        model.addAttribute("companyIdsWithAccount", withAccount);
        model.addAttribute("admin",                 session.getAttribute("admin"));
        model.addAttribute("pendingAccounts",       accountService.getPendingAccounts());
        model.addAttribute("approvedAccounts",      accountService.getApprovedAccounts());
        return "admin/companies";
    }

    @PostMapping("/companies/approve/{accountId}")
    public String approveCompany(@PathVariable int accountId, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        accountService.approve(accountId);
        ra.addFlashAttribute("success", "Company account approved! They can now login.");
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/reject/{accountId}")
    public String rejectCompany(@PathVariable int accountId, HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;
        accountService.reject(accountId);
        ra.addFlashAttribute("success", "Company account rejected and removed.");
        return "redirect:/admin/companies";
    }

    @PostMapping("/companies/add")
    public String addCompany(
            @RequestParam String  name,
            @RequestParam String  industry,
            @RequestParam String  location,
            @RequestParam String  contactEmail,
            @RequestParam String  contactPhone,
            @RequestParam(required = false) String  username,
            @RequestParam(required = false) String  password,
            @RequestParam(required = false) Boolean createAccount,
            HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;

        Company c = new Company(name, industry, location, contactEmail, contactPhone);
        Company saved = companyService.save(c);

        if (Boolean.TRUE.equals(createAccount)
                && username != null && !username.isBlank()
                && password != null && !password.isBlank()) {

            if (accountService.usernameExists(username)) {
                ra.addFlashAttribute("error",
                    "Company added, but username '" + username + "' is taken. Create account manually.");
            } else {
                accountService.register(saved.getCompanyId(), username, password);
                // Account is now PENDING — admin must approve from the Pending section
                ra.addFlashAttribute("success",
                    "✅ Company '" + name + "' added and account created for '" + username + "'. " +
                    "Approve the account from the Pending Approvals section above.");
            }
        } else {
            ra.addFlashAttribute("success", "Company '" + name + "' added successfully!");
        }
        return "redirect:/admin/companies";
    }

    /** Admin creates a PENDING account for an existing company — must then approve it from the pending list */
    @PostMapping("/companies/{companyId}/create-account")
    public String createAccountForCompany(
            @PathVariable int companyId,
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session, RedirectAttributes ra) {
        String r = checkAdmin(session); if (r != null) return r;

        if (accountService.usernameExists(username)) {
            ra.addFlashAttribute("error", "Username '" + username + "' is already taken.");
            return "redirect:/admin/companies";
        }
        if (accountService.getByCompanyId(companyId).isPresent()) {
            ra.addFlashAttribute("error", "This company already has a portal account.");
            return "redirect:/admin/companies";
        }
        accountService.register(companyId, username, password);
        // PENDING — no auto-approve
        ra.addFlashAttribute("success",
            "✅ Account created for username '" + username + "'. " +
            "Now approve it from the ⏳ Pending Approvals section.");
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
