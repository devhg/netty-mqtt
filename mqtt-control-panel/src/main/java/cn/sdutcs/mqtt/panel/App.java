package cn.sdutcs.mqtt.panel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.sdutcs.mqtt")
@MapperScan("cn.sdutcs.mqtt.panel")
public class App {
    public static void main(String[] args) {
        try {
            SpringApplication.run(App.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
