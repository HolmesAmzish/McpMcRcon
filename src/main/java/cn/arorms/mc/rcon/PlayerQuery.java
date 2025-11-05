package cn.arorms.mc.rcon;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * MCP Tool: 查询 Minecraft 服务器在线玩家列表
 * 使用 RCON 协议，通过 RconHandler 执行 list 命令并解析结果
 */
@Service
@RequiredArgsConstructor
public class PlayerQuery {

    private final RconHandler rconHandler;

    /**
     * 查询当前在线玩家列表
     * @return 格式化后的玩家信息（含人数、最大人数、玩家名）
     */
    @Tool(description = "Check player list of Minecraft Server, 查询我的世界服务器玩家列表。返回在线人数、最大人数和玩家名称列表。")
    public String listPlayers() {
        try {
            return rconHandler.query("list");
        } catch (Exception e) {
            return "错误：无法连接到 Minecraft 服务器 RCON。原因: " + e.getMessage() +
                    "\n请检查服务器是否开启 RCON、密码是否正确、网络是否通畅。";
        }
    }
}