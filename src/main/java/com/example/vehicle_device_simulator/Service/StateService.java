package com.example.vehicle_device_simulator.Service;

import com.example.vehicle_device_simulator.DTO.VehicleControllerStateDTO;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

@Service
public class StateService {

    private static String vin = null;
    private static String state = "NORMAL";
    private static String reason = "NONE";
    private static LocalDateTime issueTime = LocalDateTime.now();
    public static VehicleControllerStateDTO getStateFromRemote() {
        WebClient client = WebClient.create();
        try {
            return client.get()
                    .uri(new URI("http://localhost:8080/vsss/vehicle/getCurrentRentalSession/"+vin))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(VehicleControllerStateDTO.class)
                    .block();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (WebClientResponseException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static String getVin() {
        return vin;
    }

    public static void setVin(String vin) {
        StateService.vin = vin;
    }

    public static void setState(String state) {
        StateService.state = state;
    }

    public static String getReason() {
        return reason;
    }

    public static void setReason(String reason) {
        StateService.reason = reason;
    }

    public static LocalDateTime getIssueTime() {
        return issueTime;
    }

    public static void setIssueTime(LocalDateTime issueTime) {
        StateService.issueTime = issueTime;
    }

    public static String getState() {
        return state;
    }

    public static String getStateAsJSONString(){
        var jsonObject = new JSONObject();
        jsonObject.put("state",state);
        jsonObject.put("reason",reason);
        jsonObject.put("issueTime",issueTime.toString());
        return jsonObject.toJSONString() +"\n";
    }
}
