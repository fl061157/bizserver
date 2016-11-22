package com.handwin.utils;

import com.handwin.bean.RichMessageInfo;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by piguangtao on 15/11/24.
 */
public class MessageUtilsTest {

    private MessageUtils messageUtils = new MessageUtils();

    private ObjectMapper objectMapper = null;

    @Before
    public void init() {
        this.objectMapper = new ObjectMapper();
//        this.objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        this.objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        this.objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }

    @Test
    public void testGetAtUserIdsForRichMessage() throws Exception {
        String content = "<at name=\"测试syt2\" id=\"5de58980f65311e4812bd3656605d338\" alt=\"@测试syt2\" /> 在乎别人";

        int count = 1;
        long start = System.currentTimeMillis();
        RichMessageInfo richMessageInfo = null;
        for (int i = 0; i < count; i++) {
            richMessageInfo = messageUtils.generateRichMessage(content);
        }
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(richMessageInfo);

        content = "hello富媒体测试";
        richMessageInfo = messageUtils.generateRichMessage(content);
        System.out.println(richMessageInfo);

        content = "<user id =\"5de58980f65311e4812bd3656605d338\"/>赞了<user id =\"5de58980f65311e4812bd3656605d339\" />的露脸视频";
        richMessageInfo = messageUtils.generateRichMessage(content);
        System.out.println(richMessageInfo);

    }

    @Test
    public void testGetAtUserIdsForRichMessage1() throws Exception {
        String content = "<dadfad>dafd";

        int count = 1;
        long start = System.currentTimeMillis();
        RichMessageInfo richMessageInfo = null;
        for (int i = 0; i < count; i++) {
            richMessageInfo = messageUtils.generateRichMessage(content);
        }
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(richMessageInfo);

        content = "hello富媒体测试";
        richMessageInfo = messageUtils.generateRichMessage(content);
        System.out.println(richMessageInfo);


    }

    @Test
    public void testVideoContent() throws IOException {
        String content = "{\"desc\": \"{\\n  \\\"type\\\" : 1,\\n  \\\"costume\\\" : \\\"cat\\\",\\n  \\\"duration\\\" : 3,\\n  \\\"facelift\\\" : 0.05,\\n  \\\"burned\\\" : false\\n}\",\"short_url\": \"http:\\/\\/114.215.204.97:29102\\/api\\/file\\/download\\/2016\\/1\\/7\\/11\\/d7bf565a-7e5a-4693-8fb7-e832f18525cc\",\"video_url\": \"http:\\/\\/114.215.204.97:29102\\/api\\/file\\/download\\/2016\\/1\\/7\\/11\\/541fc996-f1bd-4f05-83c8-f89679569c4a\"}";

        Map<String, Object> video = objectMapper.readValue(content.getBytes(StandardCharsets.UTF_8), new TypeReference<Map<String, Object>>() {
        });
        if (null != video) {
            Object extraData = video.get("desc");

            Map<String, Object> descMap = objectMapper.readValue(((String) extraData).getBytes(StandardCharsets.UTF_8), new TypeReference<Map<String, Object>>() {
            });

            Object burned = descMap.get("burned");

            if (null != burned)

            {
                //表示阅后即焚
                if (((Boolean) burned)) {
                    assertTrue(false);
                } else {
                    assertTrue(true);
                }
            }

            assertTrue(null != burned);
        }
    }

    @Test
    public void testGetVideoShareUrl() {
        String sessionId = "47db8ffc03d046858c54ab892a860a0f";
        String countryCode = "0086";
        String video = "{\"extra_data\": {\"animation\": 0,\"burned\": false,\"costume\": \"cat\",\"duration\": 3,\"facelift\": 0.05,\"progress\": 0,\"read_status\": 0,\"type\": 1},\"local_video\": \"/storage/emulated/0/Android/data/me.chatgame.mobilecg/cache/.video/mv_1451960568473.ts\",\"snap_url\": \"http://114.215.204.97:29102/api/file/download/2016/1/5/10/c03b09b6-3ef4-457b-be79-bae3c81e5b95.jpg\",\"video_url\": \"http://114.215.204.97:29102/api/file/download/2016/1/5/10/6f3d1f58-fadc-4ffc-9c0f-dc63135d1cdb.ts\"}";

        MessageUtils messageUtils = new MessageUtils();
        messageUtils.httpServerUrl = "http://test.v5.cn";
        messageUtils.objectMapper = objectMapper;
        String shareUrl = messageUtils.getVideoShareUrl(sessionId, countryCode, video);
        assertTrue(null != shareUrl);
    }

    @Test
    public void test() {

    }

}