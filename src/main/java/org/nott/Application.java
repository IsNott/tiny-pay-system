package org.nott;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = {"org.nott.mapper","org.nott.mybatis.mapper"})
@SpringBootApplication(scanBasePackages = {"org.nott"})
public class Application {
    public static void main(String[] args) {

        SpringApplication.run(Application.class,args);
    }
}