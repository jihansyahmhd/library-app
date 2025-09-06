package com.exam.library.controller;

import com.exam.library.dto.request.BorrowerDto;
import com.exam.library.dto.request.CreateBorrowerRequest;
import com.exam.library.dto.response.ApiResponse;
import com.exam.library.service.BorrowerService;
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
@RequestMapping("/borrowers")
@RequiredArgsConstructor
@Tag(name = "Borrower Management", description = "APIs for managing library borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @Operation(summary = "Register a new borrower", description = "Creates a new borrower in the library system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Borrower created successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists"
            )
    })
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<BorrowerDto>>> createBorrower(
            @Valid @RequestBody CreateBorrowerRequest request) {
        log.info("REST request to create borrower with email: {}", request.getEmail());

        return borrowerService.createBorrower(request)
                .map(borrower -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Borrower created successfully", borrower)))
                .doOnSuccess(response -> log.info("Successfully created borrower via REST API"))
                .doOnError(error -> log.error("Failed to create borrower via REST API: {}", error.getMessage()));
    }

    @Operation(summary = "Get all borrowers", description = "Retrieves a list of all borrowers in the library")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Borrowers retrieved successfully"
            )
    })
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<BorrowerDto>>>> getAllBorrowers() {
        log.info("REST request to get all borrowers");

        return borrowerService.getAllBorrowers()
                .collectList()
                .map(list->ResponseEntity.ok(ApiResponse.success("Borrowers retrieved successfully", list)))
                .doOnSuccess(response -> log.info("Successfully retrieved borrowers"));

    }

    @Operation(summary = "Get borrower by ID", description = "Retrieves a specific borrower by their ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Borrower found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Borrower not found"
            )
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BorrowerDto>>> getBorrowerById(
            @Parameter(description = "Borrower ID") @PathVariable UUID id) {
        log.info("REST request to get borrower with ID: {}", id);

        return borrowerService.getBorrowerById(id)
                .map(borrower -> ResponseEntity.ok(ApiResponse.success("Borrower found", borrower)))
                .doOnSuccess(response -> log.info("Successfully retrieved borrower {} via REST API", id))
                .doOnError(error -> log.error("Failed to retrieve borrower {} via REST API: {}", id, error.getMessage()));
    }

    @Operation(summary = "Get borrower by email", description = "Retrieves a specific borrower by their email address")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Borrower found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Borrower not found"
            )
    })
    @GetMapping("/email/{email}")
    public Mono<ResponseEntity<ApiResponse<BorrowerDto>>> getBorrowerByEmail(
            @Parameter(description = "Borrower email") @PathVariable String email) {
        log.info("REST request to get borrower with email: {}", email);

        return borrowerService.getBorrowerByEmail(email)
                .map(borrower -> ResponseEntity.ok(ApiResponse.success("Borrower found", borrower)))
                .doOnSuccess(response -> log.info("Successfully retrieved borrower {} via REST API", email))
                .doOnError(error -> log.error("Failed to retrieve borrower {} via REST API: {}", email, error.getMessage()));
    }

    @Operation(summary = "Update borrower", description = "Updates an existing borrower's information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Borrower updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Borrower not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists"
            )
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BorrowerDto>>> updateBorrower(
            @Parameter(description = "Borrower ID") @PathVariable UUID id,
            @Valid @RequestBody CreateBorrowerRequest request) {
        log.info("REST request to update borrower with ID: {}", id);

        return borrowerService.updateBorrower(id, request)
                .map(borrower -> ResponseEntity.ok(ApiResponse.success("Borrower updated successfully", borrower)))
                .doOnSuccess(response -> log.info("Successfully updated borrower {} via REST API", id))
                .doOnError(error -> log.error("Failed to update borrower {} via REST API: {}", id, error.getMessage()));
    }

    @Operation(summary = "Delete borrower", description = "Deletes a borrower from the library system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Borrower deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Borrower not found"
            )
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteBorrower(
            @Parameter(description = "Borrower ID") @PathVariable UUID id) {
        log.info("REST request to delete borrower with ID: {}", id);

        return borrowerService.deleteBorrower(id)
                .then(Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(ApiResponse.<Void>success("Borrower deleted successfully", null))))
                .doOnSuccess(response -> log.info("Successfully deleted borrower {} via REST API", id))
                .doOnError(error -> log.error("Failed to delete borrower {} via REST API: {}", id, error.getMessage()));
    }
}