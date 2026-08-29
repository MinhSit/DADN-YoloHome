package yolohome.backend.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.DeviceCommandRequest;
import yolohome.backend.dto.DeviceCommandResponse;
import yolohome.backend.dto.DeviceSummaryResponse;
import yolohome.backend.dto.SensorReadingResponse;
import yolohome.backend.entity.SensorReading;
import yolohome.backend.exception.ResourceNotFoundException;
import yolohome.backend.repository.SensorReadingRepository;
import yolohome.backend.repository.SensorReadingSpecifications;

@Service
@RequiredArgsConstructor
public class SensorReadingService {

    private final SensorReadingRepository repository;
    private final MqttCommandPublisher mqttCommandPublisher;

    public List<DeviceSummaryResponse> listDevices() {
        return repository.findDeviceSummaries().stream()
                .map(DeviceSummaryResponse::from)
                .toList();
    }

    public SensorReadingResponse getLatestForDevice(String deviceId) {
        SensorReading reading = repository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Khong tim thay reading nao cho device_id: " + deviceId));
        return SensorReadingResponse.from(reading);
    }

    public Page<SensorReadingResponse> searchReadings(
            String deviceId, OffsetDateTime from, OffsetDateTime to, Pageable pageable) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Tham so 'from' phai truoc hoac bang 'to'");
        }

        return repository
                .findAll(SensorReadingSpecifications.withFilters(deviceId, from, to), pageable)
                .map(SensorReadingResponse::from);
    }

    /**
     * Gui lenh dieu khien LED/Fan xuong device qua MQTT.
     * Kiem tra device_id da tung xuat hien trong DB truoc khi gui, tranh gui nham topic
     * cho mot device khong ton tai (vi du go sai id).
     */
    public DeviceCommandResponse sendCommand(String deviceId, DeviceCommandRequest command) {
        boolean deviceExists = repository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId).isPresent();
        if (!deviceExists) {
            throw new ResourceNotFoundException(
                    "Khong tim thay device_id: " + deviceId + " trong lich su du lieu");
        }
        return mqttCommandPublisher.publishCommand(deviceId, command);
    }
}
