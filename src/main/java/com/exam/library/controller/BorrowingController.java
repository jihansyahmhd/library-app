package com.exam.library.controller;

import com.exam.library.dto.request.BorrowBookRequest;
import com.exam.library.dto.request.BorrowingRecordDto;
import com.exam.library.dto.request.ReturnBookRequest;
import com.exam.library.dto.response.ApiResponse;
import com.exam.library.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/borrowing")
@RequiredArgsConstructor
@Tag(name = "Borrowing Management", description = "APIs for managing book borrowing and returning")
public class BorrowingController {

    private final BorrowingService borrowingService;

    @Operation(summary = "Borrow a book", description = "Allows a borrower to borrow a specific book")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", 
                    description = "Book borrowed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "Invalid request data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Borrower or book not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", 
                    description = "Book is already borrowed"
            )
    })
    @PostMapping("/borrow")
    public Mono<ResponseEntity<ApiResponse<BorrowingRecordDto>>> borrowBook(
            @Valid @RequestBody BorrowBookRequest request) {
        log.info("REST request to borrow book {} by borrower {}", 
                request.getBookId(), request.getBorrowerId());
        
        return borrowingService.borrowBook(request)
                .map(record -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Book borrowed successfully", record)))
                .doOnSuccess(response -> log.info("Successfully borrowed book via REST API"))
                .doOnError(error -> log.error("Failed to borrow book via REST API: {}", error.getMessage()));
    }

    @Operation(summary = "Return a book", description = "Allows a borrower to return a borrowed book")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Book returned successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "Book is not currently borrowed"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", 
                    description = "Book was not borrowed by the specified borrower"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Borrower or book not found"
            )
    })
    @PostMapping("/return")
    public Mono<ResponseEntity<ApiResponse<BorrowingRecordDto>>> returnBook(
            @Valid @RequestBody ReturnBookRequest request) {
        log.info("REST request to return book {} by borrower {}", 
                request.getBookId(), request.getBorrowerId());
        
        return borrowingService.returnBook(request)
                .map(record -> ResponseEntity.ok(ApiResponse.success("Book returned successfully", record)))
                .doOnSuccess(response -> log.info("Successfully returned book via REST API"))
                .doOnError(error -> log.error("Failed to return book via REST API: {}", error.getMessage()));
    }

    @Operation(summary = "Get all borrowing records", description = "Retrieves all borrowing records in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Borrowing records retrieved successfully"
            )
    })
    @GetMapping("/records")
    public Mono<ResponseEntity<ApiResponse<List<BorrowingRecordDto>>>> getAllBorrowingRecords() {
        log.info("REST request to get all borrowing records");
        
        return  borrowingService.getAllBorrowingRecords()
                .collectList()
                .map(list->ResponseEntity.ok(ApiResponse.success("Borrowing records retrieved successfully", list)))
                .doOnSuccess(response -> log.info("Successfully retrieved all borrowing records via REST API"));
//        return Mono.just(ResponseEntity.ok(ApiResponse.success("Borrowing records retrieved successfully", records)))
//                .doOnSuccess(response -> log.info("Successfully retrieved all borrowing records via REST API"));
    }

    @Operation(summary = "Get active borrowings", description = "Retrieves all currently active borrowing records")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Active borrowing records retrieved successfully"
            )
    })
    @GetMapping("/records/active")
    public Mono<ResponseEntity<ApiResponse<List<BorrowingRecordDto>>>> getActiveBorrowings() {
        log.info("REST request to get active borrowing records");
        
        return borrowingService.getActiveBorrowings()
                .collectList()
                .map(list->ResponseEntity.ok(ApiResponse.success("Active borrowing records retrieved successfully", list)))
                .doOnSuccess(response -> log.info("Successfully retrieved active borrowing records via REST API"));

    }

    @Operation(summary = "Get overdue borrowings", description = "Retrieves all overdue borrowing records")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Overdue borrowing records retrieved successfully"
            )
    })
    @GetMapping("/records/overdue")
    public Mono<ResponseEntity<ApiResponse<List<BorrowingRecordDto>>>> getOverdueBorrowings() {
        log.info("REST request to get overdue borrowing records");
        
        return borrowingService.getOverdueBorrowings()
                .collectList()
                .map(records->ResponseEntity.ok(ApiResponse.success("Overdue borrowing records retrieved successfully", records)))
                .doOnSuccess(response -> log.info("Successfully retrieved overdue borrowing records via REST API"));
    }

    @Operation(summary = "Get borrowing records by borrower", description = "Retrieves all borrowing records for a specific borrower")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Borrowing records retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Borrower not found"
            )
    })
    @GetMapping("/records/borrower/{borrowerId}")
    public Mono<ResponseEntity<ApiResponse<Flux<BorrowingRecordDto>>>> getBorrowingsByBorrower(
            @Parameter(description = "Borrower ID") @PathVariable UUID borrowerId) {
        log.info("REST request to get borrowing records for borrower: {}", borrowerId);
        
        Flux<BorrowingRecordDto> records = borrowingService.getBorrowingsByBorrower(borrowerId);
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Borrowing records retrieved successfully", records)))
                .doOnSuccess(response -> log.info("Successfully retrieved borrowing records for borrower {} via REST API", borrowerId));
    }

    @Operation(summary = "Get active borrowings by borrower", description = "Retrieves all active borrowing records for a specific borrower")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Active borrowing records retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Borrower not found"
            )
    })
    @GetMapping("/records/borrower/{borrowerId}/active")
    public Mono<ResponseEntity<ApiResponse<Flux<BorrowingRecordDto>>>> getActiveBorrowingsByBorrower(
            @Parameter(description = "Borrower ID") @PathVariable UUID borrowerId) {
        log.info("REST request to get active borrowing records for borrower: {}", borrowerId);
        
        Flux<BorrowingRecordDto> records = borrowingService.getActiveBorrowingsByBorrower(borrowerId);
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Active borrowing records retrieved successfully", records)))
                .doOnSuccess(response -> log.info("Successfully retrieved active borrowing records for borrower {} via REST API", borrowerId));
    }

    @Operation(summary = "Get borrowing records by book", description = "Retrieves all borrowing records for a specific book")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Borrowing records retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Book not found"
            )
    })
    @GetMapping("/records/book/{bookId}")
    public Mono<ResponseEntity<ApiResponse<Flux<BorrowingRecordDto>>>> getBorrowingsByBook(
            @Parameter(description = "Book ID") @PathVariable UUID bookId) {
        log.info("REST request to get borrowing records for book: {}", bookId);
        
        Flux<BorrowingRecordDto> records = borrowingService.getBorrowingsByBook(bookId);
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Borrowing records retrieved successfully", records)))
                .doOnSuccess(response -> log.info("Successfully retrieved borrowing records for book {} via REST API", bookId));
    }
}