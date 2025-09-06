package com.exam.library.repository;


import com.exam.library.model.Borrower;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface BorrowerRepository extends R2dbcRepository<Borrower, UUID> {

    Mono<Borrower> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);
}
