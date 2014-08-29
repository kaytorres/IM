package com.sj.weixin.web.model.dictionary;

public enum LoginStatus {
	   INIT(0), CONNECTING(1), HASLOGIN(2), DISCONN(3);
	 
	    private int value;
	 
	    private LoginStatus(int value) {
	        this.value = value;
	    }
	 
	    public int getValue() {
	        return value;
	    }
	 
	    public boolean isRest() {
	        return false;
	    }	
}
