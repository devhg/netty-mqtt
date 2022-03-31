package cn.sdutcs.mqtt.panel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.sdutcs.mqtt.panel")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
