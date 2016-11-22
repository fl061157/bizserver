package com.handwin.service.impl;

import com.handwin.entity.TcpStrategyQuery;
import com.handwin.entity.TcpStrategyResult;
import com.handwin.service.IIpStrategyService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/xmls/config-loading.xml")
public class TcpStrategyServiceImplTest {

    @Autowired
    @Qualifier("tcpStrategyServiceImpl")
    private IIpStrategyService ipStrategyService;


    @BeforeClass
    public static void beforeClass() {
        System.setProperty("spring.profiles.active", "test-mock-cn");
    }

    @Test
    public void getTcpServersTest(){
        TcpStrategyQuery query = new TcpStrategyQuery();
        String countryCode = "0086";
        String ip = "210.47.0.1";
        query.buildContryCode(countryCode).buildIp(ip);
        TcpStrategyResult result = ipStrategyService.getTcpServers(query);
        System.out.println(result);
        assertTrue(null != result);

    }
}