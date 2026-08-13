package com.academic.authlab.repository;

import com.academic.authlab.error.DatabaseUnavailableException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FailoverUserRepository implements UserRepository {
    private static final String USER_QUERY = "SELECT username, password_hash FROM users WHERE username = ?";
    private static final int MAX_CONNECTION_ATTEMPTS = 3;

    private final JdbcTemplate primary;
    private final JdbcTemplate secondary;
    private final long retryDelayMs;

    public FailoverUserRepository(
            @Qualifier("primaryJdbcTemplate") JdbcTemplate primary,
            @Qualifier("secondaryJdbcTemplate") JdbcTemplate secondary,
            @Value("${auth.database.retry-delay-ms:100}") long retryDelayMs) {
        this.primary = primary;
        this.secondary = secondary;
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public Optional<UserCredential> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }

        try {
            // An empty result is a valid answer from Primary, never a failover condition.
            return queryWithRetries(primary, username);
        } catch (DatabaseUnavailableException primaryUnavailable) {
            return queryWithRetries(secondary, username);
        }
    }

    @Override
    public boolean isAnyDatabaseAvailable() {
        return canQuery(primary) || canQuery(secondary);
    }

    private Optional<UserCredential> queryWithRetries(JdbcTemplate database, String username) {
        for (int attempt = 1; attempt <= MAX_CONNECTION_ATTEMPTS; attempt++) {
            try {
                List<UserCredential> users = database.query(USER_QUERY,
                        (row, rowNumber) -> new UserCredential(row.getString("username"), row.getString("password_hash")),
                        username);
                return users.stream().findFirst();
            } catch (DataAccessException exception) {
                if (!isConnectivityFailure(exception)) {
                    throw exception;
                }
                if (attempt < MAX_CONNECTION_ATTEMPTS) {
                    pauseBeforeRetry();
                }
            }
        }
        throw new DatabaseUnavailableException();
    }

    private boolean canQuery(JdbcTemplate database) {
        try {
            Integer result = database.queryForObject("SELECT 1", Integer.class);
            return result != null && result == 1;
        } catch (DataAccessException exception) {
            return false;
        }
    }

    private boolean isConnectivityFailure(DataAccessException exception) {
        if (exception instanceof DataAccessResourceFailureException) {
            return true;
        }
        Throwable cause = exception.getMostSpecificCause();
        return cause instanceof SQLException sqlException && sqlException.getSQLState() != null
                && sqlException.getSQLState().startsWith("08");
    }

    private void pauseBeforeRetry() {
        try {
            Thread.sleep(retryDelayMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new DatabaseUnavailableException();
        }
    }
}
