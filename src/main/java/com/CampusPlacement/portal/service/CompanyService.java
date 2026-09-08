package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.Company;
import com.CampusPlacement.portal.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired private CompanyRepository companyRepo;

    public List<Company> getAll() { return companyRepo.findAll(); }

    public Optional<Company> getById(int id) { return companyRepo.findById(id); }

    public Company save(Company company) { return companyRepo.save(company); }

    public void delete(int id) { companyRepo.deleteById(id); }

    public long count() { return companyRepo.count(); }
}
