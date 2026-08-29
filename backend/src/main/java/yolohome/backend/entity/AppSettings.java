package yolohome.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppSettings {

    @Id
    private Short id; // luon = 1, chi co 1 hang duy nhat

    @Column(name = "temperature_threshold", nullable = false)
    private Float temperatureThreshold;

    @Column(name = "humidity_threshold", nullable = false)
    private Float humidityThreshold;

    @Column(name = "light_threshold")
    private Float lightThreshold;

    @Column(name = "adafruit_username")
    private String adafruitUsername;

    @Column(name = "adafruit_aio_key")
    private String adafruitAioKey;
}
