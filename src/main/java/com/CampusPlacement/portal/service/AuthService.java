package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.PlacementOfficer;
import com.CampusPlacement.portal.model.Student;
import com.CampusPlacement.portal.repository.PlacementOfficerRepository;
import com.CampusPlacement.portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired private StudentRepository         studentRepo;
    @Autowired private PlacementOfficerRepository officerRepo;

    /** Student login — returns Student or null */
    public Student loginStudent(String username, String password) {
        Optional<Student> student = studentRepo.findByUsername(username.trim());
        if (student.isPresent() && student.get().getPassword().equals(password.trim())) {
            return student.get();
        }
        return null;
    }

    /** Admin login — returns PlacementOfficer or null */
    public PlacementOfficer loginAdmin(String username, String password) {
        Optional<PlacementOfficer> officer =
                officerRepo.findByUsernameAndPassword(username.trim(), password.trim());
        return officer.orElse(null);
    }
}
