package com.exam.library.controller;

import com.exam.library.dto.request.BookDto;
import com.exam.library.dto.request.CreateBookRequest;
import com.exam.library.dto.response.ApiResponse;
import com.exam.library.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private WebTestClient webTestClient;
    private ObjectMapper objectMapper;

    private BookDto sampleBookDto;
    private CreateBookRequest sampleCreateRequest;
    private UUID sampleBookId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(bookController).build();
        objectMapper = new ObjectMapper();

        sampleBookId = UUID.randomUUID();

        sampleBookDto = BookDto.builder()
                .id(sampleBookId)
                .isbn("9781234567890")
                .title("Spring Boot Guide")
                .author("Tech Author")
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleCreateRequest = CreateBookRequest.builder()
                .isbn("9781234567890")
                .title("Spring Boot Guide")
                .author("Tech Author")
                .build();
    }

    @Test
    void createBook_ShouldReturnCreated_WhenValidRequest() {
        // Given
        when(bookService.createBook(any(CreateBookRequest.class)))
                .thenReturn(Mono.just(sampleBookDto));

        // When & Then
        webTestClient.post()
                .uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleCreateRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Book created successfully")
                .jsonPath("$.data.id").isEqualTo(sampleBookId.toString())
                .jsonPath("$.data.isbn").isEqualTo("9781234567890")
                .jsonPath("$.data.title").isEqualTo("Spring Boot Guide")
                .jsonPath("$.data.author").isEqualTo("Tech Author")
                .jsonPath("$.data.available").isEqualTo(true);

        verify(bookService).createBook(argThat(request ->
                request.getIsbn().equals("9781234567890") &&
                        request.getTitle().equals("Spring Boot Guide") &&
                        request.getAuthor().equals("Tech Author")
        ));
    }

    @Test
    void createBook_ShouldReturnBadRequest_WhenInvalidRequest() {
        // Given
        CreateBookRequest invalidRequest = CreateBookRequest.builder()
                .isbn("") // Invalid empty ISBN
                .title("")
                .author("")
                .build();

        // When & Then
        webTestClient.post()
                .uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(bookService, never()).createBook(any());
    }

    @Test
    void createBook_ShouldHandleServiceError() {
        // Given
        when(bookService.createBook(any(CreateBookRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        // When & Then
        webTestClient.post()
                .uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sampleCreateRequest)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(bookService).createBook(any(CreateBookRequest.class));
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        // Given
        BookDto book1 = sampleBookDto;
        BookDto book2 = BookDto.builder()
                .id(UUID.randomUUID())
                .isbn("9780987654321")
                .title("Advanced Java")
                .author("Java Expert")
                .isAvailable(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(bookService.getAllBooks())
                .thenReturn(Flux.just(book1, book2));

        // When & Then
        webTestClient.get()
                .uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Books retrieved successfully")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].id").isEqualTo(book1.getId().toString())
                .jsonPath("$.data[0].isbn").isEqualTo("9781234567890")
                .jsonPath("$.data[1].id").isEqualTo(book2.getId().toString())
                .jsonPath("$.data[1].isbn").isEqualTo("9780987654321");

        verify(bookService).getAllBooks();
    }

    @Test
    void getAllBooks_ShouldReturnEmptyList_WhenNoBooksExist() {
        // Given
        when(bookService.getAllBooks()).thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Books retrieved successfully")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(0);

        verify(bookService).getAllBooks();
    }

    @Test
    void getAvailableBooks_ShouldReturnOnlyAvailableBooks() {
        // Given
        BookDto availableBook1 = sampleBookDto;
        BookDto availableBook2 = BookDto.builder()
                .id(UUID.randomUUID())
                .isbn("9780987654321")
                .title("Available Book")
                .author("Author")
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(bookService.getAvailableBooks())
                .thenReturn(Flux.just(availableBook1, availableBook2));

        // When & Then
        webTestClient.get()
                .uri("/books/available")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Available books retrieved successfully")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].available").isEqualTo(true)
                .jsonPath("$.data[1].available").isEqualTo(true);

        verify(bookService).getAvailableBooks();
    }

    @Test
    void getBookById_ShouldReturnBook_WhenBookExists() {
        // Given
        when(bookService.getBookById(sampleBookId))
                .thenReturn(Mono.just(sampleBookDto));

        // When & Then
        webTestClient.get()
                .uri("/books/{id}", sampleBookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Book found")
                .jsonPath("$.data.id").isEqualTo(sampleBookId.toString())
                .jsonPath("$.data.isbn").isEqualTo("9781234567890")
                .jsonPath("$.data.title").isEqualTo("Spring Boot Guide");

        verify(bookService).getBookById(sampleBookId);
    }

    @Test
    void getBookById_ShouldReturnNotFound_WhenBookDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(bookService.getBookById(nonExistentId))
                .thenReturn(Mono.error(new RuntimeException("Book not found")));

        // When & Then
        webTestClient.get()
                .uri("/books/{id}", nonExistentId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(bookService).getBookById(nonExistentId);
    }

    @Test
    void getBooksByIsbn_ShouldReturnBooksWithSameIsbn() {
        // Given
        String isbn = "9781234567890";
        BookDto book1 = sampleBookDto;
        BookDto book2 = BookDto.builder()
                .id(UUID.randomUUID())
                .isbn(isbn)
                .title("Same ISBN Different Copy")
                .author("Tech Author")
                .isAvailable(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(bookService.getBooksByIsbn(isbn))
                .thenReturn(Flux.just(book1, book2));

        // When & Then
        webTestClient.get()
                .uri("/books/isbn/{isbn}", isbn)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Books retrieved successfully")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].isbn").isEqualTo(isbn)
                .jsonPath("$.data[1].isbn").isEqualTo(isbn);

        verify(bookService).getBooksByIsbn(isbn);
    }

    @Test
    void searchBooksByTitle_ShouldReturnMatchingBooks() {
        // Given
        String titleQuery = "Spring";
        BookDto matchingBook1 = sampleBookDto;
        BookDto matchingBook2 = BookDto.builder()
                .id(UUID.randomUUID())
                .isbn("9780987654321")
                .title("Spring Security Guide")
                .author("Security Expert")
                .isAvailable(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(bookService.searchBooksByTitle(titleQuery))
                .thenReturn(Flux.just(matchingBook1, matchingBook2));

        // When & Then
        webTestClient.get()
                .uri("/books/search/title?title={title}", titleQuery)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Books retrieved successfully")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(2);

        verify(bookService).searchBooksByTitle(titleQuery);
    }

    @Test
    void searchBooksByTitle_ShouldReturnEmptyList_WhenNoMatch() {
        // Given
        String titleQuery = "NonExistentTitle";
        when(bookService.searchBooksByTitle(titleQuery))
                .thenReturn(Flux.empty());

        // When & Then
        webTestClient.get()
                .uri("/books/search/title?title={title}", titleQuery)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Books retrieved successfully")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.length()").isEqualTo(0);

        verify(bookService).searchBooksByTitle(titleQuery);
    }

    @Test
    void searchBooksByAuthor_ShouldReturnMatchingBooks() {
        // Given
        String authorQuery = "Tech";
        when(bookService.searchBooksByAuthor(authorQuery))
                .thenReturn(Flux.just(sampleBookDto));

        // When & Then
        webTestClient.get()
                .uri("/books/search/author?author={author}", authorQuery)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Books retrieved successfully");

        verify(bookService).searchBooksByAuthor(authorQuery);
    }

    @Test
    void deleteBook_ShouldReturnNoContent_WhenBookDeleted() {
        // Given
        when(bookService.deleteBook(sampleBookId))
                .thenReturn(Mono.empty());

        // When & Then
        webTestClient.delete()
                .uri("/books/{id}", sampleBookId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Book deleted successfully")
                .jsonPath("$.data").isEmpty();

        verify(bookService).deleteBook(sampleBookId);
    }

    @Test
    void deleteBook_ShouldReturnError_WhenBookCannotBeDeleted() {
        // Given
        when(bookService.deleteBook(sampleBookId))
                .thenReturn(Mono.error(new RuntimeException("Book is currently borrowed")));

        // When & Then
        webTestClient.delete()
                .uri("/books/{id}", sampleBookId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(bookService).deleteBook(sampleBookId);
    }

    @Test
    void checkBookAvailability_ShouldReturnTrue_WhenBookIsAvailable() {
        // Given
        when(bookService.isBookAvailable(sampleBookId))
                .thenReturn(Mono.just(true));

        // When & Then
        webTestClient.get()
                .uri("/books/{id}/availability", sampleBookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Book availability status retrieved")
                .jsonPath("$.data").isEqualTo(true);

        verify(bookService).isBookAvailable(sampleBookId);
    }

    @Test
    void checkBookAvailability_ShouldReturnFalse_WhenBookIsNotAvailable() {
        // Given
        when(bookService.isBookAvailable(sampleBookId))
                .thenReturn(Mono.just(false));

        // When & Then
        webTestClient.get()
                .uri("/books/{id}/availability", sampleBookId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Book availability status retrieved")
                .jsonPath("$.data").isEqualTo(false);

        verify(bookService).isBookAvailable(sampleBookId);
    }

    @Test
    void checkBookAvailability_ShouldReturnError_WhenBookNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(bookService.isBookAvailable(nonExistentId))
                .thenReturn(Mono.error(new RuntimeException("Book not found")));

        // When & Then
        webTestClient.get()
                .uri("/books/{id}/availability", nonExistentId)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(bookService).isBookAvailable(nonExistentId);
    }

    // Edge case tests
    @Test
    void createBook_ShouldHandleNullRequestBody() {
        // When & Then
        webTestClient.post()
                .uri("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(bookService, never()).createBook(any());
    }

    @Test
    void searchBooksByTitle_ShouldHandleMissingParameter() {
        // When & Then
        webTestClient.get()
                .uri("/books/search/title")
                .exchange()
                .expectStatus().isBadRequest();

        verify(bookService, never()).searchBooksByTitle(any());
    }

    @Test
    void searchBooksByAuthor_ShouldHandleMissingParameter() {
        // When & Then
        webTestClient.get()
                .uri("/books/search/author")
                .exchange()
                .expectStatus().isBadRequest();

        verify(bookService, never()).searchBooksByAuthor(any());
    }

    @Test
    void getBookById_ShouldHandleInvalidUUID() {
        // When & Then
        webTestClient.get()
                .uri("/books/invalid-uuid")
                .exchange()
                .expectStatus().isBadRequest();

        verify(bookService, never()).getBookById(any());
    }

    @Test
    void deleteBook_ShouldHandleInvalidUUID() {
        // When & Then
        webTestClient.delete()
                .uri("/books/invalid-uuid")
                .exchange()
                .expectStatus().isBadRequest();

        verify(bookService, never()).deleteBook(any());
    }

    @Test
    void checkBookAvailability_ShouldHandleInvalidUUID() {
        // When & Then
        webTestClient.get()
                .uri("/books/invalid-uuid/availability")
                .exchange()
                .expectStatus().isBadRequest();

        verify(bookService, never()).isBookAvailable(any());
    }
}