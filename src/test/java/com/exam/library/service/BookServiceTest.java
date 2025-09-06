package com.exam.library.service;

import com.exam.library.dto.request.BookDto;
import com.exam.library.dto.request.CreateBookRequest;
import com.exam.library.exception.BookNotFoundException;
import com.exam.library.exception.InvalidIsbnException;
import com.exam.library.model.Book;
import com.exam.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private CreateBookRequest createBookRequest;
    private UUID bookId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        bookId = UUID.randomUUID();
        now = LocalDateTime.now();

        testBook = Book.builder()
                .id(bookId)
                .isbn("978-3-16-148410-0")
                .title("Test Book")
                .author("Test Author")
                .createdAt(now)
                .updatedAt(now)
                .build();

        createBookRequest = CreateBookRequest.builder()
                .isbn("978-3-16-148410-0")
                .title("Test Book")
                .author("Test Author")
                .build();
    }

    @Test
    @DisplayName("Should create book successfully when valid request provided")
    void createBook_ValidRequest_ShouldReturnBookDto() {
        // Given
        when(bookRepository.findByIsbn(createBookRequest.getIsbn()))
                .thenReturn(Flux.empty());
        when(bookRepository.save(any(Book.class)))
                .thenReturn(Mono.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.createBook(createBookRequest))
                .expectNextMatches(dto ->
                        dto.getId().equals(bookId) &&
                                dto.getIsbn().equals("978-3-16-148410-0") &&
                                dto.getTitle().equals("Test Book") &&
                                dto.getAuthor().equals("Test Author") &&
                                Boolean.TRUE.equals(dto.isAvailable())
                )
                .verifyComplete();

        verify(bookRepository).findByIsbn("978-3-16-148410-0");
        verify(bookRepository).save(any(Book.class));
        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should create book when ISBN exists with same title and author")
    void createBook_IsbnExistsWithSameTitleAndAuthor_ShouldReturnBookDto() {
        // Given
        Book existingBook = Book.builder()
                .isbn("978-3-16-148410-0")
                .title("Test Book")
                .author("Test Author")
                .build();

        when(bookRepository.findByIsbn(createBookRequest.getIsbn()))
                .thenReturn(Flux.just(existingBook));
        when(bookRepository.save(any(Book.class)))
                .thenReturn(Mono.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.createBook(createBookRequest))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw InvalidIsbnException when ISBN exists with different title")
    void createBook_IsbnExistsWithDifferentTitle_ShouldThrowException() {
        // Given
        Book existingBook = Book.builder()
                .isbn("978-3-16-148410-0")
                .title("Different Title")
                .author("Test Author")
                .build();

        when(bookRepository.findByIsbn(createBookRequest.getIsbn()))
                .thenReturn(Flux.just(existingBook));

        // When & Then
        StepVerifier.create(bookService.createBook(createBookRequest))
                .expectError(InvalidIsbnException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw InvalidIsbnException when ISBN exists with different author")
    void createBook_IsbnExistsWithDifferentAuthor_ShouldThrowException() {
        // Given
        Book existingBook = Book.builder()
                .isbn("978-3-16-148410-0")
                .title("Test Book")
                .author("Different Author")
                .build();

        when(bookRepository.findByIsbn(createBookRequest.getIsbn()))
                .thenReturn(Flux.just(existingBook));

        // When & Then
        StepVerifier.create(bookService.createBook(createBookRequest))
                .expectError(InvalidIsbnException.class)
                .verify();
    }

    @Test
    @DisplayName("Should return all books with availability status")
    void getAllBooks_ShouldReturnAllBooksWithAvailability() {
        // Given
        when(bookRepository.findAll())
                .thenReturn(Flux.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(bookService.getAllBooks())
                .expectNextMatches(dto ->
                        dto.getId().equals(bookId) &&
                                Boolean.FALSE.equals(dto.isAvailable())
                )
                .verifyComplete();

        verify(bookRepository).findAll();
        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should return available books only")
    void getAvailableBooks_ShouldReturnOnlyAvailableBooks() {
        // Given
        when(bookRepository.findAvailableBooks())
                .thenReturn(Flux.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.getAvailableBooks())
                .expectNextMatches(dto ->
                        dto.getId().equals(bookId) &&
                                Boolean.TRUE.equals(dto.isAvailable())
                )
                .verifyComplete();

        verify(bookRepository).findAvailableBooks();
        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should return book by ID when book exists")
    void getBookById_BookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findById(bookId))
                .thenReturn(Mono.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.getBookById(bookId))
                .expectNextMatches(dto -> dto.getId().equals(bookId))
                .verifyComplete();

        verify(bookRepository).findById(bookId);
        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book ID not found")
    void getBookById_BookNotFound_ShouldThrowException() {
        // Given
        when(bookRepository.findById(bookId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(bookService.getBookById(bookId))
                .expectError(BookNotFoundException.class)
                .verify();

        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).isBookBorrowed(any());
    }

    @Test
    @DisplayName("Should return books by ISBN")
    void getBooksByIsbn_ShouldReturnBooksWithSameIsbn() {
        // Given
        String isbn = "978-3-16-148410-0";
        when(bookRepository.findByIsbn(isbn))
                .thenReturn(Flux.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.getBooksByIsbn(isbn))
                .expectNextMatches(dto -> dto.getIsbn().equals(isbn))
                .verifyComplete();

        verify(bookRepository).findByIsbn(isbn);
        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should search books by title containing keyword")
    void searchBooksByTitle_ShouldReturnMatchingBooks() {
        // Given
        String titleKeyword = "Test";
        when(bookRepository.findByTitleContainingIgnoreCase(titleKeyword))
                .thenReturn(Flux.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.searchBooksByTitle(titleKeyword))
                .expectNextMatches(dto -> dto.getTitle().contains(titleKeyword))
                .verifyComplete();

        verify(bookRepository).findByTitleContainingIgnoreCase(titleKeyword);
        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should search books by author containing keyword")
    void searchBooksByAuthor_ShouldReturnMatchingBooks() {
        // Given
        String authorKeyword = "Test";
        when(bookRepository.findByAuthorContainingIgnoreCase(authorKeyword))
                .thenReturn(Flux.just(testBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.searchBooksByAuthor(authorKeyword))
                .expectNextMatches(dto -> dto.getAuthor().contains(authorKeyword))
                .verifyComplete();

        verify(bookRepository).findByAuthorContainingIgnoreCase(authorKeyword);
        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should update book successfully when book exists and valid request provided")
    void updateBook_ValidRequest_ShouldReturnUpdatedBookDto() {
        // Given
        CreateBookRequest updateRequest = CreateBookRequest.builder()
                .isbn("978-3-16-148410-1")
                .title("Updated Title")
                .author("Updated Author")
                .build();

        Book updatedBook = Book.builder()
                .id(bookId)
                .isbn("978-3-16-148410-1")
                .title("Updated Title")
                .author("Updated Author")
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(bookRepository.findById(bookId))
                .thenReturn(Mono.just(testBook));
        when(bookRepository.findByIsbn(updateRequest.getIsbn()))
                .thenReturn(Flux.empty());
        when(bookRepository.save(any(Book.class)))
                .thenReturn(Mono.just(updatedBook));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.updateBook(bookId, updateRequest))
                .expectNextMatches(dto ->
                        dto.getTitle().equals("Updated Title") &&
                                dto.getAuthor().equals("Updated Author")
                )
                .verifyComplete();

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when updating non-existent book")
    void updateBook_BookNotFound_ShouldThrowException() {
        // Given
        when(bookRepository.findById(bookId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(bookService.updateBook(bookId, createBookRequest))
                .expectError(BookNotFoundException.class)
                .verify();

        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete book successfully when book exists and not borrowed")
    void deleteBook_BookExistsAndNotBorrowed_ShouldDeleteSuccessfully() {
        // Given
        when(bookRepository.existsById(bookId))
                .thenReturn(Mono.just(true));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));
        when(bookRepository.deleteById(bookId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(bookService.deleteBook(bookId))
                .verifyComplete();

        verify(bookRepository).existsById(bookId);
        verify(bookRepository).isBookBorrowed(bookId);
        verify(bookRepository).deleteById(bookId);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when deleting non-existent book")
    void deleteBook_BookNotFound_ShouldThrowException() {
        // Given
        when(bookRepository.existsById(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.deleteBook(bookId))
                .expectError(BookNotFoundException.class)
                .verify();

        verify(bookRepository).existsById(bookId);
        verify(bookRepository, never()).isBookBorrowed(any());
        verify(bookRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when deleting borrowed book")
    void deleteBook_BookIsBorrowed_ShouldThrowException() {
        // Given
        when(bookRepository.existsById(bookId))
                .thenReturn(Mono.just(true));
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(bookService.deleteBook(bookId))
                .expectError(IllegalStateException.class)
                .verify();

        verify(bookRepository).existsById(bookId);
        verify(bookRepository).isBookBorrowed(bookId);
        verify(bookRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should return true when book is available (not borrowed)")
    void isBookAvailable_BookNotBorrowed_ShouldReturnTrue() {
        // Given
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(bookService.isBookAvailable(bookId))
                .expectNext(true)
                .verifyComplete();

        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should return false when book is not available (borrowed)")
    void isBookAvailable_BookIsBorrowed_ShouldReturnFalse() {
        // Given
        when(bookRepository.isBookBorrowed(bookId))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(bookService.isBookAvailable(bookId))
                .expectNext(false)
                .verifyComplete();

        verify(bookRepository).isBookBorrowed(bookId);
    }

    @Test
    @DisplayName("Should handle empty flux for getAllBooks")
    void getAllBooks_EmptyRepository_ShouldReturnEmptyFlux() {
        // Given
        when(bookRepository.findAll())
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(bookService.getAllBooks())
                .verifyComplete();

        verify(bookRepository).findAll();
    }

    @Test
    @DisplayName("Should handle repository error gracefully")
    void createBook_RepositoryError_ShouldPropagateError() {
        // Given
        when(bookRepository.findByIsbn(createBookRequest.getIsbn()))
                .thenReturn(Flux.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(bookService.createBook(createBookRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(bookRepository).findByIsbn("978-3-16-148410-0");
        verify(bookRepository, never()).save(any());
    }
}