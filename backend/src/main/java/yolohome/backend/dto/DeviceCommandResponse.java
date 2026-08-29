package yolohome.backend.dto;

public record DeviceCommandResponse(
        String deviceId,
        String topic,
        String payload,
        String status
) {
}
