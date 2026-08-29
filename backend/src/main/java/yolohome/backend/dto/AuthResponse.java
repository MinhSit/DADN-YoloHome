package yolohome.backend.dto;

public record AuthResponse(
        boolean success,
        String message,
        String token
) {
    public static AuthResponse ok(String message, String token) {
        return new AuthResponse(true, message, token);
    }

    public static AuthResponse fail(String message) {
        return new AuthResponse(false, message, null);
    }
}
