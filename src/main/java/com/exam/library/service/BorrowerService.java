package com.exam.library.service;

import com.exam.library.dto.request.BorrowerDto;
import com.exam.library.dto.request.CreateBorrowerRequest;
import com.exam.library.exception.BorrowerNotFoundException;
import com.exam.library.exception.EmailAlreadyExistsException;
import com.exam.library.model.Borrower;
import com.exam.library.repository.BorrowerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    @Transactional
    public Mono<BorrowerDto> createBorrower(CreateBorrowerRequest request) {
        log.info("Creating borrower with email: {}", request.getEmail());
        
        return borrowerRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new EmailAlreadyExistsException(
                                "Borrower with email " + request.getEmail() + " already exists"));
                    }
                    
                    Borrower borrower = Borrower.builder()
                            .name(request.getName())
                            .email(request.getEmail())
                            .build();
                    
                    return borrowerRepository.save(borrower);
                })
                .map(this::mapToDto)
                .doOnSuccess(dto -> log.info("Successfully created borrower with ID: {}", dto.getId()))
                .doOnError(error -> log.error("Failed to create borrower: {}", error.getMessage()));
    }

    @Transactional(readOnly = true)
    public Flux<BorrowerDto> getAllBorrowers() {
        log.info("Retrieving all borrowers");
        return borrowerRepository.findAll()
                .map(this::mapToDto)
                .doOnComplete(() -> log.info("Successfully retrieved all borrowers"));
    }

    @Transactional(readOnly = true)
    public Mono<BorrowerDto> getBorrowerById(UUID id) {
        log.info("Retrieving borrower with ID: {}", id);
        return borrowerRepository.findById(id)
                .switchIfEmpty(Mono.error(new BorrowerNotFoundException("Borrower not found with ID: " + id)))
                .map(this::mapToDto)
                .doOnSuccess(dto -> log.info("Successfully retrieved borrower: {}", dto.getName()))
                .doOnError(error -> log.error("Failed to retrieve borrower with ID {}: {}", id, error.getMessage()));
    }

    @Transactional(readOnly = true)
    public Mono<BorrowerDto> getBorrowerByEmail(String email) {
        log.info("Retrieving borrower with email: {}", email);
        return borrowerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BorrowerNotFoundException("Borrower not found with email: " + email)))
                .map(this::mapToDto)
                .doOnSuccess(dto -> log.info("Successfully retrieved borrower: {}", dto.getName()))
                .doOnError(error -> log.error("Failed to retrieve borrower with email {}: {}", email, error.getMessage()));
    }

    @Transactional
    public Mono<BorrowerDto> updateBorrower(UUID id, CreateBorrowerRequest request) {
        log.info("Updating borrower with ID: {}", id);
        
        return borrowerRepository.findById(id)
                .switchIfEmpty(Mono.error(new BorrowerNotFoundException("Borrower not found with ID: " + id)))
                .flatMap(existingBorrower -> {
                    // Check if email is being changed and if new email already exists
                    if (!existingBorrower.getEmail().equals(request.getEmail())) {
                        return borrowerRepository.existsByEmail(request.getEmail())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.error(new EmailAlreadyExistsException(
                                                "Email " + request.getEmail() + " is already in use"));
                                    }
                                    return Mono.just(existingBorrower);
                                });
                    }
                    return Mono.just(existingBorrower);
                })
                .map(borrower -> {
                    borrower.setName(request.getName());
                    borrower.setEmail(request.getEmail());
                    return borrower;
                })
                .flatMap(borrowerRepository::save)
                .map(this::mapToDto)
                .doOnSuccess(dto -> log.info("Successfully updated borrower with ID: {}", dto.getId()))
                .doOnError(error -> log.error("Failed to update borrower with ID {}: {}", id, error.getMessage()));
    }

    @Transactional
    public Mono<Void> deleteBorrower(UUID id) {
        log.info("Deleting borrower with ID: {}", id);
        
        return borrowerRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BorrowerNotFoundException("Borrower not found with ID: " + id));
                    }
                    return borrowerRepository.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Successfully deleted borrower with ID: {}", id))
                .doOnError(error -> log.error("Failed to delete borrower with ID {}: {}", id, error.getMessage()));
    }

    private BorrowerDto mapToDto(Borrower borrower) {
        return BorrowerDto.builder()
                .id(borrower.getId())
                .name(borrower.getName())
                .email(borrower.getEmail())
                .createdAt(borrower.getCreatedAt())
                .updatedAt(borrower.getUpdatedAt())
                .build();
    }
}