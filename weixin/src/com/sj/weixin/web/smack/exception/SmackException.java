package com.sj.weixin.web.smack.exception;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class SmackException extends Exception {

	private static final long serialVersionUID = 2396522572842581076L;

	public SmackException(Type type, String key, Throwable cause) {
		super(getMessageText(key), cause);
		this.type = type;
		this.setKey(key);
	}

	public SmackException(Type type, String key) {
		this(type, getMessageText(key), null);
	}

	public SmackException(String key, Throwable cause) {
		this(Type.MESSAGE, getMessageText(key), cause);
	}

	public SmackException(String key) {
		this(Type.MESSAGE, getMessageText(key));
	}

	public static enum Type {
		FATAL, MESSAGE
	}
	
	public static String getMessageText(String key) {
		String message = key;
		// TODO read from 'resource-ChatException'
		try{
			Locale locale = new Locale("zh","CN");
			ResourceBundle rb = ResourceBundle.getBundle("/com/sj/tesns/web/smack/exception/resource-SmackException",locale);
			message = rb.getString(key);
		} catch (MissingResourceException e) {
			// ignore
		}
		return message;
	}

	private Type type = Type.MESSAGE;
	private String key;
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
