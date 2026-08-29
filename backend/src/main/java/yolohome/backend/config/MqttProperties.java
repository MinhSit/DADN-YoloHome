package yolohome.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    /** Vi du: ssl://xxxxx.s1.eu.hivemq.cloud:8883 hoac tcp://localhost:1883 */
    private String brokerUri;

    private String clientId;

    private String username;

    private String password;

    /** Placeholder {deviceId} se duoc thay the luc publish, vi du: yolohome/{deviceId}/command */
    private String commandTopicPattern;

    private String configTopicPattern;
    
    private String manualTopicPattern;

    private int connectionTimeoutSeconds = 10;

    public String resolveCommandTopic(String deviceId) {
        return commandTopicPattern.replace("{deviceId}", deviceId);
    }
    public String resolveConfigTopic(String deviceId) {
        return configTopicPattern.replace("{deviceId}", deviceId);
    }

    public String resolveManualTopic(String deviceId) {
        return manualTopicPattern.replace("{deviceId}", deviceId);
    }
}
