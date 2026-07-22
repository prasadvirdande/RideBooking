package com.ridebooking.Payment_services;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
public class PaymentServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServicesApplication.class, args);
    }

}