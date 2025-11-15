// src/main/java/com/libraryservice/rental/RentalController.java
package org.example.libraryservice.rental;

import org.example.libraryservice.dto.RentalRequest;
import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("/issue")
    public ResponseEntity<?> issueBook(@RequestBody RentalRequest rentalRequest, @AuthenticationPrincipal User user) {
        try {
            Rental rental = rentalService.issueBook(rentalRequest.getBookId(), user);
            return ResponseEntity.ok(rental);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/return/{rentalId}")
    public ResponseEntity<?> returnBook(@PathVariable Long rentalId, @AuthenticationPrincipal User user) {
        try {
            Rental rental = rentalService.returnBook(rentalId, user);
            return ResponseEntity.ok(rental);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/history")
    public ResponseEntity<List<Rental>> getUserHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(rentalService.getUserRentalHistory(user.getId()));
    }
}