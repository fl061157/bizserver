package com.handwin.packet;

import com.handwin.packet.CommandPacket;

import java.util.Arrays;

/**
 * Created by piguangtao on 2014/6/25.
 */
public class GameServerRespPacket extends CommandPacket {
    public final static int COMMAND_GAME_SERVER_RESP_TYPE = 0x19;

    public final static int COMMAND_GAME_SERVER_RESP_PACKET_TYPE = COMMAND_GAME_SERVER_RESP_TYPE * 256 + COMMAND_PACKET_TYPE;

    public GameServerRespPacket() {
        this.setPacketType(COMMAND_GAME_SERVER_RESP_PACKET_TYPE);
    }

    private GameServerInfo[] gameServerInfos;

    public GameServerInfo[] getGameServerInfos() {
        return gameServerInfos;
    }

    public void setGameServerInfos(GameServerInfo[] gameServerInfos) {
        this.gameServerInfos = gameServerInfos;
    }

    @Override
    public void attachThirdUserId(Integer appID) {

    }

    public static class GameServerInfo {
        private int gameId;

        private String ip;

        private short port;

        private byte flag;

        private String roomId;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(short port) {
            this.port = port;
        }

        public byte getFlag() {
            return flag;
        }

        public void setFlag(byte flag) {
            this.flag = flag;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public int getGameId() {
            return gameId;
        }

        public void setGameId(int gameId) {
            this.gameId = gameId;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("GameServerInfo{");
            sb.append("gameId=").append(gameId);
            sb.append(", ip='").append(ip).append('\'');
            sb.append(", port=").append(port);
            sb.append(", flag=").append(flag);
            sb.append(", roomId='").append(roomId).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameServerRespPacket{");
        sb.append("gameServerInfos=").append(Arrays.toString(gameServerInfos));
        sb.append('}');
        return sb.toString();
    }
}
