package com.sj.weixin.web.smack.cache;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
public class JidToCometdSession extends ConcurrentHashMap {

	private static final long serialVersionUID = -8311960830053030844L;
	private static final JidToCometdSession jidToComtedSession = new JidToCometdSession();
	private JidToCometdSession(){
		
	}
	
	public static JidToCometdSession getJidToComtedSession(){
		return jidToComtedSession;
	}
}
