package com.handwin.exception;


public class ServerException extends RuntimeException {

	private static final long serialVersionUID = 4594144835799863784L;
	
	private ErrorCode code ;


    public ServerException(String msg){
        super(msg);
    }
	
	public ServerException( ErrorCode code ) {
		super() ;
		this.code = code ;
	}
	
	public ServerException(ErrorCode code , String msg ) { 
		super(msg) ;
		this.code = code ;
	}
	
	public ServerException(ErrorCode code , String msg , Throwable throwable) {
		super(msg, throwable) ; 
		this.code = code ;
	}
	
	public ServerException(ErrorCode code ,  Throwable throwable ) { 
		super(throwable) ;
		this.code = code ;
	}
	
	public ErrorCode getCode() {
		return code;
	}
	
	
	public static enum ErrorCode {
		CanHandleIoError,
		CanNotHandleIoError ,
		ResourceNotFoundError ;
	}
	
}
