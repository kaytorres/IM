package com.sj.weixin.web.entity;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

public class SuperMucClient {
	private static XMPPConnection conn;
	private ConnectionConfiguration conf;
	private String username = "sjtest2";
	private String password = "test";
	private static final SuperMucClient superMucClient = new SuperMucClient();
	private SuperMucClient(){
		conn = new XMPPConnection("fe.shenj.com");
		try {
			conn.connect();
			conn.login(username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static SuperMucClient getIntance(){
		return superMucClient;
	}
	public static XMPPConnection getConn() {
		return conn;
	}
	public void setConn(XMPPConnection conn) {
		this.conn = conn;
	}
}
