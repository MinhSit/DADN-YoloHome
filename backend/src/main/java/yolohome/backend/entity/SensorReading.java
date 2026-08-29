package yolohome.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Map 1:1 voi table sensor_readings da duoc tao san boi script SQL / Python MQTT listener.
 * ddl-auto=validate nen entity phai khop chinh xac voi schema hien co, khong duoc tu sinh/sua.
 */
@Entity
@Table(name = "sensor_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    @Column(name = "temperature", nullable = false)
    private Float temperature;

    @Column(name = "humidity", nullable = false)
    private Float humidity;

    @Column(name = "light", nullable = false)
    private Integer light;

    @Column(name = "led_state", nullable = false)
    private Boolean ledState;

    @Column(name = "fan_state", nullable = false)
    private Boolean fanState;
}
