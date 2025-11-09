package cn.arorms.mc.rcon;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReconTest {

    public static void main(String[] args) throws Exception {
        RconHandler rconHandler = new RconHandler("192.168.0.190", 25575, "20230612");
        String reuslt = rconHandler.query("list");
        System.out.println(reuslt);
    }
}