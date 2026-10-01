package com.CampusPlacement.portal.controller;

import com.CampusPlacement.portal.model.PlacementOfficer;
import com.CampusPlacement.portal.model.Student;
import com.CampusPlacement.portal.service.AuthService;
import com.CampusPlacement.portal.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private static final String COLLEGE_DOMAIN = "@citchennai.net";

    @Autowired private AuthService     authService;
    @Autowired private StudentService  studentService;

    // ── GET /login ──────────────────────────────────────────────────────────
    @GetMapping({"/" , "/login"})
    public String loginPage(HttpSession session) {
        if (session.getAttribute("student") != null) return "redirect:/student/dashboard";
        if (session.getAttribute("admin")   != null) return "redirect:/admin/dashboard";
        return "login";
    }

    // ── POST /login ─────────────────────────────────────────────────────────
    @PostMapping("/login")
    public String processLogin(@RequestParam String role,
                               @RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        if ("student".equals(role)) {
            Student student = authService.loginStudent(username, password);
            if (student != null) {
                session.setAttribute("student", student);
                return "redirect:/student/dashboard";
            }
        } else if ("admin".equals(role)) {
            PlacementOfficer officer = authService.loginAdmin(username, password);
            if (officer != null) {
                session.setAttribute("admin", officer);
                return "redirect:/admin/dashboard";
            }
        }
        model.addAttribute("error", "Invalid username or password!");
        return "login";
    }

    // ── GET /signup ──────────────────────────────────────────────────────────
    @GetMapping("/signup")
    public String signupPage(HttpSession session) {
        if (session.getAttribute("student") != null) return "redirect:/student/dashboard";
        return "signup";
    }

    // ── POST /signup ─────────────────────────────────────────────────────────
    @PostMapping("/signup")
    public String processSignup(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String department,
                                @RequestParam double cgpa,
                                @RequestParam int passYear,
                                @RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                Model model,
                                RedirectAttributes ra) {

        // 1. Validate college email domain
        if (!email.toLowerCase().endsWith(COLLEGE_DOMAIN)) {
            model.addAttribute("error",
                "Only college email addresses ending with " + COLLEGE_DOMAIN + " are allowed!");
            model.addAttribute("formData_name",       name);
            model.addAttribute("formData_username",   username);
            model.addAttribute("formData_phone",      phone);
            model.addAttribute("formData_dept",       department);
            model.addAttribute("formData_cgpa",       cgpa);
            model.addAttribute("formData_passYear",   passYear);
            return "signup";
        }

        // 2. Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            model.addAttribute("formData_name",     name);
            model.addAttribute("formData_email",    email);
            model.addAttribute("formData_username", username);
            model.addAttribute("formData_phone",    phone);
            model.addAttribute("formData_dept",     department);
            model.addAttribute("formData_cgpa",     cgpa);
            model.addAttribute("formData_passYear", passYear);
            return "signup";
        }

        // 3. Check username already exists
        if (studentService.usernameExists(username)) {
            model.addAttribute("error", "Username '" + username + "' is already taken! Please choose another.");
            model.addAttribute("formData_name",     name);
            model.addAttribute("formData_email",    email);
            model.addAttribute("formData_phone",    phone);
            model.addAttribute("formData_dept",     department);
            model.addAttribute("formData_cgpa",     cgpa);
            model.addAttribute("formData_passYear", passYear);
            return "signup";
        }

        // 4. Create and save student
        Student student = new Student(name, email, phone, department,
                                      cgpa, passYear, username, password, "", "");
        studentService.save(student);

        ra.addFlashAttribute("success",
            "Account created successfully! Login with username: " + username);
        return "redirect:/login";
    }

    // ── GET /logout ─────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
