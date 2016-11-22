package com.handwin.generic;

import com.handwin.utils.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Danny on 2014-11-19.
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class SimpleIntReadWriteTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleIntReadWriteTest.class);

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
    public void readWriteTest() {
        int a[] = {0, 1, 33, 128, 1212, 65536, 4833030, 828819239, 0xFFFFFFFF, -3, -2421232};
        ByteBuf buf = Unpooled.buffer();

        for (int i = 0; i < a.length; i++) {
            buf.clear();
            ByteBufUtils.writeSimpleInt(buf, a[i]);
            logger.debug("num : {}, bytes : {}", a[i], ByteBufUtil.hexDump(buf));
            Assert.assertEquals(a[i], ByteBufUtils.readSimpleInt(buf));
        }

        long l[] = {0, 1, 33, 128, 1212, 65536, 4833030, 828819239,
                8288192398888L, 0xFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL, -3, -2421232, -232892384729223L};

        for (int i = 0; i < l.length; i++) {
            buf.clear();
            ByteBufUtils.writeSimpleLong(buf, l[i]);
            logger.debug("num : {}, bytes : {}", l[i], ByteBufUtil.hexDump(buf));
            Assert.assertEquals(l[i], ByteBufUtils.readSimpleLong(buf));
        }

        buf.release();
    }
}
