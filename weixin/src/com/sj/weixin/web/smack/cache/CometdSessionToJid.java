package com.sj.weixin.web.smack.cache;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
public class CometdSessionToJid extends ConcurrentHashMap {

	private static final long serialVersionUID = 7520191720338115957L;
	private static final CometdSessionToJid sessionToJid = new CometdSessionToJid();
	
	private CometdSessionToJid(){
		
	}
	
	public static CometdSessionToJid getSessionToJid(){
		return sessionToJid;
	}
}
