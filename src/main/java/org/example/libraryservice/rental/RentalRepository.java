package org.example.libraryservice.rental;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    // For /user/history
    List<Rental> findByUserIdOrderByRentalDateDesc(Long userId);
    List<Rental> findByStatus(String status);
    // For /user/active (rentals that are not returned/overdue)
    List<Rental> findByUserIdAndStatus(Long userId, String status);

    // --- Analytics Queries ---

    // FIX 1: Removed "LIMIT 10" and added Pageable parameter
    @Query("SELECT r.bookAuthors as authors, COUNT(r) as count FROM Rental r GROUP BY r.bookAuthors ORDER BY count DESC")
    Page<Map<String, Object>> findTopAuthors(Pageable pageable);

    // FIX 2: Removed "LIMIT 10" and added Pageable parameter
    @Query("SELECT r.bookGenre as genre, COUNT(r) as count FROM Rental r GROUP BY r.bookGenre ORDER BY count DESC")
    Page<Map<String, Object>> findPopularGenres(Pageable pageable);

    // FIX 3: Replaced DATEDIFF with PostgreSQL-compatible EXTRACT(EPOCH ...) to get avg duration in days
    @Query("SELECT COUNT(r) as totalRentals, AVG((EXTRACT(EPOCH FROM r.returnDate) - EXTRACT(EPOCH FROM r.rentalDate)) / 86400.0) as avgDuration " +
            "FROM Rental r WHERE r.returnDate IS NOT NULL")
    Map<String, Object> findUserPatterns();
}