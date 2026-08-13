package com.academic.authlab.repository;

import java.util.Optional;

public interface UserRepository {
    Optional<UserCredential> findByUsername(String username);

    boolean isAnyDatabaseAvailable();
}
