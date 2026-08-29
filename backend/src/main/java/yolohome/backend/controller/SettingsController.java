package yolohome.backend.controller;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.*;
import yolohome.backend.service.SettingsService;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/adafruit")
    public AdafruitConfigResponse getAdafruitConfig() {
        return settingsService.getAdafruitConfig();
    }

    @PutMapping("/adafruit")
    public SimpleResponse saveAdafruitConfig(@Valid @RequestBody SaveAdafruitConfigRequest request) {
        return settingsService.saveAdafruitConfig(request);
    }

    @GetMapping("/thresholds")
    public ThresholdsResponse getThresholds() {
        return settingsService.getThresholds();
    }

    @PutMapping("/thresholds")
    public SimpleResponse saveThresholds(@Valid @RequestBody SaveThresholdsRequest request) {
        return settingsService.saveThresholds(request);
    }
}
