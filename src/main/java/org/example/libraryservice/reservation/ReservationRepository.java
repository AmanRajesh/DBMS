// src/main/java/com/libraryservice/reservation/ReservationRepository.java
package org.example.libraryservice.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdAndStatusOrderByReservationDateDesc(Long userId, String status);
}