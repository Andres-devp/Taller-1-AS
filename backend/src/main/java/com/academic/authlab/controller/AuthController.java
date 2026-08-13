package com.academic.authlab.controller;

import com.academic.authlab.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody(required = false) LoginRequest request) {
        boolean authenticated = request != null
                && authenticationService.authenticate(request.username(), request.password());

        return ResponseEntity.status(authenticated ? HttpStatus.OK : HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(authenticated));
    }
}
