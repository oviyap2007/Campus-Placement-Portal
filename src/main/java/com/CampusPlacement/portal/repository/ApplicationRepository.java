package com.CampusPlacement.portal.repository;

import com.CampusPlacement.portal.model.Application;
import com.CampusPlacement.portal.model.Application.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    List<Application> findByStudentId(int studentId);
    List<Application> findByJobId(int jobId);
    boolean existsByStudentIdAndJobId(int studentId, int jobId);
    List<Application> findByStatus(Status status);
}
