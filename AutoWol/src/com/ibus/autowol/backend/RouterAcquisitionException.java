package com.ibus.autowol.backend;

public class RouterAcquisitionException extends Exception 
{
	private static final long serialVersionUID = 1L;
	
	public RouterAcquisitionException(String message) {
        super(message);
    }

    public RouterAcquisitionException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
