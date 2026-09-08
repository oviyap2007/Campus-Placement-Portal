package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.Job;
import com.CampusPlacement.portal.repository.CompanyRepository;
import com.CampusPlacement.portal.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired private JobRepository     jobRepo;
    @Autowired private CompanyRepository companyRepo;

    /** Populate companyName on all jobs */
    private void enrich(List<Job> jobs) {
        for (Job j : jobs) {
            companyRepo.findById(j.getCompanyId())
                    .ifPresent(c -> j.setCompanyName(c.getName()));
        }
    }

    public List<Job> getAll() {
        List<Job> jobs = jobRepo.findAll();
        enrich(jobs);
        return jobs;
    }

    public List<Job> getActiveJobs() {
        List<Job> jobs = jobRepo.findByIsActiveTrue();
        enrich(jobs);
        return jobs;
    }

    public List<Job> getEligibleJobs(double cgpa) {
        List<Job> jobs = jobRepo.findByIsActiveTrueAndMinCgpaLessThanEqual(cgpa);
        enrich(jobs);
        return jobs;
    }

    public Optional<Job> getById(int id) {
        Optional<Job> opt = jobRepo.findById(id);
        opt.ifPresent(j -> companyRepo.findById(j.getCompanyId())
                .ifPresent(c -> j.setCompanyName(c.getName())));
        return opt;
    }

    public Job save(Job job) { return jobRepo.save(job); }

    public void delete(int id) { jobRepo.deleteById(id); }

    public boolean toggleStatus(int id) {
        Optional<Job> opt = jobRepo.findById(id);
        if (opt.isEmpty()) return false;
        Job job = opt.get();
        job.setActive(!job.isActive());
        jobRepo.save(job);
        return true;
    }

    public long countActive() { return jobRepo.findByIsActiveTrue().size(); }
}
