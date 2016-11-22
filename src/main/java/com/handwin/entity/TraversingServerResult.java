package com.handwin.entity;

import java.util.Arrays;

/**
 * Created by piguangtao on 15/4/15.
 */
public class TraversingServerResult {
    private TraversingServer[] servers;


    public TraversingServer[] getServers() {
        return servers;
    }

    public void setServers(TraversingServer[] servers) {
        this.servers = servers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TraversingServerResult{");
        sb.append("servers=").append(Arrays.toString(servers));
        sb.append('}');
        return sb.toString();
    }

    public static class TraversingServer {
        private String ip;
        private Integer port;
        private String id;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TraversingServer{");
            sb.append("ip='").append(ip).append('\'');
            sb.append(", port='").append(port).append('\'');
            sb.append(", id='").append(id).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
