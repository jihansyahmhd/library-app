package com.exam.library.service;

import com.exam.library.dto.request.BorrowBookRequest;
import com.exam.library.dto.request.BorrowingRecordDto;
import com.exam.library.dto.request.ReturnBookRequest;
import com.exam.library.exception.*;
import com.exam.library.model.Book;
import com.exam.library.model.Borrower;
import com.exam.library.model.BorrowingRecord;
import com.exam.library.repository.BookRepository;
import com.exam.library.repository.BorrowerRepository;
import com.exam.library.repository.BorrowingRecordRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowingService Unit Tests")
class BorrowingServiceTest {

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private BorrowingRecord testBorrowingRecord;
    private Book testBook;
    private Borrower testBorrower;
    private BorrowBookRequest borrowBookRequest;
    private ReturnBookRequest returnBookRequest;
    private UUID borrowingRecordId;
    private UUID borrowerId;
    private UUID bookId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        borrowingRecordId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        now = LocalDateTime.now();

        testBorrower = Borrower.builder()
                .id(borrowerId)
                .name("John Doe")
                .email("john@example.com")
                .createdAt(now)
                .updatedAt(now)
                .build();

        testBook = Book.builder()
                .id(bookId)
                .isbn("978-3-16-148410-0")
                .title("Test Book")
                .author("Test Author")
                .createdAt(now)
                .updatedAt(now)
                .build();

        testBorrowingRecord = BorrowingRecord.builder()
                .id(borrowingRecordId)
                .borrowerId(borrowerId)
                .bookId(bookId)
                .borrowedAt(now)
                .dueDate(now.plusDays(14))
                .createdAt(now)
                .updatedAt(now)
                .build();

        borrowBookRequest = BorrowBookRequest.builder()
                .borrowerId(borrowerId)
                .bookId(bookId)
                .build();

        returnBookRequest = ReturnBookRequest.builder()
                .borrowerId(borrowerId)
                .bookId(bookId)
                .build();
    }

    @Test
    @DisplayName("Should borrow book successfully when all validations pass")
    void borrowBook_ValidRequest_ShouldReturnBorrowingRecordDto() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(true));
        when(bookRepository.existsById(bookId)).thenReturn(Mono.just(true));
        when(borrowingRecordRepository.findActiveBorrowingByBookId(bookId)).thenReturn(Mono.empty());
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(Mono.just(testBorrowingRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.borrowBook(borrowBookRequest))
                .expectNextMatches(dto ->
                        dto.getId().equals(borrowingRecordId) &&
                                dto.getBorrowerId().equals(borrowerId) &&
                                dto.getBookId().equals(bookId) &&
                                dto.getBorrowerName().equals("John Doe") &&
                                dto.getBorrowerEmail().equals("john@example.com") &&
                                dto.getBookTitle().equals("Test Book") &&
                                dto.getBookAuthor().equals("Test Author") &&
                                dto.getBookIsbn().equals("978-3-16-148410-0")
                )
                .verifyComplete();

        verify(borrowerRepository).existsById(borrowerId);
        verify(bookRepository).existsById(bookId);
        verify(borrowingRecordRepository).findActiveBorrowingByBookId(bookId);
        verify(borrowingRecordRepository).save(any(BorrowingRecord.class));
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when borrower does not exist")
    void borrowBook_BorrowerNotFound_ShouldThrowException() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(borrowingService.borrowBook(borrowBookRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof BorrowerNotFoundException &&
                                throwable.getMessage().contains(borrowerId.toString())
                )
                .verify();

        verify(borrowerRepository).existsById(borrowerId);
        verify(bookRepository, never()).existsById(any(UUID.class));
        verify(borrowingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when book does not exist")
    void borrowBook_BookNotFound_ShouldThrowException() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(true));
        when(bookRepository.existsById(bookId)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(borrowingService.borrowBook(borrowBookRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof BookNotFoundException &&
                                throwable.getMessage().contains(bookId.toString())
                )
                .verify();

        verify(borrowerRepository).existsById(borrowerId);
        verify(bookRepository).existsById(bookId);
        verify(borrowingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BookAlreadyBorrowedException when book is already borrowed")
    void borrowBook_BookAlreadyBorrowed_ShouldThrowException() {
        // Given
        BorrowingRecord activeBorrowing = BorrowingRecord.builder()
                .id(UUID.randomUUID())
                .borrowerId(UUID.randomUUID())
                .bookId(bookId)
                .borrowedAt(now)
                .build();

        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(true));
        when(bookRepository.existsById(bookId)).thenReturn(Mono.just(true));
        when(borrowingRecordRepository.findActiveBorrowingByBookId(bookId)).thenReturn(Mono.just(activeBorrowing));

        // When & Then
        StepVerifier.create(borrowingService.borrowBook(borrowBookRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof BookAlreadyBorrowedException &&
                                throwable.getMessage().contains(bookId.toString())
                )
                .verify();

        verify(borrowingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return book successfully when valid return request provided")
    void returnBook_ValidRequest_ShouldReturnBorrowingRecordDto() {
        // Given
        BorrowingRecord activeBorrowing = BorrowingRecord.builder()
                .id(borrowingRecordId)
                .borrowerId(borrowerId)
                .bookId(bookId)
                .borrowedAt(now.minusDays(7))
                .dueDate(now.plusDays(7))
                .build();

        BorrowingRecord returnedRecord = BorrowingRecord.builder()
                .id(borrowingRecordId)
                .borrowerId(borrowerId)
                .bookId(bookId)
                .borrowedAt(now.minusDays(7))
                .dueDate(now.plusDays(7))
                .returnedAt(now)
                .build();

        when(borrowingRecordRepository.findActiveBorrowingByBookId(bookId)).thenReturn(Mono.just(activeBorrowing));
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(Mono.just(returnedRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.returnBook(returnBookRequest))
                .expectNextMatches(dto ->
                        dto.getId().equals(borrowingRecordId) &&
                                dto.getReturnedAt() != null
                )
                .verifyComplete();

        verify(borrowingRecordRepository).findActiveBorrowingByBookId(bookId);
        verify(borrowingRecordRepository).save(any(BorrowingRecord.class));
    }

    @Test
    @DisplayName("Should throw BookNotBorrowedException when book is not currently borrowed")
    void returnBook_BookNotBorrowed_ShouldThrowException() {
        // Given
        when(borrowingRecordRepository.findActiveBorrowingByBookId(bookId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(borrowingService.returnBook(returnBookRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof BookNotBorrowedException &&
                                throwable.getMessage().contains(bookId.toString())
                )
                .verify();

        verify(borrowingRecordRepository).findActiveBorrowingByBookId(bookId);
        verify(borrowingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UnauthorizedBorrowerException when borrower mismatch")
    void returnBook_UnauthorizedBorrower_ShouldThrowException() {
        // Given
        UUID differentBorrowerId = UUID.randomUUID();
        BorrowingRecord activeBorrowing = BorrowingRecord.builder()
                .id(borrowingRecordId)
                .borrowerId(differentBorrowerId)
                .bookId(bookId)
                .borrowedAt(now.minusDays(7))
                .build();

        when(borrowingRecordRepository.findActiveBorrowingByBookId(bookId)).thenReturn(Mono.just(activeBorrowing));

        // When & Then
        StepVerifier.create(borrowingService.returnBook(returnBookRequest))
                .expectError(UnauthorizedBorrowerException.class)
                .verify();

        verify(borrowingRecordRepository).findActiveBorrowingByBookId(bookId);
        verify(borrowingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return all borrowing records")
    void getAllBorrowingRecords_ShouldReturnAllRecords() {
        // Given
        BorrowingRecord anotherRecord = BorrowingRecord.builder()
                .id(UUID.randomUUID())
                .borrowerId(UUID.randomUUID())
                .bookId(UUID.randomUUID())
                .borrowedAt(now)
                .build();

        when(borrowingRecordRepository.findAll()).thenReturn(Flux.just(testBorrowingRecord, anotherRecord));
        when(borrowerRepository.findById(any(UUID.class))).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(any(UUID.class))).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.getAllBorrowingRecords())
                .expectNextCount(2)
                .verifyComplete();

        verify(borrowingRecordRepository).findAll();
    }

    @Test
    @DisplayName("Should return active borrowing records only")
    void getActiveBorrowings_ShouldReturnActiveRecords() {
        // Given
        when(borrowingRecordRepository.findAllActiveBorrowings()).thenReturn(Flux.just(testBorrowingRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.getActiveBorrowings())
                .expectNextCount(1)
                .verifyComplete();

        verify(borrowingRecordRepository).findAllActiveBorrowings();
    }

    @Test
    @DisplayName("Should return borrowings by borrower when borrower exists")
    void getBorrowingsByBorrower_BorrowerExists_ShouldReturnBorrowings() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(true));
        when(borrowingRecordRepository.findByBorrowerId(borrowerId)).thenReturn(Flux.just(testBorrowingRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.getBorrowingsByBorrower(borrowerId))
                .expectNextCount(1)
                .verifyComplete();

        verify(borrowerRepository).existsById(borrowerId);
        verify(borrowingRecordRepository).findByBorrowerId(borrowerId);
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when getting borrowings for non-existent borrower")
    void getBorrowingsByBorrower_BorrowerNotFound_ShouldThrowException() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(borrowingService.getBorrowingsByBorrower(borrowerId))
                .expectErrorMatches(throwable ->
                        throwable instanceof BorrowerNotFoundException &&
                                throwable.getMessage().contains(borrowerId.toString())
                )
                .verify();

        verify(borrowerRepository).existsById(borrowerId);
        verify(borrowingRecordRepository, never()).findByBorrowerId(any());
    }

    @Test
    @DisplayName("Should return active borrowings by borrower when borrower exists")
    void getActiveBorrowingsByBorrower_BorrowerExists_ShouldReturnActiveBorrowings() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(true));
        when(borrowingRecordRepository.findActiveBorrowingsByBorrowerId(borrowerId)).thenReturn(Flux.just(testBorrowingRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.getActiveBorrowingsByBorrower(borrowerId))
                .expectNextCount(1)
                .verifyComplete();

        verify(borrowerRepository).existsById(borrowerId);
        verify(borrowingRecordRepository).findActiveBorrowingsByBorrowerId(borrowerId);
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when getting active borrowings for non-existent borrower")
    void getActiveBorrowingsByBorrower_BorrowerNotFound_ShouldThrowException() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(borrowingService.getActiveBorrowingsByBorrower(borrowerId))
                .expectErrorMatches(throwable ->
                        throwable instanceof BorrowerNotFoundException &&
                                throwable.getMessage().contains(borrowerId.toString())
                )
                .verify();

        verify(borrowerRepository).existsById(borrowerId);
        verify(borrowingRecordRepository, never()).findActiveBorrowingsByBorrowerId(any());
    }

    @Test
    @DisplayName("Should return borrowings by book when book exists")
    void getBorrowingsByBook_BookExists_ShouldReturnBorrowings() {
        // Given
        when(bookRepository.existsById(bookId)).thenReturn(Mono.just(true));
        when(borrowingRecordRepository.findByBookId(bookId)).thenReturn(Flux.just(testBorrowingRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.getBorrowingsByBook(bookId))
                .expectNextCount(1)
                .verifyComplete();

        verify(bookRepository).existsById(bookId);
        verify(borrowingRecordRepository).findByBookId(bookId);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when getting borrowings for non-existent book")
    void getBorrowingsByBook_BookNotFound_ShouldThrowException() {
        // Given
        when(bookRepository.existsById(bookId)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(borrowingService.getBorrowingsByBook(bookId))
                .expectErrorMatches(throwable ->
                        throwable instanceof BookNotFoundException &&
                                throwable.getMessage().contains(bookId.toString())
                )
                .verify();

        verify(bookRepository).existsById(bookId);
        verify(borrowingRecordRepository, never()).findByBookId(any());
    }

    @Test
    @DisplayName("Should return overdue borrowing records")
    void getOverdueBorrowings_ShouldReturnOverdueRecords() {
        // Given
        BorrowingRecord overdueRecord = BorrowingRecord.builder()
                .id(borrowingRecordId)
                .borrowerId(borrowerId)
                .bookId(bookId)
                .borrowedAt(now.minusDays(20))
                .dueDate(now.minusDays(5))
                .build();

        when(borrowingRecordRepository.findOverdueBorrowings()).thenReturn(Flux.just(overdueRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.getOverdueBorrowings())
                .expectNextCount(1)
                .verifyComplete();

        verify(borrowingRecordRepository).findOverdueBorrowings();
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void getAllBorrowingRecords_EmptyResults_ShouldReturnEmptyFlux() {
        // Given
        when(borrowingRecordRepository.findAll()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(borrowingService.getAllBorrowingRecords())
                .verifyComplete();

        verify(borrowingRecordRepository).findAll();
    }

    @Test
    @DisplayName("Should handle repository errors during borrow validation")
    void borrowBook_RepositoryError_ShouldPropagateError() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(borrowingService.borrowBook(borrowBookRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(borrowerRepository).existsById(borrowerId);
        verify(borrowingRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle missing borrower/book data gracefully in mapping")
    void mapToDtoWithDetails_MissingData_ShouldUseDefaults() {
        // Given
        when(borrowingRecordRepository.findAll()).thenReturn(Flux.just(testBorrowingRecord));
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.empty());
        when(bookRepository.findById(bookId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(borrowingService.getAllBorrowingRecords())
                .expectNextMatches(dto ->
                        dto.getBorrowerName().equals("Unknown") &&
                                dto.getBorrowerEmail().equals("Unknown") &&
                                dto.getBookTitle().equals("Unknown") &&
                                dto.getBookAuthor().equals("Unknown") &&
                                dto.getBookIsbn().equals("Unknown")
                )
                .verifyComplete();
    }

    @Test
    @DisplayName("Should handle repository save errors during return")
    void returnBook_SaveError_ShouldPropagateError() {
        // Given
        BorrowingRecord activeBorrowing = BorrowingRecord.builder()
                .id(borrowingRecordId)
                .borrowerId(borrowerId)
                .bookId(bookId)
                .borrowedAt(now.minusDays(7))
                .build();

        when(borrowingRecordRepository.findActiveBorrowingByBookId(bookId)).thenReturn(Mono.just(activeBorrowing));
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(Mono.error(new RuntimeException("Save failed")));

        // When & Then
        StepVerifier.create(borrowingService.returnBook(returnBookRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(borrowingRecordRepository).findActiveBorrowingByBookId(bookId);
        verify(borrowingRecordRepository).save(any(BorrowingRecord.class));
    }

    @Test
    @DisplayName("Should set correct due date when borrowing book")
    void borrowBook_ShouldSetCorrectDueDate() {
        // Given
        when(borrowerRepository.existsById(borrowerId)).thenReturn(Mono.just(true));
        when(bookRepository.existsById(bookId)).thenReturn(Mono.just(true));
        when(borrowingRecordRepository.findActiveBorrowingByBookId(bookId)).thenReturn(Mono.empty());
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenAnswer(invocation -> {
            BorrowingRecord record = invocation.getArgument(0);
            return Mono.just(record);
        });
        when(borrowerRepository.findById(borrowerId)).thenReturn(Mono.just(testBorrower));
        when(bookRepository.findById(bookId)).thenReturn(Mono.just(testBook));

        // When & Then
        StepVerifier.create(borrowingService.borrowBook(borrowBookRequest))
                .expectNextMatches(dto -> {
                    LocalDateTime expectedDueDate = dto.getBorrowedAt().plusDays(14);
                    return dto.getDueDate().toLocalDate().equals(expectedDueDate.toLocalDate());
                })
                .verifyComplete();
    }
}