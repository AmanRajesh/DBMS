// src/main/java/com/libraryservice/fine/FineRepository.java
package org.example.libraryservice.fine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface FineRepository extends JpaRepository<Fine, Long> {

    // For /user
    List<Fine> findByUserIdOrderByFineDateDesc(Long userId);

    // For admin /defaulters
    @Query("SELECT f.user.id as id, f.user.name as name, f.user.email as email, SUM(f.amount) as totalFine " +
            "FROM Fine f WHERE f.status = 'pending' " +
            "GROUP BY f.user.id, f.user.name, f.user.email " +
            "ORDER BY totalFine DESC")
    List<Map<String, Object>> findDefaulters();
}