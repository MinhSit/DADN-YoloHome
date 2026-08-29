package yolohome.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.LogResponse;
import yolohome.backend.service.ActivityLogService;

@RestController
@RequiredArgsConstructor
public class LogController {

    private final ActivityLogService activityLogService;

    /**
     * GET /logs
     * Khop dung frontend LogAPI.getLogs() - doi body.logs.
     */
    @GetMapping("/logs")
    public Map<String, List<LogResponse>> getLogs() {
        return Map.of("logs", activityLogService.getAllLogs());
    }
}
