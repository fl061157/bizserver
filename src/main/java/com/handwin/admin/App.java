package com.handwin.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;

/**
 * Created by fangliang on 16/5/13.
 */
public class App {

    public static void main(String[] args) throws Exception {

        String tcpServers = "[\"54.223.150.8:443\", \"54.223.150.8:442\"]";

        String fileServers = "[\"54.223.150.8:443\", \"54.223.150.8:442\"]";


        Map<String, String[]> mm = new HashedMap();

        mm.put("tcp_servers", JSON.parseObject(tcpServers, String[].class));
        mm.put("file_servers", JSON.parseObject(fileServers, String[].class));


        String ss = JSON.toJSONString(mm);

        System.out.println(ss);

        Map<String, String[]> map = JSON.parseObject(ss, new TypeReference<Map<String, String[]>>() {
        });

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            String[] value = entry.getValue();
            for (String s : value) {
                System.out.println(s);
            }
        }


    }

}
