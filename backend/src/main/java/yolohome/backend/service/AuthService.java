package yolohome.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.AuthResponse;
import yolohome.backend.dto.LoginRequest;
import yolohome.backend.dto.RegisterRequest;
import yolohome.backend.entity.User;
import yolohome.backend.repository.UserRepository;
import yolohome.backend.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return AuthResponse.fail("Email da duoc su dung.");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.ok("Dang ky thanh cong.", token);
    }

    public AuthResponse login(LoginRequest request) {
        return userRepository.findByEmail(request.email())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .map(user -> AuthResponse.ok("Login successful.", jwtService.generateToken(user.getId(), user.getEmail())))
                .orElseGet(() -> AuthResponse.fail("Invalid email or password."));
    }
}
