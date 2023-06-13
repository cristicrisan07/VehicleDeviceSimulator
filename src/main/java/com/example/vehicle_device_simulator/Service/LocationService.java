package com.example.vehicle_device_simulator.Service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@Service
public class LocationService {

    private static Double latitude;
    private static Double longitude;
    private static boolean sendLocation = true;
    public static void startSendingLocationToServer(String vin){
        WebClient client = WebClient.create();
        try {
            String getResponse = client.post()
                    .uri(new URI("http://localhost:8080/vsss/vehicle/getCurrentLocation"))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(vin)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(getResponse!=null) {
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(getResponse);
                latitude = (double) jsonObject.get("lat");
                longitude = (double) jsonObject.get("lng");
                Random random = new Random();
                setSendLocation(true);
                System.out.println("Location sending started");
                while(sendLocation) {
                    Thread.sleep(30000L);
                    latitude += ((random.nextDouble()*10)%3-1) * (random.nextDouble(0.001)*10);
                    longitude += ((random.nextDouble()*10)%3-1) * (random.nextDouble(0.001)*10);
                    var locationToSend = new JSONObject();
                    locationToSend.put("lng",longitude);
                    locationToSend.put("lat",latitude);
                    var vehicleControllerDTO = new JSONObject();
                    vehicleControllerDTO.put("vin",StateService.getVin());
                    vehicleControllerDTO.put("location",locationToSend.toString());
                    vehicleControllerDTO.put("time", LocalDateTime.now().toString());
                    String setResponse = client.post()
                            .uri(new URI("http://localhost:8080/vsss/vehicle/setCurrentLocation"))
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON)
                            .bodyValue(vehicleControllerDTO)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    if(!Objects.equals(setResponse,"SUCCESS")){
                        if(Objects.equals(setResponse,"NOT_RENTED")){
                            setSendLocation(false);
                            String status = VehicleLocker.closeConnection();
                            if(Objects.equals(status,"SUCCESS")){
                                System.out.println(status);
                            }
                        }else {
                            System.out.println("Could not set the vehicle location on the server");
                        }
                    }
                }
                System.out.println("Location sending ended");

            }else{
                System.out.println("Could not fetch the vehicle location from the server");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (WebClientResponseException e){
            System.out.println(e.getMessage());
        } catch (ParseException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isSendLocation() {
        return sendLocation;
    }

    public static void setSendLocation(boolean sendLocation) {
        LocationService.sendLocation = sendLocation;
    }
}

