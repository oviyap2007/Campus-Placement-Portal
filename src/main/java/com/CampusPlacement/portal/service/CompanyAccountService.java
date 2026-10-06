package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.Company;
import com.CampusPlacement.portal.model.CompanyAccount;
import com.CampusPlacement.portal.repository.CompanyAccountRepository;
import com.CampusPlacement.portal.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyAccountService {

    @Autowired private CompanyAccountRepository accountRepo;
    @Autowired private CompanyRepository        companyRepo;

    /** Enrich account with company info */
    private void enrich(CompanyAccount acc) {
        companyRepo.findById(acc.getCompanyId()).ifPresent(c -> {
            acc.setCompanyName(c.getName());
            acc.setIndustry(c.getIndustry());
            acc.setLocation(c.getLocation());
            acc.setContactEmail(c.getContactEmail());
        });
    }

    private void enrichAll(List<CompanyAccount> list) {
        list.forEach(this::enrich);
    }

    /** Register a new company account (pending approval) */
    public String register(int companyId, String username, String password) {
        if (accountRepo.existsByUsername(username))
            return "username_taken";
        if (accountRepo.findByCompanyId(companyId).isPresent())
            return "company_has_account";
        CompanyAccount acc = new CompanyAccount(companyId, username, password);
        accountRepo.save(acc);
        return "success";
    }

    /** Authenticate company login — only approved accounts */
    public CompanyAccount login(String username, String password) {
        Optional<CompanyAccount> opt = accountRepo.findByUsername(username);
        if (opt.isEmpty()) return null;
        CompanyAccount acc = opt.get();
        if (!acc.getPassword().equals(password)) return null;
        if (!acc.isApproved()) return null;   // not yet approved
        enrich(acc);
        return acc;
    }

    /** Admin: approve a company account */
    public boolean approve(int accountId) {
        Optional<CompanyAccount> opt = accountRepo.findById(accountId);
        if (opt.isEmpty()) return false;
        CompanyAccount acc = opt.get();
        acc.setApproved(true);
        accountRepo.save(acc);
        return true;
    }

    /** Admin: reject (delete) a pending company account */
    public boolean reject(int accountId) {
        if (!accountRepo.existsById(accountId)) return false;
        accountRepo.deleteById(accountId);
        return true;
    }

    /** All pending accounts (for admin) */
    public List<CompanyAccount> getPendingAccounts() {
        List<CompanyAccount> list = accountRepo.findByApprovedFalse();
        enrichAll(list);
        return list;
    }

    /** All approved accounts */
    public List<CompanyAccount> getApprovedAccounts() {
        List<CompanyAccount> list = accountRepo.findByApprovedTrue();
        enrichAll(list);
        return list;
    }

    public Optional<CompanyAccount> getByCompanyId(int companyId) {
        Optional<CompanyAccount> opt = accountRepo.findByCompanyId(companyId);
        opt.ifPresent(this::enrich);
        return opt;
    }

    public boolean usernameExists(String username) {
        return accountRepo.existsByUsername(username);
    }
}
