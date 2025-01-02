package com.yhkim.fave;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan(basePackages = "com.yhkim.fave.mappers")
public class FaveApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaveApplication.class, args);
    }

}
