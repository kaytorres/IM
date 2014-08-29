package com.sj.weixin.web.cache;

import java.util.concurrent.ConcurrentHashMap;

import com.sj.weixin.web.entity.GlobalClient;

public class JidToGlobalClientMap extends ConcurrentHashMap<String, GlobalClient> {

	private static final long serialVersionUID = 8026969685990965628L;
	private static final JidToGlobalClientMap jidToClientMap = new JidToGlobalClientMap();
	
	private JidToGlobalClientMap(){
		
	}
	
	public static JidToGlobalClientMap getJidToClientMap(){
		return jidToClientMap;
	}
}
