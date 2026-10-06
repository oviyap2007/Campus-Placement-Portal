package com.CampusPlacement.portal.repository;

import com.CampusPlacement.portal.model.CompanyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyAccountRepository extends JpaRepository<CompanyAccount, Integer> {
    Optional<CompanyAccount> findByUsername(String username);
    Optional<CompanyAccount> findByCompanyId(int companyId);
    List<CompanyAccount> findByApprovedFalse();
    List<CompanyAccount> findByApprovedTrue();
    boolean existsByUsername(String username);
}
