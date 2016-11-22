package com.handwin.service.impl;

import com.handwin.entity.UdpStrategy2Result;
import com.handwin.entity.UdpStrategyQuery;
import com.handwin.service.IIpStrategyService;
import com.handwin.service.IUDPAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by piguangtao on 14-3-7.
 */
@Service("uDPStrategyAssignServiceImpl")
public class UDPStrategyAssignServiceImpl implements IUDPAssignService {

    @Autowired
    @Qualifier("udpStrategyServiceHttpImpl")
    private IIpStrategyService ipStrategyService;

    public UdpStrategy2Result getUdpStrategy(final UdpStrategyQuery query) {
        return ipStrategyService.getUdp(query);
    }

}
