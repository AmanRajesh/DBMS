// src/main/java/com/libraryservice/book/BookRepository.java
package org.example.libraryservice.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface BookRepository extends MongoRepository<Book, String> {

    // For search (replaces ILIKE)
    @Query("{$or:[ {'title': {$regex : ?0, $options: 'i'}}, {'authors': {$regex : ?0, $options: 'i'}}, {'genre': {$regex : ?0, $options: 'i'}}, {'description': {$regex : ?0, $options: 'i'}} ]}")
    List<Book> searchBooks(String query);

    // For recommendations
    List<Book> findByGenreIn(List<String> genres);

    // For pagination and genre filter from routes/books.js
    Page<Book> findByIsAvailableTrue(Pageable pageable);
    Page<Book> findByIsAvailableTrueAndGenre(String genre, Pageable pageable);

    // For recommendations (trending)
    List<Book> findTop10ByOrderByCreatedAtDesc();
}