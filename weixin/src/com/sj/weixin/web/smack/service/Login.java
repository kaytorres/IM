package com.sj.weixin.web.smack.service;

import com.sj.weixin.web.cache.ClientManager;
import com.sj.weixin.web.entity.GlobalClient;
import com.sj.weixin.web.smack.entity.SmackClient;

public class Login {
	// private static JidToGlobalClientMap jidToClient =
	// JidToGlobalClientMap.getJidToClientMap();
	public static String login(String username, String password, String server,	int port) throws Exception {
		String jid = "";
		String onlyUsername = username.substring(0, username.indexOf("@"));
		SmackClient smackClient = new SmackClient(onlyUsername, password, server,
				port);
		GlobalClient client = ClientManager.getClient(username);
		if (client != null) {
			client.setSmackClient(smackClient);
			client.setChatManagerListener();
			client.setMessageListener();
			client.setInvitationListener();
			jid = client.login();
		}
		return jid;
	}

	public static String login(String username, String password, String server)
			throws Exception {
		String jid = "";
		String onlyUsername = username.substring(0, username.indexOf("@"));
		SmackClient smackClient = new SmackClient(onlyUsername, password, server,5222);

		GlobalClient client = ClientManager.getClient(username);
		if (client != null) {
			client.setSmackClient(smackClient);
			client.setChatManagerListener();
			client.setMessageListener();
			client.setInvitationListener();
			jid = client.login();
		}
		return jid;
	}
}
