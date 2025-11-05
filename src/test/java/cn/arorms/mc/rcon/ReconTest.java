package cn.arorms.mc.rcon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
public class ReconTest {

    public static void main(String[] args) {
        // 1. 启动 Spring 容器
        ConfigurableApplicationContext context = SpringApplication.run(ReconTest.class, args);

        // 2. 获取 Spring 管理的 RconHandler Bean
        RconHandler handler = context.getBean(RconHandler.class);

        try {
            // 3. 调用实例方法
            String result = handler.query("list");
            System.out.println("RCON Response:");
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("RCON 连接失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 4. 关闭上下文
            context.close();
        }
    }
}