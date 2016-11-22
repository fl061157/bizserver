package com.handwin.mq;

import com.rabbitmq.client.Connection;

/**
 * Created by fangliang on 10/9/15.
 */
public class ConnectionWrapper {

    private final String address ;

    private final Connection connection ;

    public ConnectionWrapper( String address , Connection connection ) {
        this.address = address ;
        this.connection = connection ;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getAddress() {
        return address;
    }
}
