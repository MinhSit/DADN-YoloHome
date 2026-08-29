package yolohome.backend.dto;

public record ThresholdsResponse(
        float temperature,
        float humidity,
        Float light
) {
}
