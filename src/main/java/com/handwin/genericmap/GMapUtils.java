package com.handwin.genericmap;

import java.util.Map;

/**
 * Created by Danny on 2014-12-02.
 */
public class GMapUtils {

    public static boolean getBoolean(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && (obj.getClass() == Boolean.class || obj.getClass() == boolean.class)){
            return (boolean)obj;
        }
        return false;
    }

    public static byte getByte(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && (obj.getClass() == Byte.class || obj.getClass() == byte.class)){
            return (byte)obj;
        }
        return 0;
    }

    public static short getShort(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && (obj.getClass() == Short.class || obj.getClass() == short.class)){
            return (short)obj;
        }
        return 0;
    }

    public static int getInt(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && (obj.getClass() == Integer.class || obj.getClass() == int.class)){
            return (int)obj;
        }
        return 0;
    }

    public static long getLong(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && (obj.getClass() == Long.class || obj.getClass() == long.class)){
            return (long)obj;
        }
        return 0;
    }

    public static float getFloat(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && (obj.getClass() == Float.class || obj.getClass() == int.class)){
            return (short)obj;
        }
        return 0;
    }

    public static double getDouble(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && (obj.getClass() == Double.class || obj.getClass() == double.class)){
            return (double)obj;
        }
        return 0;
    }

    public static byte[] getBytes(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && obj.getClass() == byte[].class){
            return (byte[])obj;
        }
        return null;
    }

    public static String getString(Map map, String key){
        Object obj = map.get(key);
        if (obj != null && obj.getClass() == String.class){
            return (String)obj;
        }
        return null;
    }
}
