package com.handwin.server;

import com.handwin.entity.PushCallBean;
import com.handwin.entity.PushMsgMqBean;
import com.handwin.entity.PushTextBean;

/**
 * @author fangliang
 */
public interface PushMessageSender {

    public void write(String toRegion,PushMsgMqBean pushMsgMqBean); //TODO

    public void write(String toRegion,PushTextBean pushTextBean); //TODO

    public void write(String toRegion,PushCallBean pushCallBean); //TODO

    public void write(String toRegion,byte[] pushMessageBody);

}
