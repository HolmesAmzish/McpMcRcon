package cn.arorms.mc.rcon;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Query the status of players in server
 * @version 1.1 2025-11-09
 * @author Cacciatore
 */
@Service
public class PlayerStatusTools {

    private final RconHandler rconHandler;
    
    @Autowired
    public PlayerStatusTools(RconHandler rconHandler) {
        this.rconHandler = rconHandler;
    }

    /**
     * Query the list of server players
     * @return the number of online players and their names
     */
    @Tool(description = "Check player list of Minecraft Server.")
    public String listPlayers() {
        try {
            return rconHandler.query("list");
        } catch (Exception e) {
            return "Error, can not connect to rcon server: " + e.getMessage();
        }
    }
}