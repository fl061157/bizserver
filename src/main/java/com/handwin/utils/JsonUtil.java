package com.handwin.utils;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
* JSON的工具类
*
* <h3>Here is an example:</h3>
*
* <pre>
*     // 将json通过类型转换成对象
*     {@link JsonUtil JsonUtil}.fromJson("{\"username\":\"username\", \"password\":\"password\"}", User.class);
* </pre>
* <hr />
* <pre>
*     // 传入转换的引用类型
*     {@link JsonUtil JsonUtil}.fromJson("[{\"username\":\"username\", \"password\":\"password\"}, {\"username\":\"username\", \"password\":\"password\"}]", new TypeReference&lt;List&lt;User&gt;&gt;);
* </pre>
* <hr />
* <pre>
*     // 将对象转换成json
*     {@link JsonUtil JsonUtil}.toJson(user);
* </pre>
* <hr />
* <pre>
*     // 将对象转换成json, 可以设置输出属性
*     {@link JsonUtil JsonUtil}.toJson(user, {@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.ALWAYS});
* </pre>
* <hr />
* <pre>
*     // 将对象转换成json, 传入配置对象
*     {@link org.codehaus.jackson.map.ObjectMapper ObjectMapper} mapper = new ObjectMapper();
*     mapper.setSerializationInclusion({@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.ALWAYS});
*     mapper.configure({@link org.codehaus.jackson.map.DeserializationConfig.Feature Feature.FAIL_ON_UNKNOWN_PROPERTIES}, false);
*     mapper.configure({@link org.codehaus.jackson.map.DeserializationConfig.Feature Feature.FAIL_ON_NUMBERS_FOR_ENUMS}, true);
*     mapper.setDateFormat(new {@link java.text.SimpleDateFormat SimpleDateFormat}("yyyy-MM-dd HH:mm:ss"));
*     {@link JsonUtil JsonUtil}.toJson(user, mapper);
* </pre>
* <hr />
* <pre>
*     // 获取Mapper对象
*     {@link JsonUtil JsonUtil}.mapper();
* </pre>
*
* @see JsonUtil JsonUtil
* @see org.codehaus.jackson.map.DeserializationConfig.Feature Feature
* @see org.codehaus.jackson.map.ObjectMapper ObjectMapper
* @see org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion
* @see java.io.IOException IOException
* @see java.text.SimpleDateFormat SimpleDateFormat
*
*/
@SuppressWarnings("unchecked")
public final class JsonUtil {

    private static ObjectMapper MAPPER;

    static {
        MAPPER = generateMapper(Inclusion.NON_NULL);
    }

    private JsonUtil() {
    }

    /**
     * 将json通过类型转换成对象
     *
     * <pre>
     *     {@link JsonUtil JsonUtil}.fromJson("{\"username\":\"username\", \"password\":\"password\"}", User.class);
     * </pre>
     *
     * @param json json字符串
     * @param clazz 泛型类型
     * @return 返回对象
     * @throws java.io.IOException
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return clazz.equals(String.class) ? (T) json : MAPPER.readValue(json, clazz);
    }

    /**
     * 将json通过类型转换成对象
     *
     * <pre>
     *     {@link JsonUtil JsonUtil}.fromJson("[{\"username\":\"username\", \"password\":\"password\"}, {\"username\":\"username\", \"password\":\"password\"}]", new TypeReference&lt;List&lt;User&gt;&gt;);
     * </pre>
     *
     * @param json json字符串
     * @param typeReference 引用类型
     * @return 返回对象
     * @throws java.io.IOException
     */
    public static <T> T fromJson(String json, TypeReference<?> typeReference) throws IOException {
        return (T) (typeReference.getType().equals(String.class) ? json : MAPPER.readValue(json, typeReference));
    }

    /**
     * 将对象转换成json
     *
     * <pre>
     *     {@link JsonUtil JsonUtil}.toJson(user);
     * </pre>
     *
     * @param src 对象
     * @return 返回json字符串
     * @throws java.io.IOException
     */
    public static <T> String toJson(T src) throws IOException {
        return src instanceof String ? (String) src : MAPPER.writeValueAsString(src);
    }

    /**
     * 将对象转换成json, 可以设置输出属性
     *
     * <pre>
     *     {@link JsonUtil JsonUtil}.toJson(user, {@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.ALWAYS});
     * </pre>
     *
     * {@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion 对象枚举}
     * <ul>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.ALWAYS 全部列入}</li>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.NON_DEFAULT 字段和对象默认值相同的时候不会列入}</li>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.NON_EMPTY 字段为NULL或者""的时候不会列入}</li>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.NON_NULL 字段为NULL时候不会列入}</li>
     * </ul>
     *
     * @param src 对象
     * @param inclusion 传入一个枚举值, 设置输出属性
     * @return 返回json字符串
     * @throws java.io.IOException
     */
    public static <T> String toJson(T src, Inclusion inclusion) throws IOException {
        if (src instanceof String) {
            return (String) src;
        } else {
            ObjectMapper customMapper = generateMapper(inclusion);
            return customMapper.writeValueAsString(src);
        }
    }

    /**
     * 将对象转换成json, 传入配置对象
     *
     * <pre>
     *     {@link org.codehaus.jackson.map.ObjectMapper ObjectMapper} mapper = new ObjectMapper();
     *     mapper.setSerializationInclusion({@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.ALWAYS});
     *     mapper.configure({@link org.codehaus.jackson.map.DeserializationConfig.Feature Feature.FAIL_ON_UNKNOWN_PROPERTIES}, false);
     *     mapper.configure({@link org.codehaus.jackson.map.DeserializationConfig.Feature Feature.FAIL_ON_NUMBERS_FOR_ENUMS}, true);
     *     mapper.setDateFormat(new {@link java.text.SimpleDateFormat SimpleDateFormat}("yyyy-MM-dd HH:mm:ss"));
     *     {@link JsonUtil JsonUtil}.toJson(user, mapper);
     * </pre>
     *
     * {@link org.codehaus.jackson.map.ObjectMapper ObjectMapper}
     *
     * @see org.codehaus.jackson.map.ObjectMapper
     *
     * @param src 对象
     * @param mapper 配置对象
     * @return 返回json字符串
     * @throws java.io.IOException
     */
    public static <T> String toJson(T src, ObjectMapper mapper) throws IOException {
        if (null != mapper) {
            if (src instanceof String) {
                return (String) src;
            } else {
                return mapper.writeValueAsString(src);
            }
        } else {
            return null;
        }
    }

    /**
     * 返回{@link org.codehaus.jackson.map.ObjectMapper ObjectMapper}对象, 用于定制性的操作
     *
     * @return {@link org.codehaus.jackson.map.ObjectMapper ObjectMapper}对象
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * 通过Inclusion创建ObjectMapper对象
     *
     * {@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion 对象枚举}
     * <ul>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.ALWAYS 全部列入}</li>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.NON_DEFAULT 字段和对象默认值相同的时候不会列入}</li>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.NON_EMPTY 字段为NULL或者""的时候不会列入}</li>
     *     <li>{@link org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion Inclusion.NON_NULL 字段为NULL时候不会列入}</li>
     * </ul>
     *
     * @param inclusion 传入一个枚举值, 设置输出属性
     * @return 返回ObjectMapper对象
     */
    private static ObjectMapper generateMapper(Inclusion inclusion) {

        ObjectMapper customMapper = new ObjectMapper();

        // 设置输出时包含属性的风格
        customMapper.setSerializationInclusion(inclusion);

        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        customMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        // 禁止使用int代表Enum的order()來反序列化Enum,非常危險
        customMapper.configure(Feature.FAIL_ON_NUMBERS_FOR_ENUMS, true);


        // 所有日期格式都统一为以下样式
        customMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        return customMapper;
    }

    public static <T> String toJson(ObjectMapper mapper, T src) {
        if (src == null) return null;
        try {
            return src instanceof String ? (String) src : mapper.writeValueAsString(src);
        } catch (Exception e) {

            return null;
        }
    }

    public static <T> T fromJson(ObjectMapper mapper, String json, Class<T> clazz) {
        if (json == null) return null;
        try {
            return clazz.equals(String.class) ? (T) json : mapper.readValue(json, clazz);
        } catch (Exception e) {

            return null;
        }
    }
}