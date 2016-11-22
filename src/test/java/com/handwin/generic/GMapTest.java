package com.handwin.generic;

import com.handwin.genericmap.GMapDecodeAndEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.collections.map.ListOrderedMap;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Danny on 2014-11-20.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class GMapTest {
    private static final Logger logger = LoggerFactory.getLogger(GMapTest.class);

    @BeforeClass
    public static void beforeClass() {
    }

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void encodeDecoderTest3() {
        Map<String, Object> gmap = new ListOrderedMap();
        //gmap.put("float_1.1", 1.1F);
        //gmap.put("double_-1.1", -1.1D);
        //gmap.put("string_ok", "ok");
        boolean[] lb = {false, true, false, true};
        gmap.put("boolean_array", lb);

        ByteBuf buf = Unpooled.buffer();

        buf.clear();
        GMapDecodeAndEncoder.encode(gmap, buf);

        logger.debug("encoded len {}, bytes : {}", buf.readableBytes(), ByteBufUtil.hexDump(buf));
        logger.debug("toString:{}", gmap);

        buf.release();

    }

    @Test
    public void encodeDecoderTest2() {
        byte[] bs = {0, 1, 2, 3, (byte) 0xFF};
        String[] ss = {"ok", "asdf", "asdfadsf"};
        Map<String, Object> gmap = new ListOrderedMap();
        gmap.put("boolean_true", true);
        gmap.put("boolean_false", false);
        gmap.put("byte_1", (byte) 0x01);
        gmap.put("short_1", (short) 1);
        gmap.put("int_1", new Integer(1));
        gmap.put("int_-1", -1);
        gmap.put("long_1", 1L);
        gmap.put("float_1.1", 1.1F);
        gmap.put("double_1.1", 1.1D);
        gmap.put("string_ok", "ok");
        gmap.put("bytes_bs", bs);
        gmap.put("string_ss", ss);

        int[] ints = {3, 2, 32, -239023, -1, 0xFFFFFFFF, -0x80000000};
        short[] shorts = {3, 6, -1, 34};
        long[] longs = {2L, 4L, 92823928323L, -334289234234L};
        float[] floats = {2.2F, 3.23232323F, -0.1212F};
        double[] doubles = {2.2D, 3.23232323D, -0.1212D, -0.0000001111111D};

        gmap.put("ints", ints);
        gmap.put("shorts", shorts);
        gmap.put("longs", longs);
        gmap.put("floats", floats);
        gmap.put("doubles", doubles);

        Map smap = new ListOrderedMap();
        smap.put("sint_1", 1);
        smap.put("sstring_ok", "ok");
        gmap.put("smap", smap);

        Map[] ms = new Map[2];
        ms[0] = new ListOrderedMap();
        ms[1] = new ListOrderedMap();
        ms[0].put("ss_int_1", 1);
        ms[0].put("ss_string_ok", "ok");
        ms[1].put("ss_int_1", 1);
        ms[1].put("ss_string_ok", "ok");
        gmap.put("maps", ms);

        ByteBuf buf = Unpooled.buffer();

        int n = 1;
        long stime = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            buf.clear();
            GMapDecodeAndEncoder.encode(gmap, buf);
        }
        logger.debug("time:{}", (System.currentTimeMillis() - stime));
        logger.debug("encoded len {}, bytes : {}", buf.readableBytes(), ByteBufUtil.hexDump(buf));
        logger.debug("toString:{}", gmap);

        ObjectMapper mapper = new ObjectMapper();
        try {
            stime = System.currentTimeMillis();
            for (int i = 0; i < n; i++) {
                String json = mapper.writeValueAsString(gmap);
            }
            logger.debug("json time:{}", (System.currentTimeMillis() - stime));
            //logger.debug("json:{}:{}", json.getBytes().length, json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map gmap2 = GMapDecodeAndEncoder.decode(buf);

        Assert.assertTrue((boolean) gmap2.get("boolean_true"));
        Assert.assertTrue(!(boolean) gmap2.get("boolean_false"));
        Assert.assertEquals((byte) gmap2.get("byte_1"), (byte) 0x01);
        Assert.assertEquals((int) gmap2.get("int_1"), 1);
        Assert.assertEquals((int) gmap2.get("int_-1"), -1);
        Assert.assertEquals((short) gmap2.get("short_1"), (short) 1);
        Assert.assertEquals((long) gmap2.get("long_1"), 1L);
        Assert.assertEquals((float) gmap2.get("float_1.1"), 1.1F, 0);
        Assert.assertEquals((double) gmap2.get("double_1.1"), 1.1D, 0);
        Assert.assertEquals((String) gmap2.get("string_ok"), "ok");

        Assert.assertArrayEquals((byte[]) gmap2.get("bytes_bs"), bs);
        Assert.assertArrayEquals((String[]) gmap2.get("string_ss"), ss);
        Assert.assertArrayEquals((int[]) gmap2.get("ints"), ints);
        Assert.assertArrayEquals((short[]) gmap2.get("shorts"), shorts);
        Assert.assertArrayEquals((long[]) gmap2.get("longs"), longs);
        Assert.assertArrayEquals((float[]) gmap2.get("floats"), floats, 0);
        Assert.assertArrayEquals((double[]) gmap2.get("doubles"), doubles, 0);

        Map smap2 = (Map) gmap2.get("smap");
        Assert.assertNotNull(smap2);
        Assert.assertEquals((int) smap2.get("sint_1"), 1);
        Assert.assertEquals((String) smap2.get("sstring_ok"), "ok");

        Map[] ms2 = (Map[]) gmap2.get("maps");
        Assert.assertNotNull(ms2);
        Assert.assertEquals(ms2.length, 2);

        Assert.assertEquals((int) ms2[0].get("ss_int_1"), 1);
        Assert.assertEquals((String) ms2[0].get("ss_string_ok"), "ok");

        Assert.assertEquals((int) ms2[1].get("ss_int_1"), 1);
        Assert.assertEquals((String) ms2[1].get("ss_string_ok"), "ok");

        buf.release();
    }
}
