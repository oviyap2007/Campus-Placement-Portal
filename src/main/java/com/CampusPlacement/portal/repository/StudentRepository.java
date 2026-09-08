package com.CampusPlacement.portal.repository;

import com.CampusPlacement.portal.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    Optional<Student> findByUsername(String username);
    Optional<Student> findByEmail(String email);
    List<Student> findByIsPlaced(boolean isPlaced);
    List<Student> findByDepartment(String department);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}