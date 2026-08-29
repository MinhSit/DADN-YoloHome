package yolohome.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.*;
import yolohome.backend.entity.AppSettings;
import yolohome.backend.repository.AppSettingsRepository;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private static final short SETTINGS_ID = 1;

    private final AppSettingsRepository repository;

    private final MqttCommandPublisher mqttCommandPublisher;

    public AdafruitConfigResponse getAdafruitConfig() {
        AppSettings settings = getOrCreate();
        return new AdafruitConfigResponse(
                settings.getAdafruitUsername() == null ? "" : settings.getAdafruitUsername(),
                settings.getAdafruitAioKey() == null ? "" : settings.getAdafruitAioKey()
        );
    }

    public ThresholdsResponse getThresholds() {
        AppSettings settings = getOrCreate();
        return new ThresholdsResponse(
                settings.getTemperatureThreshold(),
                settings.getHumidityThreshold(),
                settings.getLightThreshold()
        );
    }

    @Transactional
    public SimpleResponse saveAdafruitConfig(SaveAdafruitConfigRequest request) {
        AppSettings settings = getOrCreate();
        settings.setAdafruitUsername(request.username());
        settings.setAdafruitAioKey(request.aioKey());
        repository.save(settings);
        return new SimpleResponse(true, "Adafruit IO settings saved.");
    }

    @Transactional
    public SimpleResponse saveThresholds(SaveThresholdsRequest request) {
        AppSettings settings = getOrCreate();
        settings.setTemperatureThreshold(request.temperature());
        settings.setHumidityThreshold(request.humidity());
        settings.setLightThreshold(request.light());
        repository.save(settings);
        mqttCommandPublisher.publishThresholds(
                "yolohome-01",
                request.temperature(),
                request.humidity(),
                request.light()
        );
        return new SimpleResponse(true, "Thresholds saved.");
    }

    private AppSettings getOrCreate() {
        return repository.findById(SETTINGS_ID).orElseGet(() -> {
            AppSettings settings = new AppSettings();
            settings.setId(SETTINGS_ID);
            settings.setTemperatureThreshold(30f);
            settings.setHumidityThreshold(70f);
            return repository.save(settings);
        });
    }
}
