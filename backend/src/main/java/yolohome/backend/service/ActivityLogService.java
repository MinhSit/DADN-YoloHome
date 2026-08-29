package yolohome.backend.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yolohome.backend.entity.ActivityLog;
import yolohome.backend.repository.ActivityLogRepository;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public void log(String type, String description, String device, String status) {
        ActivityLog entry = new ActivityLog();
        entry.setLogType(type);
        entry.setDescription(description);
        entry.setDevice(device);
        entry.setStatus(status);
        repository.save(entry);
    }

    public java.util.List<yolohome.backend.dto.LogResponse> getAllLogs() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "occurredAt")).stream()
                .map(yolohome.backend.dto.LogResponse::from)
                .toList();
    }
}
