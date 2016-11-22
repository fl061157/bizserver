package com.handwin.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteBufUtils {

    public final static Charset UTF8 = Charset.forName("UTF-8");

    private static final Logger logger = LoggerFactory.getLogger(ByteBufUtils.class);

    public static byte[] readByteArray(ByteBuf buf, int length) {
        byte[] ret = new byte[length];
        buf.readBytes(ret);
        return ret;
    }

    public static String readUTF8String(ByteBuf buf, int length) {
        if (length > 0) {
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            return new String(bytes, UTF8);
        } else {
            return null;
        }
    }

    public static void writeUTF8String(ByteBuf buf, String content) {
        buf.writeBytes(content.getBytes(UTF8));
    }

    public static void writeUTF8StringPrefix2ByteLength(ByteBuf buf, String content) {
        byte[] data = content.getBytes(UTF8);
        buf.writeShort(data.length);
        buf.writeBytes(data);
    }


    static byte CHAR_ZERO = '\0';

    public static void writeUTFStringFixedLength(ByteBuf buf, String content, int len) {
        byte[] data = content.getBytes(UTF8);
        int dl = data.length;
        if (dl < len) {
            byte[] nd = new byte[len];
            System.arraycopy(data, 0, nd, 0, dl);
            for (int i = dl; i < len; i++) {
                nd[i] = CHAR_ZERO;
            }
            buf.writeBytes(nd);
        }else {
            buf.writeBytes(data, 0, len);
        }
    }

    public static String readNewUTF8String(ByteBuf buf, int length) {
        if (length > 0) {
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            int index = ArrayUtils.indexOf( bytes , CHAR_ZERO ) ;
            if (index > 0) {
                byte[] nb = new byte[index];
                System.arraycopy(bytes, 0, nb, 0, index);
                bytes = nb;
            }
            return new String(bytes, UTF8);
        } else {
            return null;
        }
    }


    public static void writeByteArrayPrefix2ByteLength(ByteBuf buf, byte[] data) {
        buf.writeShort(data.length);
        buf.writeBytes(data);
    }

    public static boolean[] readBooleanArray(ByteBuf buf, int len) {
        boolean[] is = new boolean[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = buf.readByte() != 0;
        }
        return is;
    }

    public static void writeBooleanArray(ByteBuf buf, boolean[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                buf.writeByte(vs[i] ? 1 : 0);
            }
        }
    }

    public static short[] readSimpleShortArray(ByteBuf buf, int len) {
        short[] is = new short[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = (short) readSimpleInt(buf);
        }
        return is;
    }

    public static void writeSimpleShortArray(ByteBuf buf, short[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                writeSimpleInt(buf, vs[i]);
            }
        }
    }

    public static short[] readShortArray(ByteBuf buf, int len) {
        short[] is = new short[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = buf.readShort();
        }
        return is;
    }

    public static void writeShortArray(ByteBuf buf, short[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                buf.writeShort(vs[i]);
            }
        }
    }

    public static int[] readSimpleIntArray(ByteBuf buf, int len) {
        int[] is = new int[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = readSimpleInt(buf);
        }
        return is;
    }

    public static void writeSimpleIntArray(ByteBuf buf, int[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                writeSimpleInt(buf, vs[i]);
            }
        }
    }

    public static int[] readIntArray(ByteBuf buf, int len) {
        int[] is = new int[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = buf.readInt();
        }
        return is;
    }

    public static void writeIntArray(ByteBuf buf, int[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                buf.writeInt(vs[i]);
            }
        }
    }

    public static long[] readLongArray(ByteBuf buf, int len) {
        long[] is = new long[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = buf.readLong();
        }
        return is;
    }

    public static float[] readFloatArray(ByteBuf buf, int len) {
        float[] is = new float[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = buf.readFloat();
        }
        return is;
    }

    public static void writeFloatArray(ByteBuf buf, float[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                buf.writeFloat(vs[i]);
            }
        }
    }

    public static double[] readDoubleArray(ByteBuf buf, int len) {
        double[] is = new double[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = buf.readDouble();
        }
        return is;
    }

    public static void writeDoubleArray(ByteBuf buf, double[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                buf.writeDouble(vs[i]);
            }
        }
    }

    public static int readSimpleInt(ByteBuf buf) {
        int result = 0;
        int n = 0;
        while (true) {
            n++;
            if (n > 5) {
                throw new IndexOutOfBoundsException("Simple int data error.");
            }
            byte b = buf.readByte();
            result = (result << 7) | (b & 0x7F);
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return result;
    }

    public static void writeSimpleInt(ByteBuf buf, int value) {
        byte[] bs = new byte[5];
        int n = 0;
        for (int i = 4; i >= 0; i--) {
            n++;
            bs[i] = i == 4 ? (byte) (value & 0x7F) : (byte) ((value & 0x7F) | 0x80);
            value = value >>> 7;
            if (value == 0) {
                break;
            }
        }
        buf.writeBytes(bs, 5 - n, n);
    }

    public static long[] readSimpleLongArray(ByteBuf buf, int len) {
        long[] is = new long[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = readSimpleLong(buf);
        }
        return is;
    }

    public static long readSimpleLong(ByteBuf buf) {
        long result = 0;
        int n = 0;
        while (true) {
            n++;
            if (n > 10) {
                throw new IndexOutOfBoundsException("Simple int data error.");
            }
            byte b = buf.readByte();
            result = (result << 7) | (b & 0x7F);
            if ((b & 0x80) == 0) {
                break;
            }
        }
        return result;
    }

    public static void writeSimpleLong(ByteBuf buf, long value) {
        byte[] bs = new byte[10];
        int n = 0;
        for (int i = 9; i >= 0; i--) {
            n++;
            bs[i] = i == 9 ? (byte) (value & 0x7F) : (byte) ((value & 0x7F) | 0x80);
            value = value >>> 7;
            if (value == 0) {
                break;
            }
        }
        buf.writeBytes(bs, 10 - n, n);
    }

    public static void writeSimpleLongArray(ByteBuf buf, long[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                writeSimpleLong(buf, vs[i]);
            }
        }
    }

    public static void writeLongArray(ByteBuf buf, long[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                buf.writeLong(vs[i]);
            }
        }
    }

    public static String readSimpleString(ByteBuf buf) {
        int len = readSimpleInt(buf);
        if (len > 0) {
            return readUTF8String(buf, len);
        }
        return null;
    }

    public static void writeSimpleString(ByteBuf buf, String s) {
        byte[] bs = s.getBytes(UTF8);
        writeSimpleInt(buf, bs.length);
        if (bs.length > 0) {
            buf.writeBytes(bs);
        }
    }

    public static byte[] readSimpleBytes(ByteBuf buf) {
        int len = readSimpleInt(buf);
        if (len > 0) {
            byte[] bs = new byte[len];
            buf.readBytes(bs);
            return bs;
        }
        return new byte[0];
    }

    public static void writeSimpleBytes(ByteBuf buf, byte[] bs) {
        writeSimpleInt(buf, bs.length);
        if (bs.length > 0) {
            buf.writeBytes(bs);
        }
    }

    public static byte[][] readBytesArray(ByteBuf buf, int len) {
        byte[][] is = new byte[len][];
        for (int i = 0; i < is.length; i++) {
            is[i] = readSimpleBytes(buf);
        }
        return is;
    }

    public static void writeBytesArray(ByteBuf buf, byte[][] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                writeSimpleBytes(buf, vs[i]);
            }
        }
    }

    public static String[] readStringArray(ByteBuf buf, int len) {
        String[] is = new String[len];
        for (int i = 0; i < is.length; i++) {
            is[i] = readSimpleString(buf);
        }
        return is;
    }

    public static void writeStringArray(ByteBuf buf, String[] vs) {
        int len = vs.length;
        writeSimpleInt(buf, len);
        if (len > 0) {
            for (int i = 0; i < vs.length; i++) {
                writeSimpleString(buf, vs[i]);
            }
        }
    }


}
