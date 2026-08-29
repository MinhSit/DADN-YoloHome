package yolohome.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import yolohome.backend.dto.AuthResponse;
import yolohome.backend.dto.LoginRequest;
import yolohome.backend.dto.RegisterRequest;
import yolohome.backend.service.AuthService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /register
     * Body: {"email": "...", "password": "...", "name": "..."}
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * POST /login
     * Body: {"email": "...", "password": "..."}
     * Khop dung format ma frontend AuthAPI.login() dang mong doi.
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
