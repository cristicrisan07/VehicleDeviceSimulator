package com.example.vehicle_device_simulator.DTO;

public class EmergencyActionDTOForController {
    private String action;
    private  String reason;
    private  String time;

    public EmergencyActionDTOForController(String action, String reason, String time) {
        this.action = action;
        this.reason = reason;
        this.time = time;
    }

    public EmergencyActionDTOForController() {

    }

    public String getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }

    public String getTime() {
        return time;
    }
}
