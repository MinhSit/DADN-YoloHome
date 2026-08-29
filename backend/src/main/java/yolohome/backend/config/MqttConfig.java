package yolohome.backend.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    /**
     * Tao 1 MqttClient duy nhat cho toan bo app (singleton), chua connect ngay khi khoi dong -
     * viec connect that su duoc MqttCommandPublisher lo, va se retry moi lan can publish
     * neu lan truoc bi mat ket noi. Lam vay de app van khoi dong binh thuong ke ca khi
     * MQTT broker dang tam thoi khong toi duoc (khong lam sap toan bo backend chi vi MQTT).
     */
    @Bean
    public MqttClient mqttClient(MqttProperties props) throws Exception {
        String clientId = props.getClientId() + "-" + System.currentTimeMillis();
        return new MqttClient(props.getBrokerUri(), clientId, new MemoryPersistence());
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions(MqttProperties props) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(props.getConnectionTimeoutSeconds());
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            options.setUserName(props.getUsername());
        }
        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            options.setPassword(props.getPassword().toCharArray());
        }
        return options;
    }
}
