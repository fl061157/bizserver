package com.handwin.utils;


import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by piguangtao on 14-3-25.
 */
public class SystemUtils {

    private static ObjectMapper objectMapper;

    public final static boolean isLinux;

//    private final static EventLoopGroup eventLoopGroup;

    static {
        //epoll -et 在压力测试下，io.netty.channel.epoll.Native.readAddress报java.nio.channels.ClosedChannelException: null
        //先不使用epoll et
        //isLinux = false;
        String name = System.getProperty("os.name").toLowerCase(Locale.UK).trim();
        isLinux = name.startsWith("linux");

//        if (isLinux) {
//            eventLoopGroup = new EpollEventLoopGroup();
//        } else {
//            eventLoopGroup = new NioEventLoopGroup();
//        }

        objectMapper = new org.codehaus.jackson.map.ObjectMapper();
//        this.objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }
/*
    public static EventLoopGroup getEventLoopGroup() {
       return eventLoopGroup;
    }

    public static Class<? extends ServerSocketChannel> getServerSocketChannel() {
        if (isLinux) {
            return EpollServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

    public static Class<? extends SocketChannel> getSocketChannel() {
        if (isLinux) {
            return EpollSocketChannel.class;
        } else {
            return NioSocketChannel.class;
        }
    }
*/

    public static String getRandom(String[] pars) {
        Random random = ThreadLocalRandom.current();
        int index = random.nextInt(pars.length);
        return pars[index];
    }

    public static long getGreenwichTimestamp() {
        Calendar calendar = Calendar.getInstance();
//        //获取时间偏移量
//        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
//        //获取夏令时差
//        int dstOffset = calendar.get(Calendar.DST_OFFSET);
//        calendar.add(Calendar.MILLISECOND,-(zoneOffset+dstOffset));
        return calendar.getTime().getTime();
    }

    public static int readSimpleInt(byte[] intBytes) {
        if (null == intBytes || intBytes.length != 4) {
            throw new InvalidParameterException("intBytes length should be 4.");
        }
        int result;
        ByteBuffer buffer = ByteBuffer.wrap(intBytes);
        result = buffer.getInt();
        buffer.clear();
        return result;
    }

    public static void main(String[] args) {
        byte[] appKeyBytes = new byte[]{0x00, 0x03, 0x5b, 0x60};

        int appKey = readSimpleInt(appKeyBytes);
        System.out.println(appKey);

    }


    public static String getJsonStr(Map map) throws IOException {
        if (null == map) return null;
        return objectMapper.writeValueAsString(map);
    }


}
