package yolohome.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import yolohome.backend.entity.SensorReading;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long>,
        JpaSpecificationExecutor<SensorReading> {

    /**
     * Danh sach device_id duy nhat, kem thoi diem reading gan nhat cua tung device.
     * Dung projection interface (SensorReadingRepository.DeviceSummary) de tra ve gon nhe.
     */
    @Query("""
            select r.deviceId as deviceId, max(r.recordedAt) as lastSeen
            from SensorReading r
            group by r.deviceId
            order by max(r.recordedAt) desc
            """)
    List<DeviceSummary> findDeviceSummaries();

    /**
     * Reading moi nhat cua 1 device cu the.
     */
    Optional<SensorReading> findFirstByDeviceIdOrderByRecordedAtDesc(String deviceId);

    interface DeviceSummary {
        String getDeviceId();
        OffsetDateTime getLastSeen();
    }
}
