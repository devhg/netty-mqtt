package cn.sdutcs.mqtt.broker;

import cn.sdutcs.mqtt.broker.config.BrokerConfig;
import cn.sdutcs.mqtt.broker.server.BrokerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "cn.sdutcs.mqtt")
public class App implements CommandLineRunner {
    @Autowired
    BrokerConfig config;
    @Autowired
    BrokerServer server;

    @Override
    public void run(String... args) throws Exception {
        server.start();
    }

    public static void main(String[] args) {
        // 获取SpringBoot容器
        ConfigurableApplicationContext applicationContext = SpringApplication.run(App.class, args);

        // 从Spring容器中获取指定的对象
        // BrokerConfig brokerConfig = applicationContext.getBean(BrokerConfig.class);
        // System.out.println(brokerConfig);

        // 手动创建
        // ApplicationContext ctx = new AnnotationConfigApplicationContext(BrokerConfig.class);
        // BrokerConfig dao2 = ctx.getBean(BrokerConfig.class);
        // System.out.println(dao2.getPort());

        // String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        // for (String beanName : beanDefinitionNames) {
        //     System.out.println("beanName: " + beanName);
        // }
    }
}
