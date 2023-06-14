package com.example.vehicle_device_simulator.DTO;

public class EmergencyActionDTO {
    private final String action;
    private final String reason;

    public EmergencyActionDTO(String action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public String getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }
}
