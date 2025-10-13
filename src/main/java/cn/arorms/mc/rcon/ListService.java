package cn.arorms.mc.rcon;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple Minecraft RCON client (pure Java, no deps).
 * - Authenticates with RCON
 * - Sends "list" command
 * - Collects/merges response packets
 * - Prints raw response and extracts online/max/players
 * 
 * Notes:
 * - RCON uses little-endian for int32 fields.
 * - Packet structure:
 *   size:int32LE (size of id+type+body+2 null)
 *   id:int32LE
 *   type:int32LE
 *   body:bytes
 *   2x null bytes
 */
@Service
public class ListService {
    // ---------- CONFIG ----------
    static final String HOST = "127.0.0.1";   
    static final int PORT = 25575;            
    static final String PASSWORD = "20230612";
    static final int SO_TIMEOUT_MS = 5000;    
    // ----------------------------

    // RCON types
    static final int SERVERDATA_RESPONSE_VALUE = 0;
    static final int SERVERDATA_EXECCOMMAND = 2;
    static final int SERVERDATA_AUTH = 3;

    @Tool(description = "Get the player list of server")
    public String doRconList(String host, int port, String password) throws Exception {
        try (Socket sock = new Socket()) {
            sock.connect(new InetSocketAddress(host, port), SO_TIMEOUT_MS);
            sock.setSoTimeout(SO_TIMEOUT_MS);

            try (DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                 DataInputStream in = new DataInputStream(sock.getInputStream())) {
                // 1) AUTH
                int authId = newRandomId();
                byte[] authPacket = makePacket(authId, SERVERDATA_AUTH, password.getBytes("UTF-8"));
                out.write(authPacket);
                out.flush();

                // read auth response
                Packet authResp = readPacket(in);
                // Authentication failure if response id == -1
                if (authResp.id == -1) {
                    throw new SecurityException("RCON authentication failed (id == -1). Check password.");
                }
                // Some servers send an empty response packet and then the auth response; we handled first response.

                // 2) send "list" command
                int cmdId = newRandomId();
                byte[] cmdPacket = makePacket(cmdId, SERVERDATA_EXECCOMMAND, "list".getBytes("UTF-8"));
                out.write(cmdPacket);
                out.flush();

                // 3) collect responses - servers may send one or more packets
                // We'll read for a short period collecting payloads that match cmdId or type=response
                StringBuilder sb = new StringBuilder();
                long start = System.currentTimeMillis();
                // We will loop until we either get matching id payload and then no more incoming data (short wait),
                // or timeout overall.
                while (true) {
                    Packet p;
                    try {
                        p = readPacket(in);
                    } catch (SocketTimeoutException ste) {
                        // no more data in reasonable time
                        break;
                    }
                    // append payload if any
                    if (p.payload != null && p.payload.length > 0) {
                        sb.append(new String(p.payload, "UTF-8"));
                    }
                    // Heuristic stop: if we have appended something and last read was more than 200ms ago, break.
                    if (sb.length() > 0 && (System.currentTimeMillis() - start) > 200) {
                        // small pause done, break to avoid blocking further
                        break;
                    }
                    // update timer to avoid infinite loop
                    if ((System.currentTimeMillis() - start) > (SO_TIMEOUT_MS * 2)) {
                        break;
                    }
                }
                return sb.toString();
            }
        }
    }

    // Build RCON packet bytes
    static byte[] makePacket(int id, int type, byte[] body) throws IOException {
        if (body == null) body = new byte[0];
        // payload = id (4) + type (4) + body + 2 null bytes
        int payloadLen = 4 + 4 + body.length + 2;
        ByteBuffer buf = ByteBuffer.allocate(4 + payloadLen);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(payloadLen); // size
        buf.putInt(id);
        buf.putInt(type);
        buf.put(body);
        buf.put((byte) 0x00);
        buf.put((byte) 0x00);
        return buf.array();
    }

    // Read one packet from stream (blocking, expects size header available)
    static Packet readPacket(DataInputStream in) throws IOException {
        // read size (int32 little-endian)
        byte[] sizeBytes = new byte[4];
        in.readFully(sizeBytes);
        ByteBuffer sb = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN);
        int size = sb.getInt();

        if (size < 10 || size > 10_000_000) {
            // sanity check: minimum packet contains id(4)+type(4)+2 null = 10
            // if size abnormal, still try to proceed but be cautious
            // throw new IOException("Invalid packet size: " + size);
        }

        byte[] rest = new byte[size];
        in.readFully(rest);

        ByteBuffer rb = ByteBuffer.wrap(rest).order(ByteOrder.LITTLE_ENDIAN);
        int id = rb.getInt();
        int type = rb.getInt();
        byte[] payload = new byte[size - 8];
        if (payload.length > 0) {
            rb.get(payload);
            // strip trailing nulls if present
            if (payload.length >= 2 && payload[payload.length - 1] == 0 && payload[payload.length - 2] == 0) {
                int newLen = payload.length - 2;
                byte[] trimmed = new byte[newLen];
                System.arraycopy(payload, 0, trimmed, 0, newLen);
                payload = trimmed;
            }
        } else {
            payload = new byte[0];
        }
        return new Packet(id, type, payload);
    }

    static int newRandomId() {
        return new Random().nextInt(Integer.MAX_VALUE - 1) + 1;
    }

    static class Packet {
        int id;
        int type;
        byte[] payload;
        Packet(int id, int type, byte[] payload) {
            this.id = id;
            this.type = type;
            this.payload = payload;
        }
    }
}
