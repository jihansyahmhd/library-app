package com.exam.library.repository;

import com.exam.library.model.Book;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BookRepository extends R2dbcRepository<Book, UUID> {

    Flux<Book> findByIsbn(String isbn);

    Flux<Book> findByTitleContainingIgnoreCase(String title);

    Flux<Book> findByAuthorContainingIgnoreCase(String author);

    @Query("SELECT b.* FROM books b WHERE b.id NOT IN " +
            "(SELECT br.book_id FROM borrowing_records br WHERE br.returned_at IS NULL)")
    Flux<Book> findAvailableBooks();

    @Query("SELECT CASE WHEN EXISTS(" +
            "SELECT 1 FROM borrowing_records br WHERE br.book_id = :bookId AND br.returned_at IS NULL" +
            ") THEN true ELSE false END")
    Mono<Boolean> isBookBorrowed(UUID bookId);
}
