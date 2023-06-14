package com.example.vehicle_device_simulator.Controller;

import com.example.vehicle_device_simulator.DTO.EmergencyActionDTOForController;
import com.example.vehicle_device_simulator.Service.LocationService;
import com.example.vehicle_device_simulator.Service.StateService;
import com.example.vehicle_device_simulator.Service.VehicleLocker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
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
    public ResponseEntity<String> performEmergencyAction(@RequestBody EmergencyActionDTOForController emergencyActionDTO){

        if(Objects.equals(emergencyActionDTO.getAction(), "LIMP_MODE")) {
            if(emergencyActionDTO.getReason().equals("SUSPICIOUS_ACTIVITY")) {
                System.out.println("Suspicious activity reported on:\n"+
                        emergencyActionDTO.getTime()+
                        ".\n Please pull over. Your vehicle will enter limp mode soon.");
                StateService.setState(emergencyActionDTO.getAction());
                StateService.setReason(emergencyActionDTO.getReason());
                StateService.setIssueTime(LocalDateTime.parse(emergencyActionDTO.getTime()));
            }
            return ResponseEntity.ok("SUCCESS");
        }
        else{
            if(Objects.equals(emergencyActionDTO.getAction(), "NORMAL")){
                StateService.setState(emergencyActionDTO.getAction());
                StateService.setReason(emergencyActionDTO.getReason());
                StateService.setIssueTime(LocalDateTime.parse(emergencyActionDTO.getTime()));
                return ResponseEntity.ok("SUCCESS");

            }else {
                return ResponseEntity.ok("INVALID_ACTION");
            }
        }
    }

}
