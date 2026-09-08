package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.Application;
import com.CampusPlacement.portal.model.Application.Status;
import com.CampusPlacement.portal.model.Company;
import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.model.Student;
import com.CampusPlacement.portal.repository.ApplicationRepository;
import com.CampusPlacement.portal.repository.CompanyRepository;
import com.CampusPlacement.portal.repository.JobRepository;
import com.CampusPlacement.portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    @Autowired private ApplicationRepository appRepo;
    @Autowired private StudentRepository     studentRepo;
    @Autowired private JobRepository         jobRepo;
    @Autowired private CompanyRepository     companyRepo;

    /** Enrich applications with student name, job title, company name */
    private void enrich(List<Application> apps) {
        for (Application app : apps) {
            studentRepo.findById(app.getStudentId())
                    .ifPresent(s -> app.setStudentName(s.getName()));
            jobRepo.findById(app.getJobId()).ifPresent(j -> {
                app.setJobTitle(j.getTitle());
                companyRepo.findById(j.getCompanyId())
                        .ifPresent(c -> app.setCompanyName(c.getName()));
            });
        }
    }

    public List<Application> getAll() {
        List<Application> apps = appRepo.findAll();
        enrich(apps);
        return apps;
    }

    public List<Application> getByStudent(int studentId) {
        List<Application> apps = appRepo.findByStudentId(studentId);
        enrich(apps);
        return apps;
    }

    public String apply(int studentId, int jobId) {
        if (appRepo.existsByStudentIdAndJobId(studentId, jobId))
            return "already_applied";
        Application app = new Application(studentId, jobId);
        app.setApplyDate(LocalDate.now());
        appRepo.save(app);
        return "success";
    }

    public boolean withdraw(int appId, int studentId) {
        Optional<Application> opt = appRepo.findById(appId);
        if (opt.isEmpty()) return false;
        Application app = opt.get();
        if (app.getStudentId() != studentId) return false;
        if (app.getStatus() != Status.APPLIED) return false;
        appRepo.deleteById(appId);
        return true;
    }

    public boolean updateStatus(int appId, Status newStatus) {
        Optional<Application> opt = appRepo.findById(appId);
        if (opt.isEmpty()) return false;
        Application app = opt.get();
        app.setStatus(newStatus);
        appRepo.save(app);
        if (newStatus == Status.SELECTED) {
            studentRepo.findById(app.getStudentId())
                    .ifPresent(s -> { s.setPlaced(true); studentRepo.save(s); });
        }
        return true;
    }

    public long countTotal() { return appRepo.count(); }
}
