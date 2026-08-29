package yolohome.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "device_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceState {

    @Id
    @Column(name = "device_id", length = 64)
    private String deviceId;

    @Column(nullable = false)
    private String name;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "led_state", nullable = false)
    private Boolean ledState;

    @Column(name = "fan_state", nullable = false)
    private Boolean fanState;

    @Column(nullable = false)
    private Boolean online;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        updatedAt = OffsetDateTime.now();
    }
}
