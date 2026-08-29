package yolohome.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import yolohome.backend.repository.DeviceStateRepository;

/**
 * Lang nghe kenh Postgres 'sensor_reading_inserted' (duoc bao boi trigger SQL
 * trong sql/002_app_tables_and_trigger.sql moi khi co INSERT vao sensor_readings).
 * Dung 1 JDBC connection RIENG BIET (khong qua Hikari pool cua JPA) vi LISTEN
 * can giu 1 connection song lien tuc va poll notification dinh ky - khac han
 * cach dung connection ngan han, tra lai pool ngay cua JPA/Hibernate.
 *
 * Khi nhan duoc reading moi, so sanh voi nguong trong app_settings, neu vuot
 * thi tu dong tao Alert + ActivityLog - khong can frontend hay backend nao
 * khac phai polling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorNotificationListener {

    private static final String CHANNEL = "sensor_reading_inserted";

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String jdbcUser;

    @Value("${spring.datasource.password}")
    private String jdbcPassword;

    private final AlertService alertService;
    private final ActivityLogService activityLogService;
    private final SettingsService settingsService;

    private final DeviceStateRepository deviceStateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "pg-listen-sensor-readings");
        t.setDaemon(true);
        return t;
    });

    private volatile Connection listenerConnection;

    @PostConstruct
    public void start() {
        scheduler.execute(this::connectAndListenLoop);
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
        closeQuietly();
    }

    private void connectAndListenLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ensureConnected();
                pollNotifications();
                Thread.sleep(1000); // poll moi 1s - PGConnection.getNotifications() la non-blocking
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.warn("Mat ket noi LISTEN toi Postgres, se thu lai sau 5s: {}", e.getMessage());
                closeQuietly();
                sleepQuietly(5000);
            }
        }
    }

    private void ensureConnected() throws Exception {
        if (listenerConnection != null && !listenerConnection.isClosed()) {
            return;
        }
        log.info("Ket noi LISTEN toi Postgres cho kenh '{}'", CHANNEL);
        listenerConnection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
        try (Statement stmt = listenerConnection.createStatement()) {
            stmt.execute("LISTEN " + CHANNEL);
        }
    }

    private void pollNotifications() throws Exception {
        PGConnection pgConnection = listenerConnection.unwrap(PGConnection.class);
        PGNotification[] notifications = pgConnection.getNotifications(0);

        if (notifications == null) {
            return;
        }

        for (PGNotification notification : notifications) {
            handlePayload(notification.getParameter());
        }
    }

    private void handlePayload(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String deviceId = node.path("device_id").asText();
            float temperature = (float) node.path("temperature").asDouble();
            float humidity = (float) node.path("humidity").asDouble();

            boolean ledState = node.path("led_state").asBoolean();
            boolean fanState = node.path("fan_state").asBoolean();

            deviceStateRepository.findById(deviceId).ifPresentOrElse(
                    state -> {
                        state.setLedState(ledState);
                        state.setFanState(fanState);
                        deviceStateRepository.save(state);

                        log.info(
                                "Dong bo actual device state tu telemetry: device={}, led={}, fan={}",
                                deviceId,
                                ledState,
                                fanState
                        );
                    },
                    () -> log.warn(
                            "Khong tim thay device_state cho device {}",
                            deviceId
                    )
            );

            var thresholds = settingsService.getThresholds();

            if (temperature > thresholds.temperature()) {
                String msg = String.format("Temperature above threshold (%.1f°C)", temperature);
                alertService.createAlert("Critical", msg, deviceId);
                activityLogService.log("Alert", msg, "Sensor", "Critical");
            }

            if (humidity > thresholds.humidity()) {
                String msg = String.format("Humidity above threshold (%.1f%%)", humidity);
                alertService.createAlert("Warning", msg, deviceId);
                activityLogService.log("Alert", msg, "Sensor", "Warning");
            }

        } catch (Exception e) {
            log.error("Loi xu ly notification tu Postgres: {}", e.getMessage(), e);
        }
    }

    private void closeQuietly() {
        if (listenerConnection != null) {
            try {
                listenerConnection.close();
            } catch (Exception ignored) {
            }
            listenerConnection = null;
        }
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
