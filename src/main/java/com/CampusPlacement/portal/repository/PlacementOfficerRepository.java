package com.CampusPlacement.portal.repository;

import com.CampusPlacement.portal.model.PlacementOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlacementOfficerRepository extends JpaRepository<PlacementOfficer, Integer> {
    Optional<PlacementOfficer> findByUsername(String username);
    Optional<PlacementOfficer> findByUsernameAndPassword(String username, String password);
}
