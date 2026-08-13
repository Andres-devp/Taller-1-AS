package com.academic.authlab.health;

import com.academic.authlab.repository.UserRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("databaseAvailability")
public class DatabaseAvailabilityHealthIndicator implements HealthIndicator {
    private final UserRepository userRepository;

    public DatabaseAvailabilityHealthIndicator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Health health() {
        return userRepository.isAnyDatabaseAvailable()
                ? Health.up().withDetail("database", "at least one H2 instance is reachable").build()
                : Health.down().withDetail("database", "no H2 instance is reachable").build();
    }
}
