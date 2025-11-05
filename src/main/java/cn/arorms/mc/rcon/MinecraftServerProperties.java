package cn.arorms.mc.rcon;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "minecraft.server")
@Component
@Data
public class MinecraftServerProperties {
    private String address;
    private int rconPort;
    private String rconPassword;
}