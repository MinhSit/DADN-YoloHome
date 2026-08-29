package yolohome.backend.dto;

import yolohome.backend.entity.ActivityLog;

public record LogResponse(
        String time,
        String type,
        String description,
        String device,
        String status
) {
    public static LogResponse from(ActivityLog log) {
        return new LogResponse(
                log.getOccurredAt().toString(),
                log.getLogType(),
                log.getDescription(),
                log.getDevice(),
                log.getStatus()
        );
    }
}
