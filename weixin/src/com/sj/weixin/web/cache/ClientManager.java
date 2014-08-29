package com.sj.weixin.web.cache;

import com.sj.weixin.web.entity.GlobalClient;
import com.sj.weixin.web.entity.SuperMucClient;

public class ClientManager {
	private static JidToGlobalClientMap map = JidToGlobalClientMap.getJidToClientMap();
	
	@SuppressWarnings("unchecked")
	public static void addClient(String jid,GlobalClient client){
		map.put(jid, client);
	}
	
	public static GlobalClient getClient(String jid){
		return (GlobalClient) map.get(jid);
	}
	
	public static void removeClient(String jid){
		map.remove(jid);
	}
	
	public static GlobalClient createClient(String jid){
		GlobalClient client = new GlobalClient();
		client.setWsStatus(0);
		client.setXmppStatus(0);
		addClient(jid,client);
		return client;
	}
	
	public static void logout(String jid) throws Exception{
		GlobalClient client = getClient(jid);
		client.logout();
		map.remove(jid);
	}
}
