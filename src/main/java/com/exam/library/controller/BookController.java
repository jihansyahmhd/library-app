package com.exam.library.controller;


import com.exam.library.dto.request.BookDto;
import com.exam.library.dto.request.CreateBookRequest;
import com.exam.library.dto.response.ApiResponse;
import com.exam.library.service.BookService;
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
@RequestMapping("books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing library books")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Register a new book", description = "Adds a new book to the library collection")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", 
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "Invalid input data or ISBN constraint violation"
            )
    })
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<BookDto>>> createBook(
            @Valid @RequestBody CreateBookRequest request) {
        log.info("REST request to create book with ISBN: {} and title: {}", 
                request.getIsbn(), request.getTitle());
        
        return bookService.createBook(request)
                .map(book -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Book created successfully", book)))
                .doOnSuccess(response -> log.info("Successfully created book via REST API"))
                .doOnError(error -> log.error("Failed to create book via REST API: {}", error.getMessage()));
    }

    @Operation(summary = "Get all books", description = "Retrieves a list of all books in the library")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Books retrieved successfully"
            )
    })
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<BookDto>>>> getAllBooks() {
        log.info("REST request to get all books");
        return bookService.getAllBooks()
                .collectList()
                .map(list->ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", list)))
                .doOnSuccess(response -> log.info("Successfully retrieved books list"));
    }

    @Operation(summary = "Get available books", description = "Retrieves a list of all available (not borrowed) books")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Available books retrieved successfully"
            )
    })
    @GetMapping("/available")
    public Mono<ResponseEntity<ApiResponse<List<BookDto>>>> getAvailableBooks() {
        log.info("REST request to get available books");

        return bookService.getAvailableBooks()
                .collectList()
                .map(list -> ResponseEntity.ok(
                        ApiResponse.success("Available books retrieved successfully", list)))
                .doOnSuccess(response -> log.info("Successfully retrieved available books via REST API"));
    }

    @Operation(summary = "Get book by ID", description = "Retrieves a specific book by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Book found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Book not found"
            )
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BookDto>>> getBookById(
            @Parameter(description = "Book ID") @PathVariable UUID id) {
        log.info("REST request to get book with ID: {}", id);
        
        return bookService.getBookById(id)
                .map(book -> ResponseEntity.ok(ApiResponse.success("Book found", book)))
                .doOnSuccess(response -> log.info("Successfully retrieved book {} via REST API", id))
                .doOnError(error -> log.error("Failed to retrieve book {} via REST API: {}", id, error.getMessage()));
    }

    @Operation(summary = "Get books by ISBN", description = "Retrieves all books with a specific ISBN")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Books retrieved successfully"
            )
    })
    @GetMapping("/isbn/{isbn}")
    public Mono<ResponseEntity<ApiResponse<List<BookDto>>>> getBooksByIsbn(
            @Parameter(description = "Book ISBN") @PathVariable String isbn) {
        log.info("REST request to get books with ISBN: {}", isbn);

        return bookService.getBooksByIsbn(isbn)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", list)))
                .doOnSuccess(response -> log.info("Successfully retrieved books with ISBN {} via REST API", isbn));
    }

    @Operation(summary = "Search books by title", description = "Search for books by title (case-insensitive)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Books retrieved successfully"
            )
    })
    @GetMapping("/search/title")
    public Mono<ResponseEntity<ApiResponse<List<BookDto>>>> searchBooksByTitle(
            @Parameter(description = "Title to search for") @RequestParam String title) {
        log.info("REST request to search books by title: {}", title);
        return bookService.searchBooksByTitle(title)
                .collectList()
                .map(list->ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", list)))
                .doOnSuccess(response -> log.info("Successfully searched books by title {} via REST API", title));

    }

    @Operation(summary = "Search books by author", description = "Search for books by author name (case-insensitive)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Books retrieved successfully"
            )
    })
    @GetMapping("/search/author")
    public Mono<ResponseEntity<ApiResponse<Flux<BookDto>>>> searchBooksByAuthor(
            @Parameter(description = "Author to search for") @RequestParam String author) {
        log.info("REST request to search books by author: {}", author);
        
        Flux<BookDto> books = bookService.searchBooksByAuthor(author);
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books)))
                .doOnSuccess(response -> log.info("Successfully searched books by author {} via REST API", author));
    }

//    @Operation(summary = "Update book", description = "Updates an existing book's information")
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "Book updated successfully"
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "404",
//                    description = "Book not found"
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "400",
//                    description = "Invalid input data or ISBN constraint violation"
//            )
//    })
//    @PutMapping("/{id}")
//    public Mono<ResponseEntity<ApiResponse<BookDto>>> updateBook(
//            @Parameter(description = "Book ID") @PathVariable UUID id,
//            @Valid @RequestBody CreateBookRequest request) {
//        log.info("REST request to update book with ID: {}", id);
//
//        return bookService.updateBook(id, request)
//                .map(book -> ResponseEntity.ok(ApiResponse.success("Book updated successfully", book)))
//                .doOnSuccess(response -> log.info("Successfully updated book {} via REST API", id))
//                .doOnError(error -> log.error("Failed to update book {} via REST API: {}", id, error.getMessage()));
//    }

    @Operation(summary = "Delete book", description = "Deletes a book from the library collection")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204", 
                    description = "Book deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Book not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", 
                    description = "Cannot delete book that is currently borrowed"
            )
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteBook(
            @Parameter(description = "Book ID") @PathVariable UUID id) {
        log.info("REST request to delete book with ID: {}", id);
        
        return bookService.deleteBook(id)
                .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.<Void>success("Book deleted successfully", null))))
                .doOnSuccess(response -> log.info("Successfully deleted book {} via REST API", id))
                .doOnError(error -> log.error("Failed to delete book {} via REST API: {}", id, error.getMessage()));
    }

    @Operation(summary = "Check book availability", description = "Checks if a book is available for borrowing")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", 
                    description = "Book availability status retrieved"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", 
                    description = "Book not found"
            )
    })
    @GetMapping("/{id}/availability")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> checkBookAvailability(
            @Parameter(description = "Book ID") @PathVariable UUID id) {
        log.info("REST request to check availability of book with ID: {}", id);
        
        return bookService.isBookAvailable(id)
                .map(isAvailable -> ResponseEntity.ok(
                        ApiResponse.success("Book availability status retrieved", isAvailable)))
                .doOnSuccess(response -> log.info("Successfully checked availability of book {} via REST API", id))
                .doOnError(error -> log.error("Failed to check availability of book {} via REST API: {}", id, error.getMessage()));
    }
}