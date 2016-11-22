package com.handwin.test.redis;

import com.handwin.redis.RedisAddressUtil;
import com.handwin.redis.RedisCluster;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.HostAndPort;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Danny on 2015-01-11.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-test-redis-cluster.xml")
public class RedisClusterTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisClusterTest.class);

    @Autowired
    private RedisCluster redisCluster;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "dev");
    }

    @Before
    public void before() {
    }

    @After
    public void after() {
    }

    @Test
    public void redisClusterTest() {
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        for (int i = 0; i < 8; i++) {
            executorService.submit(() -> {
                getSetTest();
            });
        }
        sleep(200000000);
    }

    @Test
    public void redisClusterTest2() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(20);
        for (int i = 0; i < 1000; i++) {
            int s = Math.abs(new Random().nextInt() % 10);
            //logger.info("delay {}s", s);
            scheduledExecutorService.schedule(() -> {
                getSetTest();
            }, s, TimeUnit.SECONDS);
        }

        sleep(20000);
    }

    @Test
    public void redisClusterTest3() {
        getSetTest();
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private AtomicInteger count = new AtomicInteger();

    private void getSetTest() {
        redisCluster.getJedisTemplate().execute(jedis -> {
            jedis.set("test1_key", "test1_value");
            String v = jedis.get("test1_key");
            System.out.println(v);
            logger.info("get value : {}, times : {}", v, count.incrementAndGet());
            return null;
        });
    }

    @Test
    public void redisAddressUtilTest() {
        String[] hosts = {
                "192.168.1.1:1234;129.168.1.2:2345",
                "192.168.1.1:1234 129.168.1.2:2345",
                "192.168.1.1:1234,129.168.1.2:2345",
                "192.168.1.1:1234\t129.168.1.2:2345",
                "192.168.1.1:1234;,\t 129.168.1.2:2345",
                "192.168.1.1:1234    129.168.1.2:2345",
        };
        for (String h : hosts) {
            Set<HostAndPort> hs = RedisAddressUtil.parse(h);
            Assert.assertNotNull(hs);
            Assert.assertEquals(hs.size(), 2);
            HostAndPort[] ar = hs.toArray(new HostAndPort[hs.size()]);
            Assert.assertNotNull(ar);
            Assert.assertNotNull(ar[0]);
            Assert.assertNotNull(ar[1]);
            Assert.assertNotNull(ar[0].getHost());
            Assert.assertNotNull(ar[1].getHost());
            Assert.assertEquals(ar[0].getPort(), 1234);
            Assert.assertEquals(ar[1].getPort(), 2345);
        }
    }
}
