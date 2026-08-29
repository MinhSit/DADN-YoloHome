package yolohome.backend.dto;

import yolohome.backend.entity.Alert;

public record AlertResponse(
        Long id,
        String level,
        String message,
        String time,
        boolean read
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getLevel(),
                alert.getMessage(),
                alert.getCreatedAt().toString(),
                Boolean.TRUE.equals(alert.getRead())
        );
    }
}
