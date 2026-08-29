package yolohome.backend.exception;

public class MqttPublishException extends RuntimeException {
    public MqttPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
