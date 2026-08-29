package yolohome.backend.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.DeviceCommandRequest;
import yolohome.backend.dto.DeviceCommandResponse;
import yolohome.backend.dto.DeviceSummaryResponse;
import yolohome.backend.dto.SensorReadingResponse;
import yolohome.backend.service.SensorReadingService;

@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
public class SensorReadingController {

    private final SensorReadingService service;

    /**
     * GET /data/devices
     * Danh sach cac device_id da tung gui du lieu, kem thoi diem gan nhat.
     * Day la API noi bo/mo rong, khac voi GET /devices (dung cho frontend dashboard).
     */
    @GetMapping("/devices")
    public List<DeviceSummaryResponse> listDevices() {
        return service.listDevices();
    }

    /**
     * GET /data/devices/{deviceId}/latest
     * Reading moi nhat cua 1 device - dung cho man hinh dashboard chinh.
     */
    @GetMapping("/devices/{deviceId}/latest")
    public SensorReadingResponse getLatest(@PathVariable String deviceId) {
        return service.getLatestForDevice(deviceId);
    }

    /**
     * GET /data/readings?deviceId=yolohome-01&from=2026-08-01T00:00:00Z&to=2026-08-22T23:59:59Z&page=0&size=20&sort=recordedAt,desc
     * Lich su reading, ho tro filter theo device + khoang thoi gian, phan trang va sap xep.
     */
    @GetMapping("/readings")
    public Page<SensorReadingResponse> searchReadings(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recordedAt,desc") String sort
    ) {
        int safeSize = Math.min(size, 200); // chan client xin qua nhieu record 1 lan
        Pageable pageable = PageRequest.of(page, safeSize, parseSort(sort));
        return service.searchReadings(deviceId, from, to, pageable);
    }

    /**
     * POST /data/devices/{deviceId}/command
     * Body: {"ledState": true} hoac {"fanState": false} hoac ca hai cung luc.
     * Backend publish lenh nay len MQTT broker, Yolo:Bit subscribe topic tuong ung se nhan va thuc thi.
     * Day la API cap thap (chi publish MQTT). Frontend dashboard dung PATCH /devices/{deviceId}
     * (DeviceController) - API do goi lai ham nay ben trong, dong thoi cap nhat device_state va ghi log.
     */
    @PostMapping("/devices/{deviceId}/command")
    public DeviceCommandResponse sendCommand(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceCommandRequest command
    ) {
        return service.sendCommand(deviceId, command);
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        String property = parts.length > 0 ? parts[0] : "recordedAt";
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
