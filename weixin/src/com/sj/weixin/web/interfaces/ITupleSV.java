package com.sj.weixin.web.interfaces;

import java.util.List;

import org.jivesoftware.smackx.muc.MultiUserChat;

import com.sj.weixin.web.entity.GlobalClient;

public interface ITupleSV {
	/**
	 * 创建群
	 * @param client
	 * @param tupleJid
	 * @param tupleName
	 * @param tupleSubject
	 * @param memberList
	 * @return
	 * @throws Exception
	 */
	public int createTuple(GlobalClient client,String tupleJid,String tupleName,String tupleSubject,List<String> memberList) throws Exception;
	/**
	 * 创建群，同时以JSON格式向外推送消息
	 * @param jid
	 * @param tupleJid
	 * @param tupleName
	 * @param tupleSubject
	 * @param memberList
	 * @throws Exception
	 */
	public void createTupleJson(String jid,String tupleJid,String tupleName,String tupleSubject,List<String> memberList) throws Exception;
	
	
	/**
	 * 获得某个client的所有群列表
	 * @param client
	 * @return
	 * @throws Exception
	 */
	public List getTuples(GlobalClient client) throws Exception;
	/**
	 * 根据jid，获得某个client的所有群列表，并以JSON格式推送
	 * @param jid
	 * @throws Exception
	 */
	public void getTuplesJson(String jid) throws Exception;
	/**
	 * 获得某个client的某个所在群的花名册
	 * @param client
	 * @return
	 * @throws Exception
	 */
	public List getTupleRosters(GlobalClient client,String tupleJid) throws Exception;
	
	/**
	 * 根据jid，获得某个client的某个所在群的花名册，，并以JSON格式推送
	 * @param jid
	 * @throws Exception
	 */
	public void getTupleRostersJson(String jid,String tupleJid) throws Exception;
	/**
	 * 根据jid，获得某个client的所有群的花名册，，并以JSON格式推送
	 * @param jid
	 * @throws Exception
	 */
	public void getAllTupleRostersJson(String jid) throws Exception;
	
	/**
	 * 
	 * @param jid
	 * @param nickname
	 * @param password
	 * @param roomJid
	 * @throws Exception
	 */
	//public void joinTuple(String jid,String nickname,String password,String roomJid)throws Exception;
	/**
	 * 
	 * @param client
	 * @param roomJid
	 * @throws Exception
	 */
	public MultiUserChat joinTuple(GlobalClient client,String roomJid)throws Exception;
	/**
	 * 向某个群（房间）发送消息
	 * @param jid
	 * @param roomJid
	 * @param message
	 * @throws Exception
	 */
	public void sendMUCMessage(String jid,String roomJid,String message)throws Exception;
	
	
	/**
	 * 向某个群（房间）发送图片
	 * @param jid
	 * @param roomJid
	 * @param imgURL
	 * @param thumbnailURL
	 * @throws Exception
	 */
	public void sendMUCImg(String jid,String roomJid,String imgURL,String thumbnailURL)throws Exception;
	/**
	 * 处理对群邀请的回应
	 * @param jid
	 * @param inviter
	 * @param tupleId
	 * @param response
	 * @param message
	 * @throws Exception
	 */
	public void invitationResponse(String jid,String inviter,String tupleJid,String response,String message)throws Exception;
	/**
	 * 
	 * @param jid
	 * @throws Exception
	 */
	public void joinAllTuples(String jid) throws Exception;
	
	
	
	/**
	 * 群内添加成员
	 * @param tupleName 
	 * 
	 * 
	 */
	public void TupleInviteJson(String jid, String tupleJid,String tupleName, List<String> memberList);

	int TupleInvite(GlobalClient client, String tupleJid,String tupleName,
			List<String> memberList) throws Exception;
	public void TupleKickJson(String jid, String tupleJid, String memJid);
	int TupleKick(GlobalClient client, String tupleJid, String memJid) throws Exception;

}
