package com.extendedclip.papi.expansion.pinger;

import me.clip.placeholderapi.expansion.Cacheable;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PingerExpansion extends PlaceholderExpansion implements Cacheable, Taskable, Configurable {
    private BukkitTask pingTask = null;

    private String online = "&aOnline";

    private String offline = "&cOffline";

    private final Map<String, Pinger> servers = new ConcurrentHashMap<>();

    private final Map<String, InetSocketAddress> toPing = new ConcurrentHashMap<>();

    private int interval = 60;

    public Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("check_interval", Integer.valueOf(30));
        defaults.put("online", "&aOnline");
        defaults.put("offline", "&cOffline");
        return defaults;
    }

    public void start() {
        String on = getString("online", "&aOnline");
        this.online = (on != null) ? on : "&aOnline";
        String off = getString("offline", "&cOffline");
        this.offline = (off != null) ? off : "&cOffline";
        int time = getInt("check_interval", 60);
        if (time > 0)
            this.interval = time;
        this.pingTask = (new BukkitRunnable() {
            public void run() {
                if (PingerExpansion.this.toPing.isEmpty())
                    return;
                for (Map.Entry<String, InetSocketAddress> address : PingerExpansion.this.toPing.entrySet()) {
                    PingerExpansion.Pinger r;
                    try {
                        r = new PingerExpansion.Pinger(address.getValue().getHostName(), address.getValue().getPort());
                        if (r.fetchData()) {
                            PingerExpansion.this.servers.put(address.getKey(), r);
                            continue;
                        }
                        if (PingerExpansion.this.servers.containsKey(address.getKey()))
                            PingerExpansion.this.servers.remove(address.getKey());
                    } catch (Exception exception) {
                    }
                }
            }
        }).runTaskTimerAsynchronously(getPlaceholderAPI(), 20L, 20L * this.interval);
    }

    public void stop() {
        try {
            this.pingTask.cancel();
        } catch (Exception exception) {
        }
        this.pingTask = null;
    }

    public void clear() {
        this.servers.clear();
        this.toPing.clear();
    }

    public boolean canRegister() {
        return true;
    }

    public String getAuthor() {
        return "clip";
    }

    public String getIdentifier() {
        return "pinger";
    }

    public String getPlugin() {
        return null;
    }

    public String getVersion() {
        return "1.0.1";
    }

    public String onPlaceholderRequest(Player p, String identifier) {
        int place = identifier.indexOf("_");
        if (place == -1)
            return null;
        String type = identifier.substring(0, place);
        String address = identifier.substring(place + 1);
        Pinger r = null;
        for (String a : this.servers.keySet()) {
            if (a.equalsIgnoreCase(address)) {
                r = this.servers.get(a);
                break;
            }
        }
        if (r == null)
            if (!this.toPing.containsKey(address)) {
                int port = 25565;
                String add = address;
                if (address.contains(":")) {
                    add = address.substring(0, address.indexOf(":"));
                    try {
                        port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
                    } catch (Exception exception) {
                    }
                }
                this.toPing.put(address, new InetSocketAddress(add, port));
            }
        if (type.equalsIgnoreCase("motd"))
            return (r != null) ? r.getMotd() : "";
        if (type.equalsIgnoreCase("count") || type.equalsIgnoreCase("players"))
            return (r != null) ? String.valueOf(r.getPlayersOnline()) : "0";
        if (type.equalsIgnoreCase("max") || type.equalsIgnoreCase("maxplayers"))
            return (r != null) ? String.valueOf(r.getMaxPlayers()) : "0";
        if (type.equalsIgnoreCase("pingversion") || type.equalsIgnoreCase("pingv"))
            return (r != null) ? String.valueOf(r.getPingVersion()) : "-1";
        if (type.equalsIgnoreCase("gameversion") || type.equalsIgnoreCase("version"))
            return (r != null && r.getGameVersion() != null) ? r.getGameVersion() : "";
        if (type.equalsIgnoreCase("online") || type.equalsIgnoreCase("isonline"))
            return (r != null) ? this.online : this.offline;
        return null;
    }

    public final class Pinger {
        private String address = "localhost";

        private int port = 25565;

        private int timeout = 2000;

        private int pingVersion = -1;

        private int protocolVersion = -1;

        private String gameVersion;

        private String motd;

        private int playersOnline = -1;

        private int maxPlayers = -1;

        public Pinger(String address, int port) {
            setAddress(address);
            setPort(port);
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return this.address;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getPort() {
            return this.port;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        public int getTimeout() {
            return this.timeout;
        }

        private void setPingVersion(int pingVersion) {
            this.pingVersion = pingVersion;
        }

        public int getPingVersion() {
            return this.pingVersion;
        }

        private void setProtocolVersion(int protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        public int getProtocolVersion() {
            return this.protocolVersion;
        }

        private void setGameVersion(String gameVersion) {
            this.gameVersion = gameVersion;
        }

        public String getGameVersion() {
            return this.gameVersion;
        }

        private void setMotd(String motd) {
            this.motd = motd;
        }

        public String getMotd() {
            return this.motd;
        }

        private void setPlayersOnline(int playersOnline) {
            this.playersOnline = playersOnline;
        }

        public int getPlayersOnline() {
            return this.playersOnline;
        }

        private void setMaxPlayers(int maxPlayers) {
            this.maxPlayers = maxPlayers;
        }

        public int getMaxPlayers() {
            return this.maxPlayers;
        }

        public boolean fetchData() {
            try {
                Socket socket = new Socket();
                socket.setSoTimeout(this.timeout);
                socket.connect(
                        new InetSocketAddress(getAddress(), getPort()),
                        getTimeout());
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream,
                        StandardCharsets.UTF_16BE);

                // Send server list ping request, in 1.4-1.5 format
                // See https://wiki.vg/Server_List_Ping#1.4_to_1.5
                dataOutputStream.write(0xFE);
                dataOutputStream.write(0x01);

                // Then, read the ping response (a kick packet)
                // Beta 1.8 - 1.3 servers should respond with this format: https://wiki.vg/Server_List_Ping#Beta_1.8_to_1.3
                // 1.4+ servers should respond with this format: https://wiki.vg/Server_List_Ping#1.4_to_1.5
                // which uses the same response format as: https://wiki.vg/Server_List_Ping#1.6

                // Read packet ID field (1 byte), should be 0xFF (kick packet ID)
                int packetId = inputStream.read();
                if (packetId == -1) {
                    try {
                        socket.close();
                    } catch (IOException iOException) {
                    }
                    socket = null;
                    return false;
                }
                if (packetId != 0xFF) {
                    try {
                        socket.close();
                    } catch (IOException iOException) {
                    }
                    socket = null;
                    return false;
                }

                // Read string length field (2 bytes)
                int length = inputStreamReader.read();
                if (length == -1) {
                    try {
                        socket.close();
                    } catch (IOException iOException) {
                    }
                    socket = null;
                    return false;
                }
                if (length == 0) {
                    try {
                        socket.close();
                    } catch (IOException iOException) {
                    }
                    socket = null;
                    return false;
                }

                // Read string (length bytes)
                char[] chars = new char[length];
                if (inputStreamReader.read(chars, 0, length) != length) {
                    try {
                        socket.close();
                    } catch (IOException iOException) {
                    }
                    socket = null;
                    return false;
                }
                String string = new String(chars);

                // Read the fields of the string
                if (string.startsWith("§")) {
                    // If the string starts with '§', the server is probably running 1.4+
                    // See https://wiki.vg/Server_List_Ping#1.4_to_1.5
                    // and https://wiki.vg/Server_List_Ping#1.6

                    // In this format, fields are delimited by '\0' characters
                    String[] data = string.split("\0");
                    setPingVersion(Integer.parseInt(data[0].substring(1)));
                    setProtocolVersion(Integer.parseInt(data[1]));
                    setGameVersion(data[2]);
                    setMotd(data[3]);
                    setPlayersOnline(Integer.parseInt(data[4]));
                    setMaxPlayers(Integer.parseInt(data[5]));
                } else {
                    // If the string doesn't start with '§', the server is probably running Beta 1.8 - 1.3
                    // See https://wiki.vg/Server_List_Ping#Beta_1.8_to_1.3

                    // In this format, fields are delimited by '§' characters
                    String[] data = string.split("§");
                    setMotd(data[0]);
                    setPlayersOnline(Integer.parseInt(data[1]));
                    setMaxPlayers(Integer.parseInt(data[2]));
                }
                dataOutputStream.close();
                outputStream.close();
                inputStreamReader.close();
                inputStream.close();
                socket.close();
            } catch (SocketException exception) {
                return false;
            } catch (IOException exception) {
                return false;
            }
            return true;
        }
    }
}
