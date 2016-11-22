package com.handwin.entity;

/**
 * 
 * @author fangliang
 *
 */
public class BizOutputMessage {
	
	private String exchange ;
	
	private String routeKey ;
	
	private byte[] messageBody ;

    private String tcpZoneCode ;
	
	
	public BizOutputMessage( String exchange, String routeKey, byte[] messageBody ) { 
		this.exchange = exchange ;
		this.routeKey = routeKey ;
		this.messageBody = messageBody ;
	}
	
	
	public String getExchange() {
		return exchange;
	}
	
	public byte[] getMessageBody() {
		return messageBody;
	}
	
	public String getRouteKey() {
		return routeKey;
	}


    public String getTcpZoneCode() {
        return tcpZoneCode;
    }

    public void setTcpZoneCode(String tcpZoneCode) {
        this.tcpZoneCode = tcpZoneCode;
    }
}
