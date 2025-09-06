package com.exam.library.service;

import com.exam.library.dto.request.BookDto;
import com.exam.library.dto.request.CreateBookRequest;
import com.exam.library.exception.BookNotFoundException;
import com.exam.library.exception.InvalidIsbnException;
import com.exam.library.model.Book;
import com.exam.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional
    public Mono<BookDto> createBook(CreateBookRequest request) {
        log.info("Creating book with ISBN: {} and title: {}", request.getIsbn(), request.getTitle());
        
        return validateIsbnTitleAuthorConsistency(request.getIsbn(), request.getTitle(), request.getAuthor())
                .then(Mono.defer(() -> {
                    Book book = Book.builder()
                            .isbn(request.getIsbn())
                            .title(request.getTitle())
                            .author(request.getAuthor())
                            .build();
                    
                    return bookRepository.save(book);
                }))
                .flatMap(this::mapToDtoWithAvailability)
                .doOnSuccess(dto -> log.info("Successfully created book with ID: {}", dto.getId()))
                .doOnError(error -> log.error("Failed to create book: {}", error.getMessage()));
    }

    @Transactional(readOnly = true)
    public Flux<BookDto> getAllBooks() {
        log.info("Retrieving all books");
        return bookRepository.findAll()
                .flatMap(this::mapToDtoWithAvailability)
                .doOnComplete(() -> log.info("Successfully retrieved all books"));
    }

    @Transactional(readOnly = true)
    public Flux<BookDto> getAvailableBooks() {
        log.info("Retrieving available books");
        return bookRepository.findAvailableBooks()
                .flatMap(this::mapToDtoWithAvailability)
                .doOnComplete(() -> log.info("Successfully retrieved available books"));
    }

    @Transactional(readOnly = true)
    public Mono<BookDto> getBookById(UUID id) {
        log.info("Retrieving book with ID: {}", id);
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with ID: " + id)))
                .flatMap(this::mapToDtoWithAvailability)
                .doOnSuccess(dto -> log.info("Successfully retrieved book: {}", dto.getTitle()))
                .doOnError(error -> log.error("Failed to retrieve book with ID {}: {}", id, error.getMessage()));
    }

    @Transactional(readOnly = true)
    public Flux<BookDto> getBooksByIsbn(String isbn) {
        log.info("Retrieving books with ISBN: {}", isbn);
        return bookRepository.findByIsbn(isbn)
                .flatMap(this::mapToDtoWithAvailability)
                .doOnComplete(() -> log.info("Successfully retrieved books with ISBN: {}", isbn));
    }

    @Transactional(readOnly = true)
    public Flux<BookDto> searchBooksByTitle(String title) {
        log.info("Searching books by title: {}", title);
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .flatMap(this::mapToDtoWithAvailability)
                .doOnComplete(() -> log.info("Successfully searched books by title: {}", title));
    }

    @Transactional(readOnly = true)
    public Flux<BookDto> searchBooksByAuthor(String author) {
        log.info("Searching books by author: {}", author);
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .flatMap(this::mapToDtoWithAvailability)
                .doOnComplete(() -> log.info("Successfully searched books by author: {}", author));
    }

    @Transactional
    public Mono<BookDto> updateBook(UUID id, CreateBookRequest request) {
        log.info("Updating book with ID: {}", id);
        
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new BookNotFoundException("Book not found with ID: " + id)))
                .flatMap(existingBook -> validateIsbnTitleAuthorConsistency(request.getIsbn(), request.getTitle(), request.getAuthor())
                        .then(Mono.just(existingBook)))
                .map(book -> {
                    book.setIsbn(request.getIsbn());
                    book.setTitle(request.getTitle());
                    book.setAuthor(request.getAuthor());
                    return book;
                })
                .flatMap(bookRepository::save)
                .flatMap(this::mapToDtoWithAvailability)
                .doOnSuccess(dto -> log.info("Successfully updated book with ID: {}", dto.getId()))
                .doOnError(error -> log.error("Failed to update book with ID {}: {}", id, error.getMessage()));
    }

    @Transactional
    public Mono<Void> deleteBook(UUID id) {
        log.info("Deleting book with ID: {}", id);
        
        return bookRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BookNotFoundException("Book not found with ID: " + id));
                    }
                    return bookRepository.isBookBorrowed(id);
                })
                .flatMap(isBorrowed -> {
                    if (isBorrowed) {
                        return Mono.error(new IllegalStateException("Cannot delete book that is currently borrowed"));
                    }
                    return bookRepository.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Successfully deleted book with ID: {}", id))
                .doOnError(error -> log.error("Failed to delete book with ID {}: {}", id, error.getMessage()));
    }

    @Transactional(readOnly = true)
    public Mono<Boolean> isBookAvailable(UUID bookId) {
        return bookRepository.isBookBorrowed(bookId)
                .map(borrowed -> !borrowed);
    }

    private Mono<Void> validateIsbnTitleAuthorConsistency(String isbn, String title, String author) {
        return bookRepository.findByIsbn(isbn)
                .collectList()
                .flatMap(existingBooks -> {
                    if (!existingBooks.isEmpty()) {
                        Book firstBook = existingBooks.get(0);
                        if (!firstBook.getTitle().equals(title) || !firstBook.getAuthor().equals(author)) {
                            return Mono.error(new InvalidIsbnException(
                                    "Books with ISBN " + isbn + " must have title '" + firstBook.getTitle() + 
                                    "' and author '" + firstBook.getAuthor() + "'"));
                        }
                    }
                    return Mono.empty();
                });
    }

    private Mono<BookDto> mapToDtoWithAvailability(Book book) {
        return bookRepository.isBookBorrowed(book.getId())
                .map(isBorrowed -> BookDto.builder()
                        .id(book.getId())
                        .isbn(book.getIsbn())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .createdAt(book.getCreatedAt())
                        .updatedAt(book.getUpdatedAt())
                        .isAvailable(!isBorrowed)
                        .build());
    }
}