package yolohome.backend.dto;

import java.util.List;

public record SensorDataResponse(
        SensorValue temperature,
        SensorValue humidity,
        SensorValue light
) {
    public record SensorValue(
            float current,
            String unit,
            List<Float> history
    ) {
    }
}
