package cn.arorms.mc.rcon;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "minecraft.server")
@Component
public record ServerProperties() {
    private static String address;
    private static int rconPort;
    private static String rconPassword;
    
    public String getAddress() {
        return address;
    }

    public int getRconPort() {
        return rconPort;
    }

    public String getRconPassword() {
        return rconPassword;
    }
}