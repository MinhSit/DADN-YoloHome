package yolohome.backend.dto;

import java.time.OffsetDateTime;

import yolohome.backend.repository.SensorReadingRepository;

public record DeviceSummaryResponse(
        String deviceId,
        OffsetDateTime lastSeen
) {
    public static DeviceSummaryResponse from(SensorReadingRepository.DeviceSummary s) {
        return new DeviceSummaryResponse(s.getDeviceId(), s.getLastSeen());
    }
}
