package com.CampusPlacement.portal.controller;

import com.CampusPlacement.portal.model.PlacementOfficer;
import com.CampusPlacement.portal.model.Student;
import com.CampusPlacement.portal.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired private AuthService authService;

    // ── GET /login ──────────────────────────────────────────────────────────
    @GetMapping({"/", "/login"})
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

    // ── GET /logout ─────────────────────────────────────────────────────────
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
