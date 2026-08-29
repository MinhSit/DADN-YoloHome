package yolohome.backend.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.AccountResponse;
import yolohome.backend.entity.User;
import yolohome.backend.exception.ResourceNotFoundException;
import yolohome.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;

    public AccountResponse getCurrentAccount() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user"));
        return new AccountResponse(user.getName(), user.getEmail());
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
