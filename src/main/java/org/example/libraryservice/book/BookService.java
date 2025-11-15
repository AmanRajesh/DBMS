// src/main/java/com/libraryservice/book/BookService.java
package org.example.libraryservice.book;

import org.example.libraryservice.dto.BookDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Page<Book> getAllBooks(String genre, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        if (genre != null && !genre.isEmpty()) {
            return bookRepository.findByIsAvailableTrueAndGenre(genre, pageable);
        }
        return bookRepository.findByIsAvailableTrue(pageable);
    }

    public Optional<Book> getBookById(String id) {
        // We don't need to get tags separately, MongoDB handles the list
        return bookRepository.findById(id);
    }

    public List<Book> searchBooks(String query) {
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
        book.setAvailableCopies(bookDto.getTotalCopies()); // Initially all are available
        book.setDescription(bookDto.getDescription());
        book.setTags(bookDto.getTags());

        return bookRepository.save(book);
    }

    public Book updateBook(String id, BookDto bookDetails) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Only update fields that are in the original node route
        book.setTitle(bookDetails.getTitle());
        book.setAuthors(bookDetails.getAuthors());
        book.setGenre(bookDetails.getGenre());
        // The original route only updated available_copies, not total
        if (bookDetails.getAvailableCopies() > 0) {
            book.setAvailableCopies(bookDetails.getAvailableCopies());
        }

        return bookRepository.save(book);
    }
}