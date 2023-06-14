package com.example.vehicle_device_simulator.Controller;

import com.example.vehicle_device_simulator.DTO.EmergencyActionDTO;
import com.example.vehicle_device_simulator.Service.LocationService;
import com.example.vehicle_device_simulator.Service.StateService;
import com.example.vehicle_device_simulator.Service.VehicleLocker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

@RestController
public class ServerController {

    @PostMapping("/controller/allowConnection")
    public ResponseEntity<String> allowConnection(@RequestBody String token){
        try {
            VehicleLocker.openService(token);
            new Thread(()->LocationService.startSendingLocationToServer(StateService.getVin())).start();
            return ResponseEntity.ok("SUCCESS");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        }
    }

    @DeleteMapping("/controller/denyConnection")
    public ResponseEntity<String> denyConnection(){

        try {
            String status = VehicleLocker.closeConnection();
            return ResponseEntity.ok(status);
        }catch (IOException e){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
        }
    }

    @PostMapping("/controller/performEmergencyAction")
    public ResponseEntity<String> performEmergencyAction(@RequestBody EmergencyActionDTO emergencyActionDTO){

        if(Objects.equals(emergencyActionDTO.getAction(), "LIMP_MODE")) {
            if(emergencyActionDTO.getReason().equals("SUSPICIOUS_ACTIVITY")) {
                System.out.println("Suspicious activity detected.\n Please pull over. Your vehicle will enter limp mode in 5 minutes.");
            }
            return ResponseEntity.ok("SUCCESS");
        }
        else{
            return ResponseEntity.ok("INVALID_ACTION");
        }
    }

}
