package yolohome.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yolohome.backend.entity.AppSettings;

public interface AppSettingsRepository extends JpaRepository<AppSettings, Short> {
}
