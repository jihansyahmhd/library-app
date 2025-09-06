package com.exam.library.service;

import com.exam.library.dto.request.BorrowBookRequest;
import com.exam.library.dto.request.BorrowingRecordDto;
import com.exam.library.dto.request.ReturnBookRequest;
import com.exam.library.exception.*;
import com.exam.library.model.BorrowingRecord;
import com.exam.library.repository.BookRepository;
import com.exam.library.repository.BorrowerRepository;
import com.exam.library.repository.BorrowingRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BorrowerRepository borrowerRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Mono<BorrowingRecordDto> borrowBook(BorrowBookRequest request) {
        log.info("Processing borrow request for borrower {} and book {}", 
                request.getBorrowerId(), request.getBookId());

        return validateBorrowRequest(request)
                .then(Mono.defer(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    BorrowingRecord record = BorrowingRecord.builder()
                            .borrowerId(request.getBorrowerId())
                            .bookId(request.getBookId())
                            .borrowedAt(now)
                            .dueDate(now.plusDays(14)) // 2 weeks borrowing period
                            .build();

                    return borrowingRecordRepository.save(record);
                }))
                .flatMap(this::mapToDtoWithDetails)
                .doOnSuccess(dto -> log.info("Successfully borrowed book. Record ID: {}", dto.getId()))
                .doOnError(error -> log.error("Failed to borrow book: {}", error.getMessage()));
    }

    @Transactional
    public Mono<BorrowingRecordDto> returnBook(ReturnBookRequest request) {
        log.info("Processing return request for borrower {} and book {}", 
                request.getBorrowerId(), request.getBookId());

        return borrowingRecordRepository.findActiveBorrowingByBookId(request.getBookId())
                .switchIfEmpty(Mono.error(new BookNotBorrowedException(
                        "Book with ID " + request.getBookId() + " is not currently borrowed")))
                .flatMap(record -> {
                    if (!record.getBorrowerId().equals(request.getBorrowerId())) {
                        return Mono.error(new UnauthorizedBorrowerException(
                                "Book was not borrowed by the specified borrower"));
                    }
                    
                    record.setReturnedAt(LocalDateTime.now());
                    return borrowingRecordRepository.save(record);
                })
                .flatMap(this::mapToDtoWithDetails)
                .doOnSuccess(dto -> log.info("Successfully returned book. Record ID: {}", dto.getId()))
                .doOnError(error -> log.error("Failed to return book: {}", error.getMessage()));
    }

    @Transactional(readOnly = true)
    public Flux<BorrowingRecordDto> getAllBorrowingRecords() {
        log.info("Retrieving all borrowing records");
        return borrowingRecordRepository.findAll()
                .flatMap(this::mapToDtoWithDetails)
                .doOnComplete(() -> log.info("Successfully retrieved all borrowing records"));
    }

    @Transactional(readOnly = true)
    public Flux<BorrowingRecordDto> getActiveBorrowings() {
        log.info("Retrieving active borrowing records");
        return borrowingRecordRepository.findAllActiveBorrowings()
                .flatMap(this::mapToDtoWithDetails)
                .doOnComplete(() -> log.info("Successfully retrieved active borrowing records"));
    }

    @Transactional(readOnly = true)
    public Flux<BorrowingRecordDto> getBorrowingsByBorrower(UUID borrowerId) {
        log.info("Retrieving borrowing records for borrower: {}", borrowerId);
        
        return borrowerRepository.existsById(borrowerId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BorrowerNotFoundException("Borrower not found with ID: " + borrowerId));
                    }
                    return borrowingRecordRepository.findByBorrowerId(borrowerId).collectList();
                })
                .flatMapMany(records -> Flux.fromIterable(records))
                .flatMap(this::mapToDtoWithDetails)
                .doOnComplete(() -> log.info("Successfully retrieved borrowing records for borrower: {}", borrowerId));
    }

    @Transactional(readOnly = true)
    public Flux<BorrowingRecordDto> getActiveBorrowingsByBorrower(UUID borrowerId) {
        log.info("Retrieving active borrowing records for borrower: {}", borrowerId);
        
        return borrowerRepository.existsById(borrowerId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BorrowerNotFoundException("Borrower not found with ID: " + borrowerId));
                    }
                    return borrowingRecordRepository.findActiveBorrowingsByBorrowerId(borrowerId).collectList();
                })
                .flatMapMany(records -> Flux.fromIterable(records))
                .flatMap(this::mapToDtoWithDetails)
                .doOnComplete(() -> log.info("Successfully retrieved active borrowing records for borrower: {}", borrowerId));
    }

    @Transactional(readOnly = true)
    public Flux<BorrowingRecordDto> getBorrowingsByBook(UUID bookId) {
        log.info("Retrieving borrowing records for book: {}", bookId);
        
        return bookRepository.existsById(bookId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BookNotFoundException("Book not found with ID: " + bookId));
                    }
                    return borrowingRecordRepository.findByBookId(bookId).collectList();
                })
                .flatMapMany(records -> Flux.fromIterable(records))
                .flatMap(this::mapToDtoWithDetails)
                .doOnComplete(() -> log.info("Successfully retrieved borrowing records for book: {}", bookId));
    }

    @Transactional(readOnly = true)
    public Flux<BorrowingRecordDto> getOverdueBorrowings() {
        log.info("Retrieving overdue borrowing records");
        return borrowingRecordRepository.findOverdueBorrowings()
                .flatMap(this::mapToDtoWithDetails)
                .doOnComplete(() -> log.info("Successfully retrieved overdue borrowing records"));
    }

    private Mono<Void> validateBorrowRequest(BorrowBookRequest request) {
        return borrowerRepository.existsById(request.getBorrowerId())
                .flatMap(borrowerExists -> {
                    if (!borrowerExists) {
                        return Mono.error(new BorrowerNotFoundException(
                                "Borrower not found with ID: " + request.getBorrowerId()));
                    }
                    return bookRepository.existsById(request.getBookId());
                })
                .flatMap(bookExists -> {
                    if (!bookExists) {
                        return Mono.error(new BookNotFoundException(
                                "Book not found with ID: " + request.getBookId()));
                    }
                    return borrowingRecordRepository.findActiveBorrowingByBookId(request.getBookId());
                })
                .flatMap(activeBorrowing -> {
                    if (activeBorrowing != null) {
                        return Mono.error(new BookAlreadyBorrowedException(
                                "Book with ID " + request.getBookId() + " is already borrowed"));
                    }
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.empty())
                .then();
    }

    private Mono<BorrowingRecordDto> mapToDtoWithDetails(BorrowingRecord record) {
        Mono<String> borrowerNameMono = borrowerRepository.findById(record.getBorrowerId())
                .map(borrower -> borrower.getName())
                .defaultIfEmpty("Unknown");
                
        Mono<String> borrowerEmailMono = borrowerRepository.findById(record.getBorrowerId())
                .map(borrower -> borrower.getEmail())
                .defaultIfEmpty("Unknown");

        Mono<String> bookTitleMono = bookRepository.findById(record.getBookId())
                .map(book -> book.getTitle())
                .defaultIfEmpty("Unknown");
                
        Mono<String> bookAuthorMono = bookRepository.findById(record.getBookId())
                .map(book -> book.getAuthor())
                .defaultIfEmpty("Unknown");
                
        Mono<String> bookIsbnMono = bookRepository.findById(record.getBookId())
                .map(book -> book.getIsbn())
                .defaultIfEmpty("Unknown");

        return Mono.zip(borrowerNameMono, borrowerEmailMono, bookTitleMono, bookAuthorMono, bookIsbnMono)
                .map(tuple -> BorrowingRecordDto.builder()
                        .id(record.getId())
                        .borrowerId(record.getBorrowerId())
                        .bookId(record.getBookId())
                        .borrowedAt(record.getBorrowedAt())
                        .returnedAt(record.getReturnedAt())
                        .dueDate(record.getDueDate())
                        .createdAt(record.getCreatedAt())
                        .updatedAt(record.getUpdatedAt())
                        .borrowerName(tuple.getT1())
                        .borrowerEmail(tuple.getT2())
                        .bookTitle(tuple.getT3())
                        .bookAuthor(tuple.getT4())
                        .bookIsbn(tuple.getT5())
                        .build());
    }
}