package com.exam.library.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnBookRequest {
    @NotNull(message = "Borrower ID is required")
    private UUID borrowerId;
    
    @NotNull(message = "Book ID is required")
    private UUID bookId;
}