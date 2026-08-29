package yolohome.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.DeviceCommandRequest;
import yolohome.backend.dto.DeviceResponse;
import yolohome.backend.dto.SimpleResponse;
import yolohome.backend.entity.DeviceState;
import yolohome.backend.repository.DeviceStateRepository;

import java.util.List;

/**
 * Quan ly device_state - "single source of truth" cho GET /devices cua frontend.
 * ID vat ly duy nhat trong he thong hien tai la "yolohome-01" (1 hang trong device_state),
 * nhung duoc phoi thanh 2 "logical device" o tang API: "light" va "fan",
 * khop dung 2 dong mockData.devices ma frontend dang mong doi.
 */
@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final String PHYSICAL_DEVICE_ID = "yolohome-01";

    private final DeviceStateRepository deviceStateRepository;
    private final SensorReadingService sensorReadingService;
    private final ActivityLogService activityLogService;
    private final MqttCommandPublisher mqttCommandPublisher;

    public List<DeviceResponse> getAllDevices() {
        DeviceState state = getOrCreateState();
        return List.of(DeviceResponse.light(state), DeviceResponse.fan(state));
    }

    @Transactional
    public SimpleResponse setDeviceStatus(String logicalDeviceId, String status) {
        boolean turnOn = "on".equalsIgnoreCase(status);

        if (!"light".equals(logicalDeviceId) && !"fan".equals(logicalDeviceId)) {
            return new SimpleResponse(false, "Device not found.");
        }

        DeviceState state = getOrCreateState();

        // Cap nhat desired state cua device vua duoc dieu khien
        if ("light".equals(logicalDeviceId)) {
            state.setLedState(turnOn);
        } else {
            state.setFanState(turnOn);
        }

        // Moi MQTT command luon gui FULL desired state
        DeviceCommandRequest command = new DeviceCommandRequest(
                state.getLedState(),
                state.getFanState()
        );

        // Publish full state xuong Yolo:Bit
        sensorReadingService.sendCommand(PHYSICAL_DEVICE_ID, command);
        mqttCommandPublisher.publishManualAction(
                PHYSICAL_DEVICE_ID,
                logicalDeviceId,
                turnOn
        );

        deviceStateRepository.save(state);

        String deviceName = "light".equals(logicalDeviceId) ? "Light" : "Fan";
        activityLogService.log(
                "Device",
                deviceName + " turned " + status.toUpperCase(),
                deviceName,
                "Success"
        );

        return new SimpleResponse(true, "Device updated.");
    }

    private DeviceState getOrCreateState() {
        return deviceStateRepository.findById(PHYSICAL_DEVICE_ID)
                .orElseGet(() -> {
                    DeviceState newState = new DeviceState();
                    newState.setDeviceId(PHYSICAL_DEVICE_ID);
                    newState.setName("Yolohome Board");
                    newState.setDeviceType("toggle");
                    newState.setLedState(false);
                    newState.setFanState(false);
                    newState.setOnline(true);
                    return deviceStateRepository.save(newState);
                });
    }
}
