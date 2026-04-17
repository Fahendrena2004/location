package org.example.location_voiture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LocationVoitureApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocationVoitureApplication.class, args);
    }

}
