package yolohome.backend.dto;

import jakarta.validation.constraints.AssertTrue;

/**
 * Body cho POST /api/devices/{deviceId}/command.
 * Chi can gui field nao muon thay doi, field con lai de null se khong duoc dua vao lenh.
 * Vi du: {"ledState": true}  -> chi bat LED, khong dong cham den fan.
 */
public record DeviceCommandRequest(
        Boolean ledState,
        Boolean fanState
) {
    @AssertTrue(message = "Phai co it nhat 1 trong 2 field ledState hoac fanState")
    public boolean isAtLeastOneFieldPresent() {
        return ledState != null || fanState != null;
    }
}
