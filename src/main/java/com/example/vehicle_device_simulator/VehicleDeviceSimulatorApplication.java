package com.example.vehicle_device_simulator;

import com.example.vehicle_device_simulator.Service.LocationService;
import com.example.vehicle_device_simulator.Service.StateService;
import com.example.vehicle_device_simulator.Service.VehicleLocker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import static com.example.vehicle_device_simulator.Service.LocationService.startSendingLocationToServer;

@SpringBootApplication
public class VehicleDeviceSimulatorApplication {

    public static void main(String[] args) {

        StateService.setVin(args[1]);
        Arrays.stream(args).toList().forEach(System.out::println);
        String status = StateService.getState();
        if(!Objects.equals(status,"NOT_RENTED")) {
            try {
                VehicleLocker.openService(status);
                new Thread(()->LocationService.startSendingLocationToServer(StateService.getVin())).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        SpringApplication app = new SpringApplication(VehicleDeviceSimulatorApplication.class);
        app.setDefaultProperties(Collections
                .singletonMap("server.port", args[0]));
        app.run(args);
    }
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedOrigins("*")
                        .exposedHeaders("*")
                        .allowedHeaders("*");

            }
        };
    }

}
