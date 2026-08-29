package yolohome.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.DeviceResponse;
import yolohome.backend.dto.SetDeviceStatusRequest;
import yolohome.backend.dto.SimpleResponse;
import yolohome.backend.service.DeviceService;

@RestController
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * GET /devices
     * Khop dung frontend DeviceAPI.getDevices() - tra ve mang device truc tiep
     * (khong boc trong {devices: [...]}) vi api.js dang doi body.devices, nen
     * o day tra ve object co field "devices" de dung format.
     */
    @GetMapping("/devices")
    public java.util.Map<String, List<DeviceResponse>> getDevices() {
        return java.util.Map.of("devices", deviceService.getAllDevices());
    }

    /**
     * PATCH /devices/{deviceId}
     * Body: {"status": "on"|"off"}
     * deviceId la "light" hoac "fan" (logical id, khop voi mockData.devices).
     */
    @PatchMapping("/devices/{deviceId}")
    public SimpleResponse setDeviceStatus(
            @PathVariable String deviceId,
            @Valid @RequestBody SetDeviceStatusRequest request
    ) {
        return deviceService.setDeviceStatus(deviceId, request.status());
    }
}
