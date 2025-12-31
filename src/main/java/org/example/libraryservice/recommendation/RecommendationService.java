package org.example.libraryservice.recommendation;

import org.example.libraryservice.book.Book;
import org.example.libraryservice.book.BookRepository;
import org.example.libraryservice.nlp.NLPService;
import org.example.libraryservice.rental.Rental;
import org.example.libraryservice.rental.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;
    private final NLPService nlpService;

    public List<Book> getRecommendations(Long userId) {
        // 1. Get User's Rental History
        List<Rental> history = rentalRepository.findByUserId(userId);

        // If user is new (no history), return empty list (or could return 'Popular Books')
        if (history.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. Fetch details of the books they read
        Set<String> readBookIds = history.stream()
                .map(Rental::getBookId)
                .collect(Collectors.toSet());
        List<Book> readBooks = bookRepository.findAllById(readBookIds);

        // 3. Build a "Corpus" (One giant string of everything they read)
        StringBuilder corpus = new StringBuilder();
        for (Book book : readBooks) {
            corpus.append(book.getTitle()).append(" ");
            corpus.append(book.getGenre()).append(" ");
            corpus.append(book.getDescription()).append(" ");
        }

        // 4. Use AI to extract Interests (Keywords)
        List<String> allKeywords = nlpService.extractKeywords(corpus.toString());

        // 5. Find the "Top 5" most frequent keywords
        // (e.g. if they read 3 space books, "space" appears 3 times -> High priority)
        List<String> topKeywords = getTopKeywords(allKeywords, 5);
        System.out.println("DEBUG: User " + userId + " Interests: " + topKeywords);

        // 6. Search for new books matching these keywords
        Set<Book> recommendations = new HashSet<>();
        for (String keyword : topKeywords) {
            // Re-use the smart search query we made yesterday
            recommendations.addAll(bookRepository.searchByKeyword(keyword));
        }

        // 7. Filter: Remove books they have ALREADY read
        return recommendations.stream()
                .filter(book -> !readBookIds.contains(book.getId())) // Remove read books
                .limit(10) // Limit to 10 suggestions
                .collect(Collectors.toList());
    }

    // Helper: Sorts words by frequency (High to Low)
    private List<String> getTopKeywords(List<String> keywords, int limit) {
        return keywords.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())) // Count frequency
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // Sort Descending
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}