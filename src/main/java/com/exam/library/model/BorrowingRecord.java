package com.exam.library.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("borrowing_records")
public class BorrowingRecord {

    @Id
    private UUID id;

    @NotNull(message = "Borrower ID is required")
    @Column("borrower_id")
    private UUID borrowerId;

    @NotNull(message = "Book ID is required")
    @Column("book_id")
    private UUID bookId;

    @Column("borrowed_at")
    private LocalDateTime borrowedAt;

    @Column("returned_at")
    private LocalDateTime returnedAt;

    @Column("due_date")
    private LocalDateTime dueDate;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
