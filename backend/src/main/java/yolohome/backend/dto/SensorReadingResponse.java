package yolohome.backend.dto;

import java.time.OffsetDateTime;

import yolohome.backend.entity.SensorReading;

/**
 * DTO tra ve cho client - tach khoi entity de khong lo cau truc DB ra ngoai
 * va chu dong ve field naming (camelCase) cho frontend.
 */
public record SensorReadingResponse(
        Long id,
        String deviceId,
        OffsetDateTime recordedAt,
        Float temperature,
        Float humidity,
        Integer light,
        Boolean ledState,
        Boolean fanState
) {
    public static SensorReadingResponse from(SensorReading r) {
        return new SensorReadingResponse(
                r.getId(),
                r.getDeviceId(),
                r.getRecordedAt(),
                r.getTemperature(),
                r.getHumidity(),
                r.getLight(),
                r.getLedState(),
                r.getFanState()
        );
    }
}
