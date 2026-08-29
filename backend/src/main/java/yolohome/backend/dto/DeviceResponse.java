package yolohome.backend.dto;

import yolohome.backend.entity.DeviceState;

/**
 * Khop chinh xac shape ma frontend mockData.devices dang dung:
 * { id: "light"|"fan", name, type: "toggle", status: "on"|"off", online }
 *
 * Yolo:Bit vat ly chi la 1 board (device_state, 1 hang duy nhat: yolohome-01),
 * nhung dieu khien 2 thanh phan doc lap (LED va Fan). De khop dung UI frontend
 * (2 toggle rieng biet "Light" va "Fan"), backend tach 1 device_state thanh
 * 2 "logical device" o tang API - khong can sua gi frontend.
 */
public record DeviceResponse(
        String id,
        String name,
        String type,
        String status,
        boolean online
) {
    public static DeviceResponse light(DeviceState state) {
        return new DeviceResponse(
                "light",
                "Light",
                "toggle",
                Boolean.TRUE.equals(state.getLedState()) ? "on" : "off",
                Boolean.TRUE.equals(state.getOnline())
        );
    }

    public static DeviceResponse fan(DeviceState state) {
        return new DeviceResponse(
                "fan",
                "Fan",
                "toggle",
                Boolean.TRUE.equals(state.getFanState()) ? "on" : "off",
                Boolean.TRUE.equals(state.getOnline())
        );
    }
}
