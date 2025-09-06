package com.exam.library.service;

import com.exam.library.dto.request.BorrowerDto;
import com.exam.library.dto.request.CreateBorrowerRequest;
import com.exam.library.exception.BorrowerNotFoundException;
import com.exam.library.exception.EmailAlreadyExistsException;
import com.exam.library.model.Borrower;
import com.exam.library.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowerService Unit Tests")
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BorrowerService borrowerService;

    private Borrower testBorrower;
    private CreateBorrowerRequest createBorrowerRequest;
    private UUID borrowerId;
    private LocalDateTime now;
    private String testEmail;
    private String testName;

    @BeforeEach
    void setUp() {
        borrowerId = UUID.randomUUID();
        now = LocalDateTime.now();
        testEmail = "test@example.com";
        testName = "Test User";

        testBorrower = Borrower.builder()
                .id(borrowerId)
                .name(testName)
                .email(testEmail)
                .createdAt(now)
                .updatedAt(now)
                .build();

        createBorrowerRequest = CreateBorrowerRequest.builder()
                .name(testName)
                .email(testEmail)
                .build();
    }

    @Test
    @DisplayName("Should create borrower successfully when email does not exist")
    void createBorrower_EmailDoesNotExist_ShouldReturnBorrowerDto() {
        // Given
        when(borrowerRepository.existsByEmail(testEmail))
                .thenReturn(Mono.just(false));
        when(borrowerRepository.save(any(Borrower.class)))
                .thenReturn(Mono.just(testBorrower));

        // When & Then
        StepVerifier.create(borrowerService.createBorrower(createBorrowerRequest))
                .expectNextMatches(dto ->
                        dto.getId().equals(borrowerId) &&
                                dto.getName().equals(testName) &&
                                dto.getEmail().equals(testEmail) &&
                                dto.getCreatedAt().equals(now) &&
                                dto.getUpdatedAt().equals(now)
                )
                .verifyComplete();

        verify(borrowerRepository).existsByEmail(testEmail);
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email already exists")
    void createBorrower_EmailAlreadyExists_ShouldThrowException() {
        // Given
        when(borrowerRepository.existsByEmail(testEmail))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(borrowerService.createBorrower(createBorrowerRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof EmailAlreadyExistsException &&
                                throwable.getMessage().contains(testEmail)
                )
                .verify();

        verify(borrowerRepository).existsByEmail(testEmail);
        verify(borrowerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return all borrowers")
    void getAllBorrowers_ShouldReturnAllBorrowers() {
        // Given
        Borrower anotherBorrower = Borrower.builder()
                .id(UUID.randomUUID())
                .name("Another User")
                .email("another@example.com")
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(borrowerRepository.findAll())
                .thenReturn(Flux.just(testBorrower, anotherBorrower));

        // When & Then
        StepVerifier.create(borrowerService.getAllBorrowers())
                .expectNextCount(2)
                .verifyComplete();

        verify(borrowerRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty flux when no borrowers exist")
    void getAllBorrowers_NoBorrowers_ShouldReturnEmptyFlux() {
        // Given
        when(borrowerRepository.findAll())
                .thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(borrowerService.getAllBorrowers())
                .verifyComplete();

        verify(borrowerRepository).findAll();
    }

    @Test
    @DisplayName("Should return borrower by ID when borrower exists")
    void getBorrowerById_BorrowerExists_ShouldReturnBorrowerDto() {
        // Given
        when(borrowerRepository.findById(borrowerId))
                .thenReturn(Mono.just(testBorrower));

        // When & Then
        StepVerifier.create(borrowerService.getBorrowerById(borrowerId))
                .expectNextMatches(dto ->
                        dto.getId().equals(borrowerId) &&
                                dto.getName().equals(testName) &&
                                dto.getEmail().equals(testEmail)
                )
                .verifyComplete();

        verify(borrowerRepository).findById(borrowerId);
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when borrower ID not found")
    void getBorrowerById_BorrowerNotFound_ShouldThrowException() {
        // Given
        when(borrowerRepository.findById(borrowerId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(borrowerService.getBorrowerById(borrowerId))
                .expectErrorMatches(throwable ->
                        throwable instanceof BorrowerNotFoundException &&
                                throwable.getMessage().contains(borrowerId.toString())
                )
                .verify();

        verify(borrowerRepository).findById(borrowerId);
    }

    @Test
    @DisplayName("Should return borrower by email when borrower exists")
    void getBorrowerByEmail_BorrowerExists_ShouldReturnBorrowerDto() {
        // Given
        when(borrowerRepository.findByEmail(testEmail))
                .thenReturn(Mono.just(testBorrower));

        // When & Then
        StepVerifier.create(borrowerService.getBorrowerByEmail(testEmail))
                .expectNextMatches(dto ->
                        dto.getEmail().equals(testEmail) &&
                                dto.getName().equals(testName)
                )
                .verifyComplete();

        verify(borrowerRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when borrower email not found")
    void getBorrowerByEmail_BorrowerNotFound_ShouldThrowException() {
        // Given
        String nonExistentEmail = "nonexistent@example.com";
        when(borrowerRepository.findByEmail(nonExistentEmail))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(borrowerService.getBorrowerByEmail(nonExistentEmail))
                .expectErrorMatches(throwable ->
                        throwable instanceof BorrowerNotFoundException &&
                                throwable.getMessage().contains(nonExistentEmail)
                )
                .verify();

        verify(borrowerRepository).findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("Should update borrower successfully when borrower exists and email unchanged")
    void updateBorrower_BorrowerExistsEmailUnchanged_ShouldReturnUpdatedDto() {
        // Given
        CreateBorrowerRequest updateRequest = CreateBorrowerRequest.builder()
                .name("Updated Name")
                .email(testEmail) // Same email
                .build();

        Borrower updatedBorrower = Borrower.builder()
                .id(borrowerId)
                .name("Updated Name")
                .email(testEmail)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(borrowerRepository.findById(borrowerId))
                .thenReturn(Mono.just(testBorrower));
        when(borrowerRepository.save(any(Borrower.class)))
                .thenReturn(Mono.just(updatedBorrower));

        // When & Then
        StepVerifier.create(borrowerService.updateBorrower(borrowerId, updateRequest))
                .expectNextMatches(dto ->
                        dto.getId().equals(borrowerId) &&
                                dto.getName().equals("Updated Name") &&
                                dto.getEmail().equals(testEmail)
                )
                .verifyComplete();

        verify(borrowerRepository).findById(borrowerId);
        verify(borrowerRepository).save(any(Borrower.class));
        verify(borrowerRepository, never()).existsByEmail(any()); // Should not check email existence
    }

    @Test
    @DisplayName("Should update borrower successfully when email is changed and new email is available")
    void updateBorrower_EmailChangedAndAvailable_ShouldReturnUpdatedDto() {
        // Given
        String newEmail = "newemail@example.com";
        CreateBorrowerRequest updateRequest = CreateBorrowerRequest.builder()
                .name("Updated Name")
                .email(newEmail)
                .build();

        Borrower updatedBorrower = Borrower.builder()
                .id(borrowerId)
                .name("Updated Name")
                .email(newEmail)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(borrowerRepository.findById(borrowerId))
                .thenReturn(Mono.just(testBorrower));
        when(borrowerRepository.existsByEmail(newEmail))
                .thenReturn(Mono.just(false));
        when(borrowerRepository.save(any(Borrower.class)))
                .thenReturn(Mono.just(updatedBorrower));

        // When & Then
        StepVerifier.create(borrowerService.updateBorrower(borrowerId, updateRequest))
                .expectNextMatches(dto ->
                        dto.getEmail().equals(newEmail) &&
                                dto.getName().equals("Updated Name")
                )
                .verifyComplete();

        verify(borrowerRepository).findById(borrowerId);
        verify(borrowerRepository).existsByEmail(newEmail);
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when updating with existing email")
    void updateBorrower_EmailChangedButAlreadyExists_ShouldThrowException() {
        // Given
        String existingEmail = "existing@example.com";
        CreateBorrowerRequest updateRequest = CreateBorrowerRequest.builder()
                .name("Updated Name")
                .email(existingEmail)
                .build();

        when(borrowerRepository.findById(borrowerId))
                .thenReturn(Mono.just(testBorrower));
        when(borrowerRepository.existsByEmail(existingEmail))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(borrowerService.updateBorrower(borrowerId, updateRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof EmailAlreadyExistsException &&
                                throwable.getMessage().contains(existingEmail)
                )
                .verify();

        verify(borrowerRepository).findById(borrowerId);
        verify(borrowerRepository).existsByEmail(existingEmail);
        verify(borrowerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when updating non-existent borrower")
    void updateBorrower_BorrowerNotFound_ShouldThrowException() {
        // Given
        when(borrowerRepository.findById(borrowerId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(borrowerService.updateBorrower(borrowerId, createBorrowerRequest))
                .expectErrorMatches(throwable ->
                        throwable instanceof BorrowerNotFoundException &&
                                throwable.getMessage().contains(borrowerId.toString())
                )
                .verify();

        verify(borrowerRepository).findById(borrowerId);
        verify(borrowerRepository, never()).existsByEmail(any());
        verify(borrowerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete borrower successfully when borrower exists")
    void deleteBorrower_BorrowerExists_ShouldDeleteSuccessfully() {
        // Given
        when(borrowerRepository.existsById(borrowerId))
                .thenReturn(Mono.just(true));
        when(borrowerRepository.deleteById(borrowerId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(borrowerService.deleteBorrower(borrowerId))
                .verifyComplete();

        verify(borrowerRepository).existsById(borrowerId);
        verify(borrowerRepository).deleteById(borrowerId);
    }

    @Test
    @DisplayName("Should throw BorrowerNotFoundException when deleting non-existent borrower")
    void deleteBorrower_BorrowerNotFound_ShouldThrowException() {
        // Given
        when(borrowerRepository.existsById(borrowerId))
                .thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(borrowerService.deleteBorrower(borrowerId))
                .expectErrorMatches(throwable ->
                        throwable instanceof BorrowerNotFoundException &&
                                throwable.getMessage().contains(borrowerId.toString())
                )
                .verify();

        verify(borrowerRepository).existsById(borrowerId);
        verify(borrowerRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should handle repository error during creation")
    void createBorrower_RepositoryError_ShouldPropagateError() {
        // Given
        when(borrowerRepository.existsByEmail(testEmail))
                .thenReturn(Mono.error(new RuntimeException("Database connection error")));

        // When & Then
        StepVerifier.create(borrowerService.createBorrower(createBorrowerRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(borrowerRepository).existsByEmail(testEmail);
        verify(borrowerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository error during save")
    void createBorrower_SaveError_ShouldPropagateError() {
        // Given
        when(borrowerRepository.existsByEmail(testEmail))
                .thenReturn(Mono.just(false));
        when(borrowerRepository.save(any(Borrower.class)))
                .thenReturn(Mono.error(new RuntimeException("Save failed")));

        // When & Then
        StepVerifier.create(borrowerService.createBorrower(createBorrowerRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(borrowerRepository).existsByEmail(testEmail);
        verify(borrowerRepository).save(any(Borrower.class));
    }

    @Test
    @DisplayName("Should handle repository error during getAllBorrowers")
    void getAllBorrowers_RepositoryError_ShouldPropagateError() {
        // Given
        when(borrowerRepository.findAll())
                .thenReturn(Flux.error(new RuntimeException("Database error")));

        // When & Then
        StepVerifier.create(borrowerService.getAllBorrowers())
                .expectError(RuntimeException.class)
                .verify();

        verify(borrowerRepository).findAll();
    }

    @Test
    @DisplayName("Should map borrower to DTO correctly")
    void mapToDto_ShouldMapAllFields() {
        // This is implicitly tested in other methods, but we can verify the mapping logic
        // by checking that all fields are properly mapped in our test assertions

        // Given
        when(borrowerRepository.findById(borrowerId))
                .thenReturn(Mono.just(testBorrower));

        // When & Then
        StepVerifier.create(borrowerService.getBorrowerById(borrowerId))
                .expectNextMatches(dto ->
                        dto.getId().equals(testBorrower.getId()) &&
                                dto.getName().equals(testBorrower.getName()) &&
                                dto.getEmail().equals(testBorrower.getEmail()) &&
                                dto.getCreatedAt().equals(testBorrower.getCreatedAt()) &&
                                dto.getUpdatedAt().equals(testBorrower.getUpdatedAt())
                )
                .verifyComplete();
    }
}