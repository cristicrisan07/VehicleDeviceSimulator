package com.example.vehicle_device_simulator.Service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class StateService {

    private static String vin = null;
    public static String getState() {
        WebClient client = WebClient.create();
        try {
            return client.post()
                    .uri(new URI("http://localhost:8080/vsss/vehicle/getCurrentRentalSession"))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(vin)
                    .retrieve()
                    .bodyToMono(String.class)
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
}
