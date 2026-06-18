package com.rent1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class RentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentManagementApplication.class, args);
    }
}
