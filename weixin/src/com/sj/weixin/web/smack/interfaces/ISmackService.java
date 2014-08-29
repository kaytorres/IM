package com.sj.weixin.web.smack.interfaces;

import org.jivesoftware.smack.Roster;

public interface ISmackService {
	public String login(String username,String password,String server,int port) throws Exception;
	public String login(String username,String password,String server) throws Exception;
	
	public void createAccount(String username,String password,String server) throws Exception;
	
	public String getRoster(String Jid) throws Exception;
//	public String createChat(String jid,String toJid) throws Exception;
//	
//	public void sendMessage(String jid,String toJid,String message) throws Exception;
	String getName(String jid) throws Exception;
}
