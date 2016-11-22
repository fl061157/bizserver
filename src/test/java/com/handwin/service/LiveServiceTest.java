package com.handwin.service;
import com.handwin.entity.TraversingServerResult;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Test;


public class LiveServiceTest {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper = new ObjectMapper();
//        this.objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

        objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }

    @Test
    public void testGetLiveServer() throws Exception {
        String result = "[{\"id\":\"114.215.193.49\",\"ip\":\"/dudu/traversing/traversing_v5_67bb778ee31211e4a66000163e02247e\",\"port\":7011}]";
        TraversingServerResult.TraversingServer[] traversingServers = objectMapper.readValue(result, new TypeReference<TraversingServerResult.TraversingServer[]>() {
        });

        Assert.assertTrue(null != traversingServers);


    }
}