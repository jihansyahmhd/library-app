package com.exam.library.repository;

import com.exam.library.model.BorrowingRecord;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BorrowingRecordRepository extends R2dbcRepository<BorrowingRecord, UUID> {

    Flux<BorrowingRecord> findByBorrowerId(UUID borrowerId);

    Flux<BorrowingRecord> findByBookId(UUID bookId);

    @Query("SELECT * FROM borrowing_records WHERE borrower_id = :borrowerId AND returned_at IS NULL")
    Flux<BorrowingRecord> findActiveBorrowingsByBorrowerId(UUID borrowerId);

    @Query("SELECT * FROM borrowing_records WHERE book_id = :bookId AND returned_at IS NULL")
    Mono<BorrowingRecord> findActiveBorrowingByBookId(UUID bookId);

    @Query("SELECT * FROM borrowing_records WHERE returned_at IS NULL")
    Flux<BorrowingRecord> findAllActiveBorrowings();

    @Query("SELECT * FROM borrowing_records WHERE due_date < CURRENT_TIMESTAMP AND returned_at IS NULL")
    Flux<BorrowingRecord> findOverdueBorrowings();
}
