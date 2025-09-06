package com.exam.library.controller;

import com.exam.library.dto.request.BorrowBookRequest;
import com.exam.library.dto.request.BorrowingRecordDto;
import com.exam.library.dto.request.ReturnBookRequest;
import com.exam.library.dto.response.ApiResponse;
import com.exam.library.service.BorrowingService;
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

@ExtendWith(MockitoExtension.class)
class BorrowingControllerUnitTest {

    @Mock
    private BorrowingService borrowingService;

    @InjectMocks
    private BorrowingController borrowingController;

    @Test
    void borrowBook_shouldReturnCreated_andApiResponseWithData() {
        BorrowBookRequest req = new BorrowBookRequest();
        BorrowingRecordDto record = new BorrowingRecordDto();
        when(borrowingService.borrowBook(any(BorrowBookRequest.class))).thenReturn(Mono.just(record));

        Mono<ResponseEntity<ApiResponse<BorrowingRecordDto>>> result = borrowingController.borrowBook(req);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(201);
                    ApiResponse<BorrowingRecordDto> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getMessage()).containsIgnoringCase("borrowed");
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void returnBook_shouldReturnOk_andApiResponseWithData() {
        ReturnBookRequest req = new ReturnBookRequest();
        BorrowingRecordDto record = new BorrowingRecordDto();
        when(borrowingService.returnBook(any(ReturnBookRequest.class))).thenReturn(Mono.just(record));

        Mono<ResponseEntity<ApiResponse<BorrowingRecordDto>>> result = borrowingController.returnBook(req);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<BorrowingRecordDto> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getMessage()).containsIgnoringCase("returned");
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getAllBorrowingRecords_shouldReturnList_inApiResponse() {
        BorrowingRecordDto dto = new BorrowingRecordDto();
        when(borrowingService.getAllBorrowingRecords()).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<List<BorrowingRecordDto>>>> result = borrowingController.getAllBorrowingRecords();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<List<BorrowingRecordDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).hasSize(1);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getActiveBorrowings_shouldReturnList_inApiResponse() {
        BorrowingRecordDto dto = new BorrowingRecordDto();
        when(borrowingService.getActiveBorrowings()).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<List<BorrowingRecordDto>>>> result = borrowingController.getActiveBorrowings();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<List<BorrowingRecordDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).hasSize(1);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getOverdueBorrowings_shouldReturnList_inApiResponse() {
        BorrowingRecordDto dto = new BorrowingRecordDto();
        when(borrowingService.getOverdueBorrowings()).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<List<BorrowingRecordDto>>>> result = borrowingController.getOverdueBorrowings();

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<List<BorrowingRecordDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).hasSize(1);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getBorrowingsByBorrower_shouldReturnFluxWrappedInApiResponse() {
        UUID borrowerId = UUID.randomUUID();
        BorrowingRecordDto dto = new BorrowingRecordDto();
        when(borrowingService.getBorrowingsByBorrower(borrowerId)).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<Flux<BorrowingRecordDto>>>> result = borrowingController.getBorrowingsByBorrower(borrowerId);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<Flux<BorrowingRecordDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).isInstanceOf(Flux.class);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getActiveBorrowingsByBorrower_shouldReturnFluxWrappedInApiResponse() {
        UUID borrowerId = UUID.randomUUID();
        BorrowingRecordDto dto = new BorrowingRecordDto();
        when(borrowingService.getActiveBorrowingsByBorrower(borrowerId)).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<Flux<BorrowingRecordDto>>>> result = borrowingController.getActiveBorrowingsByBorrower(borrowerId);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<Flux<BorrowingRecordDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).isInstanceOf(Flux.class);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void getBorrowingsByBook_shouldReturnFluxWrappedInApiResponse() {
        UUID bookId = UUID.randomUUID();
        BorrowingRecordDto dto = new BorrowingRecordDto();
        when(borrowingService.getBorrowingsByBook(bookId)).thenReturn(Flux.just(dto));

        Mono<ResponseEntity<ApiResponse<Flux<BorrowingRecordDto>>>> result = borrowingController.getBorrowingsByBook(bookId);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getStatusCodeValue()).isEqualTo(200);
                    ApiResponse<Flux<BorrowingRecordDto>> body = response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.isSuccess()).isTrue();
                    assertThat(body.getData()).isNotNull();
                    assertThat(body.getData()).isInstanceOf(Flux.class);
                    assertThat(body.getTimestamp()).isNotNull();
                })
                .verifyComplete();
    }
}
