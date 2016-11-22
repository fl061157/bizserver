package com.handwin.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author fangliang
 *
 */
public enum ServerMessageType {
	
	WrongMessage(1) ,
	ServerHeartBeatMessage(2),
	ServerForwardHeartBeatMessage(22) ;
	
	private int messageType ;
	private ServerMessageType(int messageType){
		this.messageType = messageType ;
	}
	public int getMessageType() {
		return messageType;
	}
	
	static Map<Integer, ServerMessageType> MESSAGE_TYPE_MAP = new HashMap<Integer, ServerMessageType>() ;
	static {
		for( ServerMessageType serverMessageType : ServerMessageType.values() ) {
			MESSAGE_TYPE_MAP.put(serverMessageType.messageType, serverMessageType) ;
		}
		
	}
	
	public static ServerMessageType getServerMessageType( int messageType ) {
		return MESSAGE_TYPE_MAP.get( messageType ) ;
	}
	
	
	

}
