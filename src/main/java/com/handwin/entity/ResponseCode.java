package com.handwin.entity;

/**
 * 
 * @author fangliang
 *
 */
public enum ResponseCode {
	
	Ok200(200),
	ResourceNotFound(404) ,
	ServerInternalError(500) ;
	
	private ResponseCode(int code) {
		this.code = code;
	}
 	private int code ;
	public int getCode() {
		return code;
	}
	

}
