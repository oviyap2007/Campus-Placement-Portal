package com.CampusPlacement.portal.controller;

import com.CampusPlacement.portal.model.*;
import com.CampusPlacement.portal.model.Application.Status;
import com.CampusPlacement.portal.model.InterviewRound.RoundType;
import com.CampusPlacement.portal.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/company")
public class CompanyController {

    @Autowired private CompanyAccountService  accountService;
    @Autowired private CompanyService         companyService;
    @Autowired private JobService             jobService;
    @Autowired private ApplicationService     appService;
    @Autowired private StudentService         studentService;
    @Autowired private InterviewRoundService  roundService;
    @Autowired private RoundResultService     resultService;

    // ── Auth helpers ──────────────────────────────────────────────────────────
    private CompanyAccount getCompany(HttpSession session) {
        return (CompanyAccount) session.getAttribute("company");
    }

    private String checkLogin(HttpSession session) {
        if (getCompany(session) == null) return "redirect:/company/login";
        return null;
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (getCompany(session) != null) return "redirect:/company/dashboard";
        return "company/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session, Model model) {
        CompanyAccount acc = accountService.login(username, password);
        if (acc != null) {
            session.setAttribute("company", acc);
            return "redirect:/company/dashboard";
        }
        // Check if account exists but not approved
        model.addAttribute("error",
            "Invalid credentials or your account is pending admin approval.");
        return "company/login";
    }

    // ── Register ──────────────────────────────────────────────────────────────
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("companies", companyService.getAll());
        return "company/register";
    }

    @PostMapping("/register")
    public String processRegister(
            // For existing company
            @RequestParam(required = false) Integer companyId,
            // For new company
            @RequestParam(required = false) String  companyName,
            @RequestParam(required = false) String  industry,
            @RequestParam(required = false) String  location,
            @RequestParam(required = false) String  contactEmail,
            @RequestParam(required = false) String  contactPhone,
            // Common
            @RequestParam String  registerType,   // "existing" or "new"
            @RequestParam String  username,
            @RequestParam String  password,
            RedirectAttributes ra) {

        // ── 1. Validate username availability ─────────────────────────
        if (accountService.usernameExists(username)) {
            ra.addFlashAttribute("error", "Username '" + username + "' is already taken. Choose another.");
            return "redirect:/company/register";
        }

        // ── 2. Resolve company ID ──────────────────────────────────────
        int resolvedCompanyId;

        if ("new".equals(registerType)) {
            // Validate required fields
            if (companyName == null || companyName.trim().isEmpty()) {
                ra.addFlashAttribute("error", "Company name is required.");
                return "redirect:/company/register";
            }
            // Create new company record
            Company newCompany = new Company(
                companyName.trim(),
                industry  != null ? industry.trim()      : "",
                location  != null ? location.trim()      : "",
                contactEmail != null ? contactEmail.trim(): "",
                contactPhone != null ? contactPhone.trim(): ""
            );
            Company saved = companyService.save(newCompany);
            resolvedCompanyId = saved.getCompanyId();

        } else {
            // Existing company
            if (companyId == null) {
                ra.addFlashAttribute("error", "Please select a company from the list.");
                return "redirect:/company/register";
            }
            if (accountService.getByCompanyId(companyId).isPresent()) {
                ra.addFlashAttribute("error", "This company already has a portal account. Contact the admin.");
                return "redirect:/company/register";
            }
            resolvedCompanyId = companyId;
        }

        // ── 3. Create account (pending approval) ──────────────────────
        String result = accountService.register(resolvedCompanyId, username, password);
        if ("username_taken".equals(result)) {
            ra.addFlashAttribute("error", "Username '" + username + "' is already taken.");
        } else {
            ra.addFlashAttribute("success",
                "Registration submitted! 🎉 Your account is pending admin approval. " +
                "You will be able to login once approved.");
        }
        return "redirect:/company/login";
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("company");
        return "redirect:/company/login";
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String r = checkLogin(session); if (r != null) return r;
        CompanyAccount company = getCompany(session);
        model.addAttribute("company", company);

        List<Job> myJobs = jobService.getAll().stream()
            .filter(j -> j.getCompanyId() == company.getCompanyId())
            .collect(Collectors.toList());

        long totalApplicants = myJobs.stream()
            .mapToLong(j -> appService.countByJob(j.getJobId()))
            .sum();

        long placed = myJobs.stream()
            .mapToLong(j -> appService.getByJobAndStatus(j.getJobId(), Status.SELECTED).size())
            .sum();

        long activeJobs = myJobs.stream().filter(Job::isActive).count();

        model.addAttribute("myJobs",          myJobs);
        model.addAttribute("totalApplicants", totalApplicants);
        model.addAttribute("totalPlaced",     placed);
        model.addAttribute("activeJobs",      activeJobs);
        model.addAttribute("jobCount",        myJobs.size());

        return "company/dashboard";
    }

    // ── Jobs ──────────────────────────────────────────────────────────────────
    @GetMapping("/jobs")
    public String jobs(HttpSession session, Model model) {
        String r = checkLogin(session); if (r != null) return r;
        CompanyAccount company = getCompany(session);
        model.addAttribute("company", company);

        List<Job> myJobs = jobService.getAll().stream()
            .filter(j -> j.getCompanyId() == company.getCompanyId())
            .collect(Collectors.toList());

        // Attach round count and applicant count per job
        List<Map<String, Object>> jobsWithMeta = myJobs.stream().map(j -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("job",        j);
            m.put("roundCount", roundService.getRoundsForJob(j.getJobId()).size());
            m.put("appCount",   appService.countByJob(j.getJobId()));
            m.put("hasRounds",  roundService.hasRounds(j.getJobId()));
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("jobsWithMeta", jobsWithMeta);
        model.addAttribute("roundTypes",   RoundType.values());
        return "company/jobs";
    }

    @PostMapping("/jobs/add")
    public String addJob(@RequestParam String title,
                         @RequestParam String description,
                         @RequestParam double salary,
                         @RequestParam double minCgpa,
                         @RequestParam String deadline,
                         @RequestParam(defaultValue = "") String requiredSkills,
                         @RequestParam(defaultValue = "") String domain,
                         @RequestParam(defaultValue = "") String jobRole,
                         @RequestParam(value = "roundNames",  defaultValue = "") List<String> roundNames,
                         @RequestParam(value = "roundTypes",  defaultValue = "") List<String> roundTypeValues,
                         HttpSession session, RedirectAttributes ra) {
        String r = checkLogin(session); if (r != null) return r;
        CompanyAccount company = getCompany(session);
        try {
            Job job = new Job(company.getCompanyId(), title, description, salary, minCgpa,
                              LocalDate.parse(deadline), requiredSkills, domain, jobRole);
            Job saved = jobService.save(job);

            // Save rounds
            for (int i = 0; i < roundNames.size(); i++) {
                String rName = roundNames.get(i).trim();
                if (rName.isEmpty()) continue;
                RoundType rt = RoundType.valueOf(roundTypeValues.get(i));
                InterviewRound round = new InterviewRound(saved.getJobId(), i + 1, rName, rt);
                roundService.save(round);
            }

            ra.addFlashAttribute("success", "Job posted with " + roundNames.size() + " interview rounds!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error posting job: " + e.getMessage());
        }
        return "redirect:/company/jobs";
    }

    @PostMapping("/jobs/toggle/{id}")
    public String toggleJob(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        String r = checkLogin(session); if (r != null) return r;
        jobService.toggleStatus(id);
        ra.addFlashAttribute("success", "Job status updated.");
        return "redirect:/company/jobs";
    }

    // ── Add / Replace Rounds for existing job ────────────────────────────────
    @PostMapping("/jobs/{jobId}/rounds")
    public String saveRounds(@PathVariable int jobId,
                             @RequestParam List<String> roundNames,
                             @RequestParam List<String> roundTypeValues,
                             HttpSession session, RedirectAttributes ra) {
        String r = checkLogin(session); if (r != null) return r;
        List<InterviewRound> rounds = new ArrayList<>();
        for (int i = 0; i < roundNames.size(); i++) {
            String rName = roundNames.get(i).trim();
            if (rName.isEmpty()) continue;
            rounds.add(new InterviewRound(jobId, i + 1, rName, RoundType.valueOf(roundTypeValues.get(i))));
        }
        roundService.replaceRounds(jobId, rounds);
        ra.addFlashAttribute("success", "Interview rounds updated!");
        return "redirect:/company/applicants/" + jobId;
    }

    // ── Applicants ────────────────────────────────────────────────────────────
    @GetMapping("/applicants")
    public String applicantsAll(HttpSession session, Model model) {
        String r = checkLogin(session); if (r != null) return r;
        CompanyAccount company = getCompany(session);
        model.addAttribute("company", company);

        List<Job> myJobs = jobService.getAll().stream()
            .filter(j -> j.getCompanyId() == company.getCompanyId())
            .collect(Collectors.toList());
        model.addAttribute("myJobs", myJobs);

        // Default to first job
        if (!myJobs.isEmpty()) {
            return "redirect:/company/applicants/" + myJobs.get(0).getJobId();
        }
        model.addAttribute("apps", Collections.emptyList());
        model.addAttribute("rounds", Collections.emptyList());
        return "company/applicants";
    }

    @GetMapping("/applicants/{jobId}")
    public String applicantsByJob(@PathVariable int jobId, HttpSession session, Model model) {
        String r = checkLogin(session); if (r != null) return r;
        CompanyAccount company = getCompany(session);

        // Verify this job belongs to this company
        List<Job> myJobs = jobService.getAll().stream()
            .filter(j -> j.getCompanyId() == company.getCompanyId())
            .collect(Collectors.toList());

        boolean owns = myJobs.stream().anyMatch(j -> j.getJobId() == jobId);
        if (!owns) return "redirect:/company/applicants";

        model.addAttribute("company",    company);
        model.addAttribute("myJobs",     myJobs);
        model.addAttribute("selectedJobId", jobId);

        // Get selected job
        jobService.getById(jobId).ifPresent(j -> model.addAttribute("selectedJob", j));

        // Get rounds for this job
        List<InterviewRound> rounds = roundService.getRoundsForJob(jobId);
        model.addAttribute("rounds",     rounds);
        model.addAttribute("roundTypes", RoundType.values());
        model.addAttribute("hasRounds",  !rounds.isEmpty());

        // Get applications with student info and round progress
        List<Application> apps = appService.getByJob(jobId);
        List<Map<String, Object>> appsWithProgress = apps.stream().map(app -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("app",         app);
            m.put("progress",    resultService.getProgressForApplication(app.getApplicationId()));
            m.put("currentRound",resultService.getCurrentRound(app.getApplicationId()).orElse(null));

            // Student info
            studentService.getById(app.getStudentId()).ifPresent(s -> {
                m.put("student", s);
            });
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("appsWithProgress", appsWithProgress);
        return "company/applicants";
    }

    // ── Shortlist (initializes rounds) ────────────────────────────────────────
    @PostMapping("/shortlist/{applicationId}")
    public String shortlist(@PathVariable int applicationId,
                            HttpSession session, RedirectAttributes ra) {
        String r = checkLogin(session); if (r != null) return r;

        Optional<Application> appOpt = appService.findById(applicationId);
        if (appOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Application not found.");
            return "redirect:/company/applicants";
        }
        Application app = appOpt.get();

        if (!roundService.hasRounds(app.getJobId())) {
            ra.addFlashAttribute("error",
                "Please define interview rounds for this job before shortlisting.");
            return "redirect:/company/applicants/" + app.getJobId();
        }

        appService.updateStatus(applicationId, Status.SHORTLISTED);
        resultService.initializeRounds(applicationId, app.getJobId());
        ra.addFlashAttribute("success", "Student shortlisted! Interview rounds initialized.");
        return "redirect:/company/applicants/" + app.getJobId();
    }

    // ── Pass Round ────────────────────────────────────────────────────────────
    @PostMapping("/round/pass/{applicationId}/{roundId}")
    public String passRound(@PathVariable int applicationId,
                            @PathVariable int roundId,
                            @RequestParam(defaultValue = "") String remarks,
                            HttpSession session, RedirectAttributes ra) {
        String r = checkLogin(session); if (r != null) return r;

        String result = resultService.passRound(applicationId, roundId, remarks);
        int jobId = appService.findById(applicationId).map(Application::getJobId).orElse(0);

        if ("selected".equals(result)) {
            ra.addFlashAttribute("success", "🎉 All rounds cleared! Student has been SELECTED and placed!");
        } else if ("advanced".equals(result)) {
            ra.addFlashAttribute("success", "✅ Round marked as PASSED. Student advanced to next round.");
        } else {
            ra.addFlashAttribute("error", "Could not update round.");
        }
        return "redirect:/company/applicants/" + jobId;
    }

    // ── Fail Round ────────────────────────────────────────────────────────────
    @PostMapping("/round/fail/{applicationId}/{roundId}")
    public String failRound(@PathVariable int applicationId,
                            @PathVariable int roundId,
                            @RequestParam(defaultValue = "") String remarks,
                            HttpSession session, RedirectAttributes ra) {
        String r = checkLogin(session); if (r != null) return r;

        resultService.failRound(applicationId, roundId, remarks);
        int jobId = appService.findById(applicationId).map(Application::getJobId).orElse(0);
        ra.addFlashAttribute("error", "❌ Round marked as FAILED. Student has been rejected.");
        return "redirect:/company/applicants/" + jobId;
    }

    // ── Company Profile ───────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String r = checkLogin(session); if (r != null) return r;
        CompanyAccount company = getCompany(session);
        model.addAttribute("company", company);
        companyService.getById(company.getCompanyId()).ifPresent(c -> model.addAttribute("companyInfo", c));
        return "company/profile";
    }
}
