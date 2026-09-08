package com.CampusPlacement.portal.repository;

import com.CampusPlacement.portal.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {
    List<Job> findByIsActiveTrue();
    List<Job> findByIsActiveTrueAndMinCgpaLessThanEqual(double cgpa);
    List<Job> findByCompanyId(int companyId);
}
