// src/main/java/com/libraryservice/rental/RentalController.java
package org.example.libraryservice.rental;

import org.example.libraryservice.dto.RentalRequest;
import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.example.libraryservice.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/issue")
    public ResponseEntity<?> issueBook(@RequestBody RentalRequest rentalRequest) {
        try {
            // 2. VALIDATION: Check if User ID was provided
            if (rentalRequest.getUserId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID is required for issuing books."));
            }

            // 3. FETCH STUDENT: Find the user based on the ID sent by Admin
            User student = userService.getUserById(rentalRequest.getUserId())
                    .orElseThrow(() -> new RuntimeException("Student not found with ID: " + rentalRequest.getUserId()));

            // 4. ISSUE BOOK: Pass the *Student* (not the Admin) to the service
            Rental rental = rentalService.issueBook(rentalRequest.getBookId(), student);

            return ResponseEntity.ok(rental);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/request")
    public ResponseEntity<?> requestBook(@RequestBody RentalRequest rentalRequest, @AuthenticationPrincipal User user) {
        try {
            Rental rental = rentalService.requestBook(rentalRequest.getBookId(), user);
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
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/issued")
    public ResponseEntity<List<Rental>> getAllIssuedBooks() {
        return ResponseEntity.ok(rentalService.getAllIssuedBooks());
    }
}