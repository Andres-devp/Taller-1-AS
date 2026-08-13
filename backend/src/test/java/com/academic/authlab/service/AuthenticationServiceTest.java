package com.academic.authlab.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.academic.authlab.repository.UserCredential;
import com.academic.authlab.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Test
    void authenticatesOnlyWhenTheHashMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        when(userRepository.findByUsername("student"))
                .thenReturn(Optional.of(new UserCredential("student", encoder.encode("laboratory-password"))));
        AuthenticationService service = new AuthenticationService(userRepository, encoder);

        assertThat(service.authenticate("student", "laboratory-password")).isTrue();
        assertThat(service.authenticate("student", "incorrect-password")).isFalse();
    }

    @Test
    void rejectsUnknownAndBlankCredentialsWithoutExposingTheReason() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        AuthenticationService service = new AuthenticationService(userRepository, encoder);

        assertThat(service.authenticate("unknown", "anything")).isFalse();
        assertThat(service.authenticate("", "anything")).isFalse();
        assertThat(service.authenticate("student", "")).isFalse();
    }
}
