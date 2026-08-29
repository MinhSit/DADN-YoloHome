package yolohome.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yolohome.backend.entity.DeviceState;

public interface DeviceStateRepository extends JpaRepository<DeviceState, String> {
}
