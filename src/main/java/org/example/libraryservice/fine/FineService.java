// src/main/java/com/libraryservice/fine/FineService.java
package org.example.libraryservice.fine;

import org.example.libraryservice.rental.Rental;
import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;

    @Transactional
    public Fine createFine(User user, Rental rental, double amount) {
        Fine fine = new Fine();
        fine.setUser(user);
        fine.setRental(rental);
        fine.setAmount(amount);
        fine.setStatus("pending");
        fine.setFineDate(Instant.now());
        return fineRepository.save(fine);
    }

    @Transactional
    public Fine payFine(Long fineId, Long userId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new RuntimeException("Fine not found"));

        if (!fine.getUser().getId().equals(userId)) {
            throw new SecurityException("User not authorized to pay this fine");
        }

        fine.setStatus("paid");
        fine.setPaymentDate(Instant.now());
        return fineRepository.save(fine);
    }

    public List<Fine> getUserFines(Long userId) {
        return fineRepository.findByUserIdOrderByFineDateDesc(userId);
    }

    public List<Map<String, Object>> getDefaulters() {
        return fineRepository.findDefaulters();
    }
}