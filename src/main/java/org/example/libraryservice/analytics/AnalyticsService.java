package org.example.libraryservice.analytics;

import org.example.libraryservice.rental.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final RentalRepository rentalRepository;
    // You could also inject BookRepository for "trending"

    public List<Map<String, Object>> getTopAuthors() {
        // FIX 1: Create a Pageable object to request the first page with 10 items
        Pageable topTen = PageRequest.of(0, 10);
        return rentalRepository.findTopAuthors(topTen).getContent();
    }

    public List<Map<String, Object>> getPopularGenres() {
        // FIX 2: Create a Pageable object to request the first page with 10 items
        Pageable topTen = PageRequest.of(0, 10);
        return rentalRepository.findPopularGenres(topTen).getContent();
    }

    public Map<String, Object> getUserPatterns() {
        // This method was fine, just calls the fixed repository query
        return rentalRepository.findUserPatterns();
    }
}