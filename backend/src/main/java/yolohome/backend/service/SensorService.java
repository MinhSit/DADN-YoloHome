package yolohome.backend.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.SensorDataResponse;
import yolohome.backend.entity.SensorReading;
import yolohome.backend.repository.SensorReadingRepository;
import yolohome.backend.repository.SensorReadingSpecifications;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorService {

    private static final String DEFAULT_DEVICE_ID = "yolohome-01";
    private static final int HISTORY_SIZE = 12;

    private final SensorReadingRepository repository;

    public SensorDataResponse getSensorData() {
        // Lay 12 reading gan nhat cua device mac dinh, sap xep moi nhat truoc
        List<SensorReading> recent = repository.findAll(
                SensorReadingSpecifications.withFilters(DEFAULT_DEVICE_ID, null, null),
                PageRequest.of(0, HISTORY_SIZE, Sort.by(Sort.Direction.DESC, "recordedAt"))
        ).getContent();

        if (recent.isEmpty()) {
            SensorDataResponse.SensorValue empty = new SensorDataResponse.SensorValue(0, "", Collections.emptyList());
            return new SensorDataResponse(empty, empty, empty);
        }

        // Dao lai thanh thu tu tang dan theo thoi gian de ve bieu do dung chieu
        List<SensorReading> chronological = recent.reversed();

        SensorReading latest = recent.get(0);

        return new SensorDataResponse(
                new SensorDataResponse.SensorValue(
                        latest.getTemperature(), "°C",
                        chronological.stream().map(SensorReading::getTemperature).toList()
                ),
                new SensorDataResponse.SensorValue(
                        latest.getHumidity(), "%",
                        chronological.stream().map(SensorReading::getHumidity).toList()
                ),
                new SensorDataResponse.SensorValue(
                        latest.getLight(), "",
                        chronological.stream().map(r -> (float) r.getLight()).toList()
                )
        );
    }
}

