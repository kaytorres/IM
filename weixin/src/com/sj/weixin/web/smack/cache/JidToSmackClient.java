package com.sj.weixin.web.smack.cache;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
public class JidToSmackClient extends ConcurrentHashMap {

	private static final long serialVersionUID = 8026964730990965628L;
	private static final JidToSmackClient jidToSmackClient = new JidToSmackClient();
	
	private JidToSmackClient(){
		
	}
	
	public static JidToSmackClient getJidToSmackClient(){
		return jidToSmackClient;
	}
}
