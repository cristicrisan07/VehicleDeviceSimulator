package com.example.vehicle_device_simulator.DTO;

public class VehicleControllerStateDTO {
    private String token;
    private EmergencyActionDTOForController emergency;

    public VehicleControllerStateDTO(String token, EmergencyActionDTOForController emergency) {
        this.token = token;
        this.emergency = emergency;
    }

    public VehicleControllerStateDTO() {
    }

    public String getToken() {
        return token;
    }

    public EmergencyActionDTOForController getEmergency() {
        return emergency;
    }
}
