package yolohome.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetDeviceStatusRequest(
        @NotBlank @Pattern(regexp = "on|off", message = "status phai la 'on' hoac 'off'") String status
) {
}
