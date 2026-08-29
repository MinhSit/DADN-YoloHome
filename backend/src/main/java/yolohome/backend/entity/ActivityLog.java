package yolohome.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "log_type", nullable = false, length = 32)
    private String logType; // Device | Alert | System

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 64)
    private String device;

    @Column(nullable = false, length = 32)
    private String status; // Success | Warning | Critical

    @PrePersist
    public void prePersist() {
        if (occurredAt == null) {
            occurredAt = OffsetDateTime.now();
        }
    }
}
