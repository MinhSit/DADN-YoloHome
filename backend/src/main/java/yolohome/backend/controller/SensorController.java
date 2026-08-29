package yolohome.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.SensorDataResponse;
import yolohome.backend.service.SensorService;

@RestController
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    /**
     * GET /sensors
     * Khop dung frontend SensorAPI.getSensorData().
     */
    @GetMapping("/sensors")
    public SensorDataResponse getSensorData() {
        return sensorService.getSensorData();
    }
}
