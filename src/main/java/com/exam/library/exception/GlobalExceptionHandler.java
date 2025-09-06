package com.exam.library.exception;

import com.exam.library.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(BorrowerNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBorrowerNotFoundException(BorrowerNotFoundException ex) {
        log.error("Borrower not found: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBookNotFoundException(BookNotFoundException ex) {
        log.error("Book not found: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        log.error("Email already exists: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(BookAlreadyBorrowedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBookAlreadyBorrowedException(BookAlreadyBorrowedException ex) {
        log.error("Book already borrowed: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(BookNotBorrowedException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleBookNotBorrowedException(BookNotBorrowedException ex) {
        log.error("Book not borrowed: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(UnauthorizedBorrowerException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleUnauthorizedBorrowerException(UnauthorizedBorrowerException ex) {
        log.error("Unauthorized borrower: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(InvalidIsbnException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleInvalidIsbnException(InvalidIsbnException ex) {
        log.error("Invalid ISBN: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> handleValidationExceptions(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Method argument validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = "Data integrity violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique_isbn_title_author")) {
                message = "Books with the same ISBN must have the same title and author";
            } else if (ex.getMessage().contains("unique_active_book_borrowing")) {
                message = "This book is already borrowed by someone else";
            } else if (ex.getMessage().contains("email")) {
                message = "Email address already exists";
            }
        }
        
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message)));
    }

    @ExceptionHandler(IllegalStateException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later.")));
    }
}