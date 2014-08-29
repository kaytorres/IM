package com.sj.weixin.web.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemStatus;

import com.sj.kunlun.entity.type.TypeAddsbookView;
import com.sj.weixin.web.entity.AddsBook;
import com.sj.weixin.web.entity.Folder;
import com.sj.weixin.web.entity.SimpleXCard;
import com.sj.weixin.web.entity.XCard;
import com.sj.weixin.web.smack.common.Function;

public class JsonHandler {
	/**
	 * 
	 * @param from
	 * @param to
	 * @param name
	 * @param dateTime
	 * @param content
	 * @return
	 */
	private static String MUC_TAG = "muc";
	private static String MUC_GROUP_TAG = "Tuple";
	
	
	public static String messageJson(String from, String to, String name,	String dateTime, String content,String title) {
		
		
		String ret = "{\"packagetype\":\"message\",\"data\":[{\"from\":\""
				+ from + "\",\"to\":\"" + to + "\",\"name\":\"" + name
				+ "\",\"time\":\"" + dateTime + "\",\"body\":\"" + Function.transferHTML(content)
				+ "\",\"title\":\""+Function.transferHTML(title)+"\"}]}";

		System.out.println("messageJson  " + ret);

		return ret;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param name
	 * @param dateTime
	 * @param content
	 * @return
	 */
	public static String MUCMessageJson(String from, String to, String name,
			String dateTime, String content,String title) {
		/*
		content = content.replace("\\","\\\\");
		content = content.replace("&","&amp;");
		content = content.replace("\"","&quot;");
		content = content.replace("<","&lt;");
		content = content.replace(">","&gt;");
		content = content.replace("\n","<br>");
		content = content.replace("\r","<br>");
		*/
		
		String ret = "{\"packagetype\":\"mucmessage\",\"data\":[{\"from\":\""
				+ from + "\",\"to\":\"" + to
				+ "\",\"type\":\"groupchat\",\"name\":\"" + name
				+ "\",\"time\":\"" + dateTime + "\",\"body\":\"" + Function.transferHTML(content)
				+ "\",\"title\":\""+Function.transferHTML(title)+"\"}]}";

		System.out.println("MUCMessageJson  " + ret);

		return ret;
	}

	/**
	 * 
	 * @param from
	 * @param to
	 * @param name
	 * @param dateTime
	 * @param content
	 * @return
	 */
	public static String PrivateMUCMessageJson(String from, String to,
			String name, String dateTime, String content) {
		String ret = "{\"packagetype\":\"mucmessage\",\"data\":[{\"from\":\""
				+ from + "\",\"to\":\"" + to
				+ "\",\"type\":\"chat\",\"name\":\"" + name + "\",\"time\":\""
				+ dateTime + "\",\"body\":\"" + content + "\"}]}";
		return ret;
	}

	/**
	 * 
	 * @param type
	 * @param roster
	 * @return
	 */
	public static String rosterJson(String type, Roster roster) {
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"roster\",\"data\":[");
		Collection<RosterEntry> entries = roster.getEntries();
		int i = 0;
		for (RosterEntry entry : entries) {
			Collection<RosterGroup> groups = entry.getGroups();
			Presence pr = roster.getPresence(entry.getUser());
			pr.getStatus();
			for (RosterGroup group : groups) {
				if (!"Tuple".equals(group.getName())) {
					if (i == 0) {
						ret.append("{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ group.getName() + "\"}");
					} else {
						ret.append(",{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ group.getName() + "\"}");
					}
					i++;
				}
			}
		}
		ret.append("]}");
		return ret.toString();
	}
	
	public static String rosterAlfaSortJsonForUpdate(String string,	Roster roster) {
		// TODO Auto-generated method stub
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"rosterForUpdate\",\"data\":[");
		Collection<RosterEntry> entries = roster.getEntries();
		List<RosterEntry>	RosterList = new ArrayList<RosterEntry>();//原始roster
		for (RosterEntry entry : entries)
		{
			RosterList.add(entry);
		}
		int  RosterListLength = RosterList.size();
		for(int i = 0; i<RosterListLength; i++)
		{
			
			for(int j=i+1;j<RosterListLength;j++)
			{
				if(AlfaCompare(RosterList.get(i).getName(),RosterList.get(j).getName())>0)
				{
					RosterEntry tempEntry = RosterList.get(i);
					RosterList.set(i, RosterList.get(j));
					RosterList.set(j, tempEntry);
				}
			}
		}
		int i = 0;
		for (RosterEntry entry : RosterList)
		{
			Collection<RosterGroup> groups = entry.getGroups();
			Presence pr = roster.getPresence(entry.getUser());
			pr.getStatus();
			for (RosterGroup group : groups) {
				if (!"Tuple".equals(group.getName())) 
				{
					if (i == 0) 
					{
						ret.append("{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"false\"}");
					} 
					else 
					{
						ret.append(",{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"false\"}");
					}
					i++;
				}
				else
				{
					if (i == 0) 
					{
						ret.append("{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"true\"}");
					}
					else
					{
						ret.append(",{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"true\"}");
					}
					i++;
				}
			}
		}
		
		
		
		ret.append("]}");
		return ret.toString();
	}
	
	public static String XCardListSortJson(List<XCard> xCardList)
	{
		List<SimpleXCard>	newXCardList = new ArrayList<SimpleXCard>();
		
		for(XCard xCard : xCardList)
		{
			
			Iterator<SimpleXCard> it = newXCardList.iterator(); 
			boolean ifadd = true;
			while (it.hasNext()) 
			{
				SimpleXCard value = it.next();
				if(value.getJid().equals(xCard.getJid()))
				{
					if(AlfaCompare(value.getName(),xCard.getName())>0)
					{
						it.remove();
						ifadd = true;
						break;
					}
					else
					{
						ifadd = false;
						break;
					}
					
				}
			} 
			
			if(ifadd)
			{
				SimpleXCard sxc =  new SimpleXCard(xCard.getJid(),xCard.getName());
				newXCardList.add(sxc);
			}
		}
		for(int i=0;i<newXCardList.size()-1;i++)
		{
			for(int j=i+1;j<newXCardList.size();j++)
			{
				if(AlfaCompare(newXCardList.get(i).getName(), newXCardList.get(j).getName())>0)
				{
					SimpleXCard tempXCard = new SimpleXCard(newXCardList.get(i).getJid(),newXCardList.get(i).getName());
					
					
					newXCardList.set(i, newXCardList.get(j));
					newXCardList.set(j, tempXCard);
					
					
				}
			}
		}
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"xCardSort\",\"data\":[");
		int ind = 0;
		for(SimpleXCard sxc : newXCardList)
		{
			if (ind == 0) 
			{
				ret.append("{\"jid\":\""+sxc.getJid()+"\",\"name\":\""+sxc.getName()+"\",\"group\":\""+AlfaClass(sxc.getName())+"\"}");
				ind++;
			}
			else
			{
				ret.append(",{\"jid\":\""+sxc.getJid()+"\",\"name\":\""+sxc.getName()+"\",\"group\":\""+AlfaClass(sxc.getName())+"\"}");
				ind++;
			}
		}
		ret.append("]}");
		return ret.toString();
	}
	
	
	
	/**
	 * 
	 * @param type
	 * @param roster
	 * @return
	 */
	public static String rosterAlfaSortJson(String type, Roster roster) {
		
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"roster\",\"data\":[");
		Collection<RosterEntry> entries = roster.getEntries();
		List<RosterEntry>	RosterList = new ArrayList<RosterEntry>();//原始roster
		for (RosterEntry entry : entries)
		{
			RosterList.add(entry);
		}
		int  RosterListLength = RosterList.size();
		for(int i = 0; i<RosterListLength; i++)
		{
			for(int j=i+1;j<RosterListLength;j++)
			{
				if(AlfaCompare(RosterList.get(i).getName(),RosterList.get(j).getName())>0)
				{
					RosterEntry tempEntry = RosterList.get(i);
					RosterList.set(i, RosterList.get(j));
					RosterList.set(j, tempEntry);
				}
			}
		}
		int i = 0;
		for (RosterEntry entry : RosterList)
		{
			Collection<RosterGroup> groups = entry.getGroups();
			//Presence pr = roster.getPresence(entry.getUser());
			//pr.getStatus();
			String jid = entry.getUser();
			String domain = jid.split("@")[1];
			if((domain.split("\\.")[0] != null)&&(!domain.split("\\.")[0].equals(MUC_TAG)))	//好友
			{
				if (i == 0) 
				{
					ret.append("{\"type\":\"get\",\"jid\":\""
							+ entry.getUser() + "\",\"name\":\""
							+ entry.getName()
							+ "\",\"subscription\":\"\",\"group\":\""
							+ AlfaClass(entry.getName()) + "\",\"muc\":\"false\"}");
				} 
				else 
				{
					ret.append(",{\"type\":\"get\",\"jid\":\""
							+ entry.getUser() + "\",\"name\":\""
							+ entry.getName()
							+ "\",\"subscription\":\"\",\"group\":\""
							+ AlfaClass(entry.getName()) + "\",\"muc\":\"false\"}");
				}
				i++;
			}
			else																			//群
			{
				if (i == 0) 
				{
					ret.append("{\"type\":\"get\",\"jid\":\""
							+ entry.getUser() + "\",\"name\":\""
							+ entry.getName()
							+ "\",\"subscription\":\"\",\"group\":\""
							+ AlfaClass(entry.getName()) + "\",\"muc\":\"true\"}");
				}
				else
				{
					ret.append(",{\"type\":\"get\",\"jid\":\""
							+ entry.getUser() + "\",\"name\":\""
							+ entry.getName()
							+ "\",\"subscription\":\"\",\"group\":\""
							+ AlfaClass(entry.getName()) + "\",\"muc\":\"true\"}");
				}
				i++;
			}
			/*
			for (RosterGroup group : groups) {
				if (!"Tuple".equals(group.getName())) 
				{
					if (i == 0) 
					{
						ret.append("{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"false\"}");
					} 
					else 
					{
						ret.append(",{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"false\"}");
					}
					i++;
				}
				else
				{
					if (i == 0) 
					{
						ret.append("{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"true\"}");
					}
					else
					{
						ret.append(",{\"type\":\"get\",\"jid\":\""
								+ entry.getUser() + "\",\"name\":\""
								+ entry.getName()
								+ "\",\"subscription\":\"\",\"group\":\""
								+ AlfaClass(entry.getName()) + "\",\"muc\":\"true\"}");
					}
					i++;
				}
			}	*/
			
		}
		
		
		
		ret.append("]}");
		return ret.toString();
	
	}
	
	//比较两字符串字母表排序
	private static int AlfaCompare(String str1,String str2)
	{
		return PinyinComparator.compare(str1,str2);
	}
	
	private static String AlfaClass(String str)
	{
        return PinyinConv.cn2py(str);
	}
	
	/**
	 * 
	 * @param roster
	 * @return
	 */
	public static String presenceInitJson(Roster roster) {
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"presence\",\"data\":[");
		Collection<RosterEntry> entries = roster.getEntries();
		int i = 0;
		for (RosterEntry entry : entries) {
			
			Presence presence = roster.getPresence(entry.getUser());
			
			
			String from = Function.getPureId(presence.getFrom());
			String to = presence.getTo();
			String domain = from.split("@")[1];
			if((domain.split("\\.")[0] != null)&&(!domain.split("\\.")[0].equals(MUC_TAG)))
			{
			
				//String type = pr.getType() == null ? "null" : pr.getType().toString();
				if((presence.getStatus() != null)&&(presence.getMode() != null))
				{
					
					String show = presence.getMode().toString();
					String status = presence.getStatus();
					
					status = Function.transferHTML(status);
					if(i==0)
					{
						ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
								+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\""+status+"\",\"show\":\""+show+"\"}");
					}
					else
					{
						ret.append(",{\"from\":\"" + from + "\",\"to\":\"" + to
							+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\""+status+"\",\"show\":\""+show+"\"}");
					}
					i++;
					
					
				}
				else
				{
					if(presence.getStatus() != null)		//状态签名修改
					{
						
						String status = presence.getStatus();
						
						
						
						status = Function.transferHTML(status);
						if(i==0)
						{
							ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
									+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\""+status+"\",\"show\":\"\"}");
						}
						else
						{
							ret.append(",{\"from\":\"" + from + "\",\"to\":\"" + to
								+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\""+status+"\",\"show\":\"\"}");
						}
						i++;
						
					}
					else
					{
						if(presence.getMode() != null)		//在线状态变更
						{
							
							String show = presence.getMode().toString();
							if(i==0)
							{
								ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
										+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\"\",\"show\":\"" + show + "\"}");
							}
							else
							{
								ret.append(",{\"from\":\"" + from + "\",\"to\":\"" + to
									+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\"\",\"show\":\"" + show + "\"}");
							}
							i++;
						}
						else
						{
							if(presence.getType().toString().equals("unavailable"))	//不在线
							{
								if(i==0)
								{
									ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
											+ "\",\"type\":\"unavailable\",\"id\":\"\",\"status\":\"\",\"show\":\"\"}");
								}
								else
								{
									ret.append(",{\"from\":\"" + from + "\",\"to\":\"" + to
										+ "\",\"type\":\"unavailable\",\"id\":\"\",\"status\":\"\",\"show\":\"\"}");
								}
								i++;
							}
							else
							{
								if(i==0)
								{
									ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
											+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\"\",\"show\":\"\"}");
								}
								else
								{
									ret.append(",{\"from\":\"" + from + "\",\"to\":\"" + to
										+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\"\",\"show\":\"\"}");
								}
								i++;
							}
						}
					}
				}
			}
		}
		ret.append("]}");
		return ret.toString();
	}

	/**
	 * 
	 * @param pr
	 * @return
	 */
	public static String presenceListenJson(Presence presence) {
		StringBuffer ret = new StringBuffer();
		String from = Function.getPureId(presence.getFrom());
		String to = presence.getTo();
		String domain = from.split("@")[1];
		if((domain.split("\\.")[0] != null)&&(!domain.split("\\.")[0].equals(MUC_TAG)))
		{
			if((presence.getStatus() != null)&&(presence.getMode() != null))
			{
				ret.append("{\"packagetype\":\"presence\",\"data\":[");
				String show = presence.getMode().toString();
				String status = presence.getStatus();
				status = Function.transferHTML(status);
				ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
						+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\""+status+"\",\"show\":\""+show+"\"}");
				ret.append("]}");
			}
			else
			{
				if(presence.getStatus() != null)		//状态签名修改
				{
					ret.append("{\"packagetype\":\"presence\",\"data\":[");
					String status = presence.getStatus();
					status = Function.transferHTML(status);
					ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
							+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\""+status+"\",\"show\":\"\"}");
					ret.append("]}");
					
				}
				else
				{
					if(presence.getMode() != null)		//在线状态变更
					{
						ret.append("{\"packagetype\":\"presence\",\"data\":[");
						String show = presence.getMode().toString();
						ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
								+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\"\",\"show\":\"" + show + "\"}");
						ret.append("]}");
					}
					else
					{
						if(presence.getType().toString().equals("unavailable"))	//不在线
						{
							ret.append("{\"packagetype\":\"presence\",\"data\":[");
							ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
									+ "\",\"type\":\"unavailable\",\"id\":\"\",\"status\":\"\",\"show\":\"\"}");
							ret.append("]}");
						}
						else
						{
							ret.append("{\"packagetype\":\"presence\",\"data\":[");
							ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
									+ "\",\"type\":\"available\",\"id\":\"\",\"status\":\"\",\"show\":\"\"}");
							ret.append("]}");
						}
					}
				}
			}
		}
		
		//ret.append("{\"packagetype\":\"presence\",\"data\":[");
		//String status = pr.getStatus();
		//String show = pr.getMode() == null ? "null" : pr.getMode().toString();
		//String from = Function.getPureId(pr.getFrom());
		//String to = pr.getTo();
		//String type = pr.getType() == null ? "null" : pr.getType().toString();

		//ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
		//		+ "\",\"type\":\"" + type + "\",\"id\":\"\",\"status\":\""
		//		+ status + "\",\"show\":\"" + show + "\"}");

		//ret.append("]}");
		return ret.toString();
		
	}

	public static String mucPresenceInitJson(String jid,
			List<Map> tupleRosterList) {
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"mucpresence\",\"data\":[");
		int i = 0;
		for (Map map : tupleRosterList) {
			String presenceJid = (String) map.get("jid");
			String from = (String) map.get("tuple");
			String to = jid;
			String presenceNickName = (String) map.get("nickName");
			String type = "available";
			if (presenceNickName == null) {
				type = "unavailable";
			}

			if (i == 0) {
				ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
						+ "\",\"type\":\"" + type + "\",\"id\":\""
						+ presenceJid + "\",\"name\":\"" + presenceNickName
						+ "\",\"status\":\"\",\"show\":\"\"}");
			} else {
				ret.append(",{\"from\":\"" + from + "\",\"to\":\"" + to
						+ "\",\"type\":\"" + type + "\",\"id\":\""
						+ presenceJid + "\",\"name\":\"" + presenceNickName
						+ "\",\"status\":\"\",\"show\":\"\"}");
			}
			i++;
		}
		ret.append("]}");

		System.out.println("mucPresenceInitJson  " + ret.toString());

		return ret.toString();
	}

	/**
	 * 
	 * @param from
	 *            群号
	 * @param to
	 *            接受者jid
	 * @param presenceJid
	 *            当前状态变化者的jid
	 * @param presenceNickName
	 *            当前状态变化者的nickName
	 * @param type
	 *            状态类型 available（在群内，已经加入群） unavailable（没有加入群，离开状态）
	 * @return
	 */
	public static String mucPresenceListenJson(String from, String to,
			String presenceJid, String presenceNickName, String type) {
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"mucpresence\",\"data\":[");

		ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
				+ "\",\"type\":\"" + type + "\",\"id\":\"" + presenceJid
				+ "\",\"name\":\"" + presenceNickName
				+ "\",\"status\":\"\",\"show\":\"\"}");

		ret.append("]}");

		System.out.println("mucPresenceListenJson  " + ret.toString());

		return ret.toString();
	}

	/**
	 * 
	 * @param groupList
	 * @param from
	 * @param to
	 * @param xmlns
	 * @return
	 */
	public static String tupleJson(List<Map> groupList, String from, String to,
			String xmlns) {
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"muciq\",\"data\":[");
		int i = 0;
		String type = "login";
		ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
				+ "\",\"type\":\"" + type + "\",\"id\":\"\",\"xmlns\":\""
				+ xmlns + "\",\"mucs\":[");

		for (Map map : groupList) {
			String jid = (String) map.get("jid");
			String name = (String) map.get("name");
			String title = (String) map.get("title");
			String occupantcount = ((Integer) map.get("occupantcount"))
					.toString();
			String role = (String) map.get("role");

			if (i == 0) {
				ret.append("{\"jid\":\"" + jid + "\",\"name\":\"" + name
						+ "\",\"title\":\"" + title
						+ "\",\"id\":\"\",\"occupantcount\":\"" + occupantcount
						+ "\",\"role\":\"" + role + "\"}");
			} else {
				ret.append(",{\"jid\":\"" + jid + "\",\"name\":\"" + name
						+ "\",\"title\":\"" + title
						+ "\",\"id\":\"\",\"occupantcount\":\"" + occupantcount
						+ "\",\"role\":\"" + role + "\"}");
			}
			i++;

		}
		ret.append("]}]}");

		System.out.println("Tuple Json" + ret.toString());

		return ret.toString();
	}

	/**
	 * 
	 * @param tupleRosterList
	 * @return
	 */
	public static String tupleRosterJson(List<Map> tupleRosterList) {
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"mucroster\",\"data\":[");
		int i = 0;
		for (Map map : tupleRosterList) {
			String jid = (String) map.get("jid");
			String name = (String) map.get("name");
			String subscription = (String) map.get("subscription");
			String tuple = (String) map.get("tuple");
			if (i == 0) {
				ret.append("{\"type\":\"get\",\"jid\":\"" + jid
						+ "\",\"name\":\"" + name + "\",\"subscription\":\""
						+ subscription + "\",\"tuple\":\"" + tuple + "\"}");
			} else {
				ret.append(",{\"type\":\"get\",\"jid\":\"" + jid
						+ "\",\"name\":\"" + name + "\",\"subscription\":\""
						+ subscription + "\",\"tuple\":\"" + tuple + "\"}");
			}
			i++;
		}
		ret.append("]}");

		System.out.println("Tuple Rosters Json" + ret.toString());

		return ret.toString();
	}

	/**
	 * 
	 * @param success
	 * @param tupleJid
	 * @param to
	 * @param xmlns
	 * @return
	 */
	public static String createTupleJson(String success, String tupleJid,
			String to) {
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"muciq\",\"data\":[");
		String type = "create";
		String xmlns = "http://kunlun.cn/muc/create";
		ret.append("{\"from\":\"\",\"to\":\"" + to + "\",\"type\":\"" + type
				+ "\",\"id\":\"\",\"xmlns\":\"" + xmlns + "\",\"result\":\""
				+ success + "\",\"muc\":\"" + tupleJid + "\"");

		ret.append("}]}");
		return ret.toString();
	}

	/**
	 * 
	 * @param tupleJid
	 * @param from
	 * @param to
	 * @param datetime
	 * @param message
	 * @return
	 */
	public static String invitationJson(String tupleJid, String from,
			String to, String datetime, String message) {
		StringBuffer ret = new StringBuffer();
		String type = "invitation";
		String xmlns = "http://kunlun.cn/muc/invitation";
		ret.append("{\"packagetype\":\"muciq\",\"datetime\":\"" + datetime
				+ "\",\"data\":[");
		ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
				+ "\",\"type\":\"" + type + "\",\"tuple\":\"" + tupleJid
				+ "\",\"message\":\"" + message + "\",\"xmlns\":\"" + xmlns
				+ "\"");

		ret.append("}]}");

		System.out.println("mucPresenceInitJson  " + ret.toString());

		return ret.toString();
	}

	/**
	 * 
	 * @param tupleJid
	 * @param from
	 * @param to
	 * @param datetime
	 * @param response
	 * @param message
	 * @return
	 */
	public static String invitationResponseJson(String tupleJid, String from,
			String to, String datetime, String response, String message) {
		StringBuffer ret = new StringBuffer();
		String type = "invitationResponse";
		String xmlns = "http://kunlun.cn/muc/invitation";
		ret.append("{\"packagetype\":\"muciq\",\"datetime\":\"" + datetime
				+ "\",\"data\":[");
		ret.append("{\"from\":\"" + from + "\",\"to\":\"" + to
				+ "\",\"type\":\"" + type + "\",\"tuple\":\"" + tupleJid
				+ "\",\"response\":\"" + response + "\",\"message\":\""
				+ message + "\",\"xmlns\":\"" + xmlns + "\"");

		ret.append("}]}");
		return ret.toString();
	}

	/**
	 * 被邀请加入新群
	 * @param tupleJid
	 * @param smackJid
	 * @param subject 
	 * @param newMucRoster 
	 * @return
	 */
	public static String mucInvitationJson(String tupleJid, String smackJid,String datetime, String subject, List<Map> newMucRoster) {
		// TODO Auto-generated method stub
		StringBuffer ret = new StringBuffer();
		String type = "mucInvitation";
		String xmlns = "http://kunlun.cn/muc/invitation";
		ret.append("{\"packagetype\":\"muciq\",\"datetime\":\"" + datetime+ "\",\"data\":[");
		ret.append("{\"from\":\"" + tupleJid + "\",\"to\":\"" + smackJid+ "\",\"type\":\"" + type + "\",\"tuple\":\"" + tupleJid+ "\",\"subject\":\""+subject+"\",\"xmlns\":\"" + xmlns + "\",\"roster\":[");
		int i = 0;
		for (Map map : newMucRoster) {
			
			String jid = (String) map.get("jid");
			String name = (String) map.get("name");
			String subscription = (String) map.get("subscription");
			String tuple = (String) map.get("tuple");
			if(i == 0)
			{
				ret.append("{\"jid\":\""+jid+"\",\"name\":\""+name+"\",\"subscription\":\""+subscription+"\",\"tuple\":\""+tuple+"\"}");
			}
			else
			{
				ret.append(",{\"jid\":\""+jid+"\",\"name\":\""+name+"\",\"subscription\":\""+subscription+"\",\"tuple\":\""+tuple+"\"}");
			}
			i++;
		}
		
		
		ret.append("]}]}");
		return ret.toString();
		
		
		
		
		
	}
	
	/**
	 * 被踢出群
	 * @param tupleJid
	 * @param smackJid
	 * 
	 * 
	 * @return
	 */
	public static String mucModeratorRevokedJson(String tupleJid, String to,
			String type) {
		// TODO Auto-generated method stub
		
		
		
		
		StringBuffer ret = new StringBuffer();
		
		String xmlns = "http://kunlun.cn/muc/ModeratorRevoked";
		ret.append("{\"packagetype\":\"muciq\",\"data\":[");
		ret.append("{\"from\":\"" + tupleJid + "\",\"to\":\"" + to+ "\",\"type\":\"" + type + "\",\"tuple\":\"" + tupleJid+ "\"");
		ret.append("}]}");
		return ret.toString();
		
	}

	/**
	 * 通讯录列表
	 * 
	 * @param typeAddsbookView
	 * 
	 * 
	 * @return
	 */
	public static String AddsbookJson(TypeAddsbookView[] typeAddsbookViews) {
		// TODO Auto-generated method stub
		
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"addsbook\",\"data\":[");
		int i=0;
		for(TypeAddsbookView typeAddsbookView :typeAddsbookViews)
		{
			if(i==0)
			{
				typeAddsbookView.getAddbookId();
				typeAddsbookView.getDisplayName();
				typeAddsbookView.getFriendJid();
			}
			else
			{
				
				
			}
			i++;
		}
		
		return null;
	}

	
	
	public static String AddsBookListJson(List<AddsBook> addsbookList)
	{
		StringBuffer ret = new StringBuffer();
		ret.append("{\"packagetype\":\"addsbook\",\"data\":[");
		int i=0;
		for(AddsBook adb : addsbookList)
		{
			if(i==0)
			{
				ret.append("{\"name\":\""+adb.getName()+"\",\"id\":\""+adb.getId()+"\",\"xCardList\":[");
				String childXCardListJson = XCardListJson(adb.getxCardList());
				ret.append(childXCardListJson);
				ret.append("]");
				ret.append(",\"folderList\":[");
				String childFolderListJson = FolderListJson(adb.getFolderList());
				ret.append(childFolderListJson);
				ret.append("]");
				ret.append("}");
				i++;
			}
			else
			{
				ret.append(",{\"name\":\""+adb.getName()+"\",\"id\":\""+adb.getId()+"\",\"xCardList\":[");
				String childXCardListJson = XCardListJson(adb.getxCardList());
				ret.append(childXCardListJson);
				ret.append("]");
				ret.append(",\"folderList\":[");
				String childFolderListJson = FolderListJson(adb.getFolderList());
				ret.append(childFolderListJson);
				ret.append("]");
				ret.append("}");
				i++;
			}
		}
		ret.append("]}");
		return ret.toString();
	}
	
	public static String XCardListJson(List<XCard> xCardList)
	{
		StringBuffer ret = new StringBuffer();
		int i=0;
		for(XCard xCard : xCardList)
		{
			if(i==0)
			{
				ret.append("{\"id\":\""+xCard.getId()+"\",\"name\":\""+xCard.getName()+"\",\"jid\":\""+xCard.getJid()+"\",\"linkId\":\""+xCard.getLinkId()+"\",\"index\":\""+xCard.getIndex()+"\",\"vCard\":\"\"}");
				i++;
			}
			else
			{
				ret.append(",{\"id\":\""+xCard.getId()+"\",\"name\":\""+xCard.getName()+"\",\"jid\":\""+xCard.getJid()+"\",\"linkId\":\""+xCard.getLinkId()+"\",\"index\":\""+xCard.getIndex()+"\",\"vCard\":\"\"}");
				i++;
			}
		}
		return ret.toString();
	}
	
	
	public static String FolderListJson(List<Folder> folderList)
	{
		StringBuffer ret = new StringBuffer();
		int i=0;
		for(Folder folder : folderList)
		{
			if(i==0)
			{
				ret.append("{\"id\":\""+folder.getId()+"\",\"name\":\""+folder.getName()+"\",\"index\":\""+folder.getIndex()+"\",\"parentId\":\""+folder.getParentId()+"\",\"xCardList\":[");
				String childXCardListJson = XCardListJson(folder.getxCardList());
				ret.append(childXCardListJson);
				ret.append("]");
				ret.append(",\"folderList\":[");
				String childFolderListJson = FolderListJson(folder.getFolderList());
				ret.append(childFolderListJson);
				ret.append("]");
				ret.append("}");
				i++;
			}
			else
			{
				ret.append(",{\"id\":\""+folder.getId()+"\",\"name\":\""+folder.getName()+"\",\"index\":\""+folder.getIndex()+"\",\"parentId\":\""+folder.getParentId()+"\",\"xCardList\":[");
				String childXCardListJson = XCardListJson(folder.getxCardList());
				ret.append(childXCardListJson);
				ret.append("]");
				ret.append(",\"folderList\":[");
				String childFolderListJson = FolderListJson(folder.getFolderList());
				ret.append(childFolderListJson);
				ret.append("]");
				ret.append("}");
				i++;
			}
		}
		return ret.toString();
	}

}
