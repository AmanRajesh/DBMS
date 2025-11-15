// src/main/java/com/libraryservice/analytics/AnalyticsController.java
package org.example.libraryservice.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    // Inject RecommendationService if you want trending books here

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        // The "trending_books" logic is complex (rentals in last 30 days)
        // For simplicity, we'll use the queries from RentalRepository

        Map<String, Object> response = Map.of(
                "top_authors", analyticsService.getTopAuthors(),
                "popular_genres", analyticsService.getPopularGenres(),
                "user_patterns", analyticsService.getUserPatterns()
                // "trending_books" would require a more complex query
        );

        return ResponseEntity.ok(response);
    }
}