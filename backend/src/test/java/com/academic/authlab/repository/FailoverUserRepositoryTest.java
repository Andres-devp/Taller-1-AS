package com.academic.authlab.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.academic.authlab.error.DatabaseUnavailableException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class FailoverUserRepositoryTest {
    private JdbcTemplate secondary;

    @BeforeEach
    void setUpSecondary() {
        secondary = template("jdbc:h2:mem:secondary;DB_CLOSE_DELAY=-1");
        secondary.execute("DROP TABLE IF EXISTS users");
        secondary.execute("CREATE TABLE users (username VARCHAR(100) PRIMARY KEY, password_hash VARCHAR(100) NOT NULL)");
        secondary.update("INSERT INTO users(username, password_hash) VALUES (?, ?)", "student", "hash");
    }

    @Test
    void usesSecondaryWhenPrimaryHasAConnectivityFailure() {
        FailoverUserRepository repository = new FailoverUserRepository(
                template("jdbc:h2:tcp://localhost:65534/./offline"), secondary, 1);

        assertThat(repository.findByUsername("student"))
                .contains(new UserCredential("student", "hash"));
    }

    @Test
    void doesNotFailOverWhenPrimaryReturnsNoUser() {
        JdbcTemplate primary = template("jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1");
        primary.execute("CREATE TABLE users (username VARCHAR(100) PRIMARY KEY, password_hash VARCHAR(100) NOT NULL)");
        FailoverUserRepository repository = new FailoverUserRepository(primary, secondary, 1);

        assertThat(repository.findByUsername("student")).isEqualTo(Optional.empty());
    }

    @Test
    void reportsInfrastructureFailureWhenNoDatabaseCanBeReached() {
        JdbcTemplate unavailable = template("jdbc:h2:tcp://localhost:65534/./offline");
        FailoverUserRepository repository = new FailoverUserRepository(unavailable, unavailable, 1);

        assertThatThrownBy(() -> repository.findByUsername("student"))
                .isInstanceOf(DatabaseUnavailableException.class);
        assertThat(repository.isAnyDatabaseAvailable()).isFalse();
    }

    private JdbcTemplate template(String url) {
        return new JdbcTemplate(new DriverManagerDataSource(url, "sa", ""));
    }
}
