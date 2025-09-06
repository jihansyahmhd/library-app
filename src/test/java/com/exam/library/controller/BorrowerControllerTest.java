package com.exam.library.controller;

import com.exam.library.dto.request.BookDto;
import com.exam.library.dto.request.BorrowerDto;
import com.exam.library.dto.request.CreateBookRequest;
import com.exam.library.dto.request.CreateBorrowerRequest;
import com.exam.library.dto.response.ApiResponse;
import com.exam.library.service.BookService;
import com.exam.library.service.BorrowerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Fixed unit tests for BookController and BorrowerController.
 *
 * These tests assert the ApiResponse structure produced by the controllers:
 * - success (boolean)
 * - message (String)
 * - data (T)
 * - timestamp (LocalDateTime)
 */
@ExtendWith(MockitoExtension.class)
class ControllerUnitTests {

    // ---------------------- BookController tests ----------------------
    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @Test
    void book_createBook_shouldReturnCreatedResponse_withApiResponseFields() {
        when(bookService.createBook(any(CreateBookRequest.class))).thenReturn(Mono.just(new BookDto()));

        Mono<ResponseEntity<ApiResponse<BookDto>>> result = bookController.createBook(new CreateBookRequest());

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(201);
                    ApiResponse<BookDto> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getMessage()).containsIgnoringCase("created");
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void book_getAllBooks_shouldReturnList_inApiResponse() {
        BookDto dto = new BookDto();
        when(bookService.getAllBooks()).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<List<BookDto>>>> result = bookController.getAllBooks();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<List<BookDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).hasSize(1);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void book_getBookById_whenNotFound_shouldReturnEmptyMono() {
        UUID id = UUID.randomUUID();
        when(bookService.getBookById(id)).thenReturn(Mono.empty());

        StepVerifier.create(bookController.getBookById(id))
                .expectComplete()
                .verify();
    }

    // ---------------------- BorrowerController tests ----------------------
    @Mock
    private BorrowerService borrowerService;

    @InjectMocks
    private BorrowerController borrowerController;

    @Test
    void borrower_createBorrower_shouldReturnCreatedResponse_withApiResponseFields() {
        when(borrowerService.createBorrower(any(CreateBorrowerRequest.class))).thenReturn(Mono.just(new BorrowerDto()));

        Mono<ResponseEntity<ApiResponse<BorrowerDto>>> result = borrowerController.createBorrower(new CreateBorrowerRequest());

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(201);
                    ApiResponse<BorrowerDto> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getMessage()).containsIgnoringCase("created");
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void borrower_getAllBorrowers_shouldReturnList_inApiResponse() {
        BorrowerDto dto = new BorrowerDto();
        when(borrowerService.getAllBorrowers()).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<List<BorrowerDto>>>> result = borrowerController.getAllBorrowers();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<List<BorrowerDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).hasSize(1);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void borrower_getBorrowerById_whenFound_shouldReturnOk_andApiResponseWithData() {
        UUID id = UUID.randomUUID();
        BorrowerDto dto = new BorrowerDto();
        when(borrowerService.getBorrowerById(id)).thenReturn(Mono.just(dto));

        Mono<ResponseEntity<ApiResponse<BorrowerDto>>> result = borrowerController.getBorrowerById(id);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<BorrowerDto> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void borrower_getBorrowerById_whenNotFound_shouldReturnEmptyMono() {
        UUID id = UUID.randomUUID();
        when(borrowerService.getBorrowerById(id)).thenReturn(Mono.empty());

        StepVerifier.create(borrowerController.getBorrowerById(id))
                .expectComplete()
                .verify();
    }

    @Test
    void borrower_getBorrowerByEmail_whenFound_shouldReturnOk_andApiResponseWithData() {
        String email = "test@example.com";
        BorrowerDto dto = new BorrowerDto();
        when(borrowerService.getBorrowerByEmail(email)).thenReturn(Mono.just(dto));

        Mono<ResponseEntity<ApiResponse<BorrowerDto>>> result = borrowerController.getBorrowerByEmail(email);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<BorrowerDto> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void borrower_updateBorrower_shouldReturnOk_andApiResponseWithData() {
        UUID id = UUID.randomUUID();
        CreateBorrowerRequest req = new CreateBorrowerRequest();
        BorrowerDto dto = new BorrowerDto();
        when(borrowerService.updateBorrower(eq(id), any(CreateBorrowerRequest.class))).thenReturn(Mono.just(dto));

        Mono<ResponseEntity<ApiResponse<BorrowerDto>>> result = borrowerController.updateBorrower(id, req);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<BorrowerDto> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void borrower_deleteBorrower_shouldReturnNoContent_andApiResponseSuccess() {
        UUID id = UUID.randomUUID();
        when(borrowerService.deleteBorrower(id)).thenReturn(Mono.empty());

        Mono<ResponseEntity<ApiResponse<Void>>> result = borrowerController.deleteBorrower(id);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(204);
                    ApiResponse<Void> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    // data is expected to be null for delete
                    assertThat(body.getData()).isNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }
}
