package com.handwin.genericmap;

import com.handwin.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GMapDecodeAndEncoder {

    private static Logger logger = LoggerFactory.getLogger(GMapDecodeAndEncoder.class);

    public static Map decode(ByteBuf buf) {
        Map<String, Object> gmap = new ListOrderedMap();
        int len;
        while (buf.isReadable()) {
            String key = ByteBufUtils.readSimpleString(buf);
            byte valueFlag = buf.readByte();
            byte typeInt = (byte) (valueFlag & 0x1F);
            boolean useShortInt = (valueFlag & 0x20) != 0;
            GValueType type = GValueType.getInstance(typeInt);
            switch (type) {
                case BOOL:
                    gmap.put(key, buf.readByte() != 0);
                    break;
                case BYTE:
                    gmap.put(key, buf.readByte());
                    break;
                case SHORT:
                    gmap.put(key, (short) (useShortInt ? ByteBufUtils.readSimpleInt(buf) : buf.readShort()));
                    break;
                case INT:
                    gmap.put(key, (int) (useShortInt ? ByteBufUtils.readSimpleInt(buf) : buf.readInt()));
                    break;
                case LONG:
                    gmap.put(key, (long) (useShortInt ? ByteBufUtils.readSimpleLong(buf) : buf.readLong()));
                    break;
                case FLOAT:
                    gmap.put(key, buf.readFloat());
                    break;
                case DOUBLE:
                    gmap.put(key, buf.readDouble());
                    break;
                case BYTES:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, ByteBufUtils.readByteArray(buf, len));
                    }
                    break;
                case STRING:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, new String(ByteBufUtils.readByteArray(buf, len), ByteBufUtils.UTF8));
                    }
                    break;
                case LBOOL:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, ByteBufUtils.readBooleanArray(buf, len));
                    }
                    break;
                case LBYTE:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, ByteBufUtils.readByteArray(buf, len));
                    }
                    break;
                case LSHORT:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, useShortInt ?
                                ByteBufUtils.readSimpleShortArray(buf, len) :
                                ByteBufUtils.readShortArray(buf, len));
                    }
                    break;
                case LINT:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, useShortInt ?
                                ByteBufUtils.readSimpleIntArray(buf, len) :
                                ByteBufUtils.readIntArray(buf, len));
                    }
                    break;
                case LLONG:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, useShortInt ?
                                ByteBufUtils.readSimpleLongArray(buf, len) :
                                ByteBufUtils.readLongArray(buf, len));
                    }
                    break;
                case LFLOAT:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, ByteBufUtils.readFloatArray(buf, len));
                    }
                    break;
                case LDOUBLE:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, ByteBufUtils.readDoubleArray(buf, len));
                    }
                    break;
                case LBYTES:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, ByteBufUtils.readBytesArray(buf, len));
                    }
                    break;
                case LSTRING:
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        gmap.put(key, ByteBufUtils.readStringArray(buf, len));
                    }
                    break;
                case OBJECT: {
                    int size = ByteBufUtils.readSimpleInt(buf);
                    if (size > 0) {
                        ByteBuf sbuf = buf.slice(buf.readerIndex(), size);
                        Map smap = decode(sbuf);
                        if (smap != null) {
                            gmap.put(key, smap);
                        }
                        buf.skipBytes(size);
                    }
                }
                break;
                case LOBJECT: {
                    len = ByteBufUtils.readSimpleInt(buf);
                    if (len > 0) {
                        Map[] subMaps = new Map[len];
                        gmap.put(key, subMaps);
                        for (int i = 0; i < len; i++) {
                            int size = ByteBufUtils.readSimpleInt(buf);
                            if (size > 0) {
                                ByteBuf sbuf = buf.slice(buf.readerIndex(), size);
                                subMaps[i] = decode(sbuf);
                                buf.skipBytes(size);
                            }
                        }
                    }
                }
                break;
                default:
                    logger.error("unknown value type {}", typeInt);
                    throw new RuntimeException("unknown gmap type");

            }
        }
        return gmap.size() > 0 ? gmap : null;
    }


    public static void encode(Map<?, ?> gmap, ByteBuf buf) {
        buf.markWriterIndex();
        for (Map.Entry en : gmap.entrySet()) {
            String key = StringUtils.trimToNull(en.getKey().toString());
            Object value = en.getValue();
            if (value == null || key == null) {
                continue;
            }
            if (String.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.STRING.id() & 0x1F);
                ByteBufUtils.writeSimpleString(buf, (String) value);
            } else if (Integer.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.INT.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleInt(buf, ((Integer) value).intValue());
            } else if (int.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.INT.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleInt(buf, (int) value);
            } else if (boolean.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.BOOL.id() & 0x1F);
                buf.writeByte(((boolean) value) ? 1 : 0);
            } else if (Boolean.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.BOOL.id() & 0x1F);
                buf.writeByte(((Boolean) value) ? 1 : 0);
            } else if (byte.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.BYTE.id() & 0x1F);
                buf.writeByte((byte) value);
            } else if (Byte.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.BYTE.id() & 0x1F);
                buf.writeByte(((Byte) value).byteValue());
            } else if (short.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.SHORT.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleInt(buf, (short) value);
            } else if (Short.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.SHORT.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleInt(buf, ((Short) value).shortValue());
            } else if (long.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.LONG.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleLong(buf, (long) value);
            } else if (Long.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.LONG.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleLong(buf, ((Long) value).longValue());
            } else if (float.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.FLOAT.id() & 0x1F);
                buf.writeFloat((float) value);
            } else if (Float.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.FLOAT.id() & 0x1F);
                buf.writeFloat(((Float) value).floatValue());
            } else if (double.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.DOUBLE.id() & 0x1F);
                buf.writeDouble((double) value);
            } else if (Double.class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.DOUBLE.id() & 0x1F);
                buf.writeDouble(((Double) value).doubleValue());
            } else if (byte[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.BYTES.id() & 0x1F);
                ByteBufUtils.writeSimpleBytes(buf, (byte[]) value);
            } else if (boolean[].class == value.getClass() || Boolean[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.LBOOL.id() & 0x1F);
                ByteBufUtils.writeBooleanArray(buf, (boolean[]) value);
            } else if (short[].class == value.getClass() || Short[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.LSHORT.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleShortArray(buf, (short[]) value);
            } else if (int[].class == value.getClass() || Integer[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.LINT.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleIntArray(buf, (int[]) value);
            } else if (long[].class == value.getClass() || Long[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte((GValueType.LLONG.id() & 0x1F) | 0x20);
                ByteBufUtils.writeSimpleLongArray(buf, (long[]) value);
            } else if (float[].class == value.getClass() || Float[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.LFLOAT.id() & 0x1F);
                ByteBufUtils.writeFloatArray(buf, (float[]) value);
            } else if (double[].class == value.getClass() || Double[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.LDOUBLE.id() & 0x1F);
                ByteBufUtils.writeDoubleArray(buf, (double[]) value);
            } else if (String[].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.LSTRING.id() & 0x1F);
                ByteBufUtils.writeStringArray(buf, (String[]) value);
            } else if (byte[][].class == value.getClass()) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.LBYTES.id() & 0x1F);
                ByteBufUtils.writeBytesArray(buf, (byte[][]) value);
            } else if (value instanceof Map) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.OBJECT.id() & 0x1F);
                ByteBuf gbuf = Unpooled.buffer();
                encode((Map) value, gbuf);
                ByteBufUtils.writeSimpleInt(buf, gbuf.readableBytes());
                if (gbuf.readableBytes() > 0) {
                    buf.writeBytes(gbuf);
                }
                gbuf.release();
            } else if (value instanceof Map[]) {
                ByteBufUtils.writeSimpleString(buf, key);
                buf.writeByte(GValueType.LOBJECT.id() & 0x1F);
                Map[] gMaps = (Map[]) value;
                int count = gMaps == null ? 0 : gMaps.length;
                ByteBufUtils.writeSimpleInt(buf, count);
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        if (gMaps[i] != null && gMaps[i].size() > 0) {
                            ByteBuf gbuf = Unpooled.buffer();
                            encode(gMaps[i], gbuf);
                            int slen = gbuf.readableBytes();
                            ByteBufUtils.writeSimpleInt(buf, slen);
                            if (slen > 0) {
                                buf.writeBytes(gbuf);
                            }
                            gbuf.release();
                        } else {
                            ByteBufUtils.writeSimpleInt(buf, 0);
                        }
                    }
                }
            } else {
                logger.warn("unsupport type for {}", value.getClass());
            }
        }
    }
}
