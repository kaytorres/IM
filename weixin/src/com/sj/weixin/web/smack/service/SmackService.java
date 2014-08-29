package com.sj.weixin.web.smack.service;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;

import com.sj.weixin.web.cache.ClientManager;
import com.sj.weixin.web.entity.GlobalClient;
import com.sj.weixin.web.smack.cache.JidToSmackClient;
import com.sj.weixin.web.smack.entity.SmackClient;
import com.sj.weixin.web.smack.interfaces.ISmackService;

public class SmackService implements ISmackService {
	@Override
	public String login(String username, String password, String server,
			int port) throws Exception {
		// TODO Auto-generated method stub
		return Login.login(username, password, server, port);
	}

	@Override
	public String login(String username, String password, String server)
			throws Exception {
		// TODO Auto-generated method stub
		return Login.login(username, password, server);
	}

	@Override
	public String getRoster(String jid) throws Exception {
		GlobalClient client = (GlobalClient) ClientManager.getClient(jid);
		client.getRoster();
		return null;
	}
	
	@Override
	public String getName(String jid) throws Exception {
		GlobalClient client = (GlobalClient) ClientManager.getClient(jid);
		
		return client.getSmackClient().getUsername();
	}

	@Override
	public void createAccount(String username, String password,String server)
			throws Exception {
		XMPPConnection connection = new XMPPConnection(server);
		connection.connect();
		AccountManager amgr = connection.getAccountManager();
		amgr.createAccount(username, password);

		connection.disconnect();	
	}

//	@Override
//	public String createChat(String jid,String toJid) throws Exception {
//		SmackClient smackClient = (SmackClient) jidToClient.get(jid);
//		smackClient.createChat(toJid);
//		return null;
//	}
//
//	@Override
//	public void sendMessage(String jid,String toJid, String message) throws Exception {
//		SmackClient smackClient = (SmackClient) jidToClient.get(jid);
//		smackClient.sendMessage(toJid, message);
//
//	}

}
