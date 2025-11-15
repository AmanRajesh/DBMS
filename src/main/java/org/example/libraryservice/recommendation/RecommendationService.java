// src/main/java/com/libraryservice/recommendation/RecommendationService.java
package org.example.libraryservice.recommendation;

import org.example.libraryservice.book.Book;
import org.example.libraryservice.book.BookRepository;
import org.example.libraryservice.rental.Rental;
import org.example.libraryservice.rental.RentalRepository;
import org.example.libraryservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RentalRepository rentalRepository; // Postgres
    private final BookRepository bookRepository;     // Mongo

    public Map<String, Object> getRecommendations(User user) {

        // 1. Get user's borrowing history (from Postgres)
        List<Rental> userHistory = rentalRepository.findByUserIdOrderByRentalDateDesc(user.getId());

        // 2. Get unique genres from history
        List<String> genres = userHistory.stream()
                .map(Rental::getBookGenre)
                .distinct()
                .limit(5) // Match original query
                .collect(Collectors.toList());

        if (genres.isEmpty()) {
            // 3. No history? Return trending books (from Mongo)
            List<Book> trending = bookRepository.findTop10ByOrderByCreatedAtDesc();
            return Map.of(
                    "recommendations", trending,
                    "reason", "trending"
            );
        }

        // 4. Get recommendations based on genres (from Mongo)
        List<Book> recommendations = bookRepository.findByGenreIn(genres);

        // 5. Filter out books user has already rented
        List<String> rentedBookIds = userHistory.stream()
                .map(Rental::getBookId)
                .collect(Collectors.toList());

        List<Book> finalRecs = recommendations.stream()
                .filter(book -> !rentedBookIds.contains(book.getId()))
                .limit(10)
                .collect(Collectors.toList());

        return Map.of(
                "recommendations", finalRecs,
                "reason", "genre_similarity"
        );
    }
}