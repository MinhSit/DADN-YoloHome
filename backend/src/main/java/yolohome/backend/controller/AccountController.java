package yolohome.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.AccountResponse;
import yolohome.backend.service.AccountService;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * GET /account
     * Lay thong tin user hien tai dua tren JWT token trong header Authorization.
     */
    @GetMapping("/account")
    public AccountResponse getAccount() {
        return accountService.getCurrentAccount();
    }
}
