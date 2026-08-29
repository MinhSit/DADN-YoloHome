package yolohome.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.AlertResponse;
import yolohome.backend.dto.SimpleResponse;
import yolohome.backend.service.AlertService;

@RestController
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /alerts
     * Khop dung frontend AlertAPI.getAlerts() - doi body.alerts.
     */
    @GetMapping("/alerts")
    public Map<String, List<AlertResponse>> getAlerts() {
        return Map.of("alerts", alertService.getAllAlerts());
    }

    /**
     * PATCH /alerts/{alertId}/read
     */
    @PatchMapping("/alerts/{alertId}/read")
    public SimpleResponse markAsRead(@PathVariable Long alertId) {
        alertService.markAsRead(alertId);
        return new SimpleResponse(true, "Alert marked as read.");
    }
}
