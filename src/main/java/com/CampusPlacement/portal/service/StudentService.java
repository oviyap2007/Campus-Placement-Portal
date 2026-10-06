package com.CampusPlacement.portal.service;

import com.CampusPlacement.portal.model.Student;
import com.CampusPlacement.portal.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired private StudentRepository studentRepo;

    public List<Student> getAllStudents() { return studentRepo.findAll(); }

    public Optional<Student> getById(int id) { return studentRepo.findById(id); }

    public Student save(Student student) { return studentRepo.save(student); }

    public void delete(int id) { studentRepo.deleteById(id); }

    public boolean usernameExists(String username) {
        return studentRepo.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return studentRepo.existsByEmail(email);
    }

    public long countPlaced() {
        return studentRepo.findByPlaced(true).size();
    }

    public long countTotal() { return studentRepo.count(); }
}
