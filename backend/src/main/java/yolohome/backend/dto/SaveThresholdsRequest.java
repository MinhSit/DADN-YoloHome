package yolohome.backend.dto;

public record SaveThresholdsRequest(
        float temperature,
        float humidity,
        Float light
) {
}