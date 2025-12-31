package org.example.libraryservice.book;

import org.example.libraryservice.dto.BookDto;

import org.example.libraryservice.nlp.NLPService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    // 👇 2. Inject the service here so we can use it
    private final NLPService nlpService;

    public Page<Book> getAllBooks(String genre, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        if (genre != null && !genre.isEmpty()) {
            return bookRepository.findByIsAvailableTrueAndGenre(genre, pageable);
        }
        return bookRepository.findByIsAvailableTrue(pageable);
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    public List<Book> searchBooks(String query) {
        // This is your old simple search
        return bookRepository.searchBooks(query);
    }

    // --- Admin Methods ---

    public Book addBook(BookDto bookDto) {
        Book book = new Book();
        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setAuthors(bookDto.getAuthors());
        book.setGenre(bookDto.getGenre());
        book.setPublisher(bookDto.getPublisher());
        book.setTotalCopies(bookDto.getTotalCopies());
        book.setAvailableCopies(bookDto.getTotalCopies());
        book.setDescription(bookDto.getDescription());
        book.setTags(bookDto.getTags());
        book.setImageUrl(bookDto.getImageUrl());
        if (bookDto.getImageUrl() != null) {
            book.setImageUrl(bookDto.getImageUrl());
        }

        return bookRepository.save(book);
    }

    public Book updateBook(String id, BookDto bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setTitle(bookDetails.getTitle());
        book.setAuthors(bookDetails.getAuthors());
        book.setGenre(bookDetails.getGenre());
        book.setImageUrl(bookDetails.getImageUrl());
        if (bookDetails.getAvailableCopies() > 0) {
            book.setAvailableCopies(bookDetails.getAvailableCopies());
        }

        return bookRepository.save(book);
    }

    // 👇 3. The AI Search Logic
    public List<Book> smartSearch(String userQuery) {
        // A. Use AI to extract only the important words
        List<String> keywords = nlpService.extractKeywords(userQuery);

        System.out.println("DEBUG: Searching for keywords: " + keywords);

        // Fallback: If AI returns nothing, search the raw sentence
        if (keywords.isEmpty()) {
            return bookRepository.searchByKeyword(userQuery);
        }

        // 2. Search MongoDB using the new 'searchByKeyword' method
        Set<Book> results = new HashSet<>();
        for (String keyword : keywords) {
            // This now checks Description and Tags too!
            results.addAll(bookRepository.searchByKeyword(keyword));
        }

        return new ArrayList<>(results);
    }
}