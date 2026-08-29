package yolohome.backend.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import yolohome.backend.service.SensorReadingService;

@RestController
@RequiredArgsConstructor
public class SystemController {

    private static final String DEFAULT_DEVICE_ID = "yolohome-01";

    private final SensorReadingService sensorReadingService;

    /**
     * GET /system/status
     * Khop dung frontend SystemAPI.getLastUpdated() - doi body.lastUpdated.
     */
    @GetMapping("/system/status")
    public Map<String, String> getStatus() {
        try {
            var latest = sensorReadingService.getLatestForDevice(DEFAULT_DEVICE_ID);
            return Map.of("lastUpdated", latest.recordedAt().toString());
        } catch (Exception e) {
            return Map.of("lastUpdated", "--");
        }
    }
}
