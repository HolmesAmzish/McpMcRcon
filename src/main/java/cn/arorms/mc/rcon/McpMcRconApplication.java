package cn.arorms.mc.rcon;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class McpMcRconApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpMcRconApplication.class, args);
    }


    @Bean
    public ToolCallbackProvider listTools(ListService listService) {
        return  MethodToolCallbackProvider.builder().toolObjects(listService).build();
    }

}
