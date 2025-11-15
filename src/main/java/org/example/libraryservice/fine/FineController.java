// src/main/java/com/libraryservice/fine/FineController.java
package org.example.libraryservice.fine;

import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @GetMapping("/user")
    public ResponseEntity<List<Fine>> getUserFines(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(fineService.getUserFines(user.getId()));
    }

    @PostMapping("/{fineId}/pay")
    public ResponseEntity<?> payFine(@PathVariable Long fineId, @AuthenticationPrincipal User user) {
        try {
            return ResponseEntity.ok(fineService.payFine(fineId, user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}