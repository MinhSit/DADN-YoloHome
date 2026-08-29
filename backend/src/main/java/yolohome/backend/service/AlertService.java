package yolohome.backend.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import yolohome.backend.dto.AlertResponse;
import yolohome.backend.entity.Alert;
import yolohome.backend.exception.ResourceNotFoundException;
import yolohome.backend.repository.AlertRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;

    public void createAlert(String level, String message, String deviceId) {
        Alert alert = new Alert();
        alert.setLevel(level);
        alert.setMessage(message);
        alert.setDeviceId(deviceId);
        repository.save(alert);
    }

    public List<AlertResponse> getAllAlerts() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(AlertResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long alertId) {
        Alert alert = repository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay alert id: " + alertId));
        alert.setRead(true);
        repository.save(alert);
    }
}
