package com.sj.weixin.web.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.cometd.bayeux.server.ServerSession;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sj.kunlun.dao.AddsbookDao;
import com.sj.kunlun.dao.GlobalDao;
import com.sj.kunlun.entity.type.TypeAddsbook;
import com.sj.kunlun.entity.type.TypeAddsbookView;
import com.sj.kunlun.exception.ErrorResultException;
import com.sj.weixin.web.cache.ClientManager;
import com.sj.weixin.web.cometd.server.MessageService;
import com.sj.weixin.web.common.JsonHandler;
import com.sj.weixin.web.interfaces.ITupleSV;
import com.sj.weixin.web.service.TupleSV;
import com.sj.weixin.web.smack.common.Function;
import com.sj.weixin.web.smack.entity.SmackClient;

public class GlobalClient {
	
	private Date lastActiveTime;
	private Date loginTime;
	private String smackJid;
	private String wsId;
	private String wsSessionId;
	private int wsStatus;
	private SmackClient smackClient;
	private int xmppStatus;
	private Roster rosterCache;
	private ServerSession cometdSession;
	private String avatarBASE64;
	private List<XCard> xCardList = new ArrayList<XCard>();
	
	private List<AddsBook> addsbookList = new ArrayList<AddsBook>();

	
	
	public List<XCard> getxCardList() {
		return xCardList;
	}

	public void setxCardList(List<XCard> xCardList) {
		this.xCardList = xCardList;
	}

	public void AddAddsBook(AddsBook addsbook)
	{
		this.addsbookList.add(addsbook);
	}
	
	public void ClearAddsBook()
	{
		this.addsbookList.clear();
	}
	
	public String getAvatarBASE64() {
		return avatarBASE64;
	}

	public void setAvatarBASE64(String avatarBASE64) {
		this.avatarBASE64 = avatarBASE64;
	}

	public List<AddsBook> getAddsbookList() {
		return addsbookList;
	}

	public void setAddsbookList(List<AddsBook> addsbookList) {
		this.addsbookList = addsbookList;
	}

	// cometd的session的list，为以后多终端扩展使用
	private List<ServerSession> cometdSessionList = Collections
			.synchronizedList(new ArrayList<ServerSession>());
	private List<String> browserSessionList = Collections
			.synchronizedList(new ArrayList<String>());

	private String name;
	
	private AddsbookDao addsbookDao;

	@Autowired
	public void setAddsbookDao(AddsbookDao addsbookDao) {
		this.addsbookDao = addsbookDao;
	}

	/**
	 * 登入
	 * 
	 * @return
	 * @throws Exception
	 */
	public String login() throws Exception {
		smackJid = smackClient.login();
		return smackJid;
	}

	/**
	 * 登出
	 * 
	 * @throws Exception
	 */
	public void logout() throws Exception {
		smackClient.logout();
		wsStatus = 3;
		xmppStatus = 3;
		cometdSessionList.clear();
		browserSessionList.clear();
	}

	/**
	 * 获得好友列表
	 * 
	 * @throws Exception
	 */
	public void getRoster() throws Exception {
		rosterCache = smackClient.getRoster();
		Collection<RosterEntry> entries = rosterCache.getEntries();
		for (RosterEntry entry : entries) {
			Collection<RosterGroup> groups = entry.getGroups();
			for (RosterGroup group : groups) {
				if ("Tuple".equals(group.getName())) {
					String jid = entry.getUser();
					MultiUserChat muc = smackClient.getMUCInstance(jid);
					try
					{
						muc.getOwners();
					}
					catch(XMPPException e)
					{
						rosterCache.removeEntry(entry);
						System.out.println("被群 "+jid+" 踢出");
					}
					
				}
			}
		}
		
		
		Roster roster = rosterCache;
		//String rosterJson = JsonHandler.rosterJson("", roster);
		//对Roster按首字母进行分组
		String rosterJson = JsonHandler.rosterAlfaSortJson("", roster);
		
		
		String presenceInitJson = JsonHandler.presenceInitJson(roster);
		
		roster.addRosterListener(new RosterListener() {
			public void entriesDeleted(Collection<String> addresses) {
			}

			public void entriesUpdated(Collection<String> addresses) {
			}

			public void presenceChanged(Presence presence) {
				String changePresence = JsonHandler
						.presenceListenJson(presence);
				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", changePresence, null);
				}
			}

			public void entriesAdded(Collection<String> collection) {
			}
		});
		if (cometdSession != null) {
			System.out.println("srversession2:"+cometdSession);
			cometdSession.deliver(MessageService.getLocalSession(),
					"/kunlun/xmpp/client", rosterJson, null);
			cometdSession.deliver(MessageService.getLocalSession(),
					"/kunlun/xmpp/client", presenceInitJson, null);
		}
	}

	/**
	 * 发送消息
	 * 
	 * @param toJid
	 * @param message
	 * @throws XMPPException
	 */
	public void sendMessage(String toJid, String message) throws XMPPException {
		String chatId = (String) smackClient.getParticipantToChatThread().get(toJid);
		if (chatId == null || chatId == "") {
			smackClient.createChat(toJid);
		}
		smackClient.sendMessage(toJid, message);
	}
	
	/**
	 * 发送图片
	 * 
	 * @param toJid
	 * @param imgURL
	 * @throws XMPPException
	 */
	public void sendImg(String toJid,String imgURL,String thumbnailURL) throws XMPPException
	{
		String chatId = (String) smackClient.getParticipantToChatThread().get(toJid);
		if (chatId == null || chatId == "") {
			smackClient.createChat(toJid);
		}
		smackClient.sendImg(toJid, imgURL,thumbnailURL);
	}
	

	/**
	 * 监听消息
	 */
	public void setMessageListener() {
		MessageListener messageListener = new MessageListener() {
			public void processMessage(Chat ch, Message msg) {
				if(msg.getBody()!=null)
				{
					Collection<PacketExtension> ext = msg.getExtensions();
					Iterator<PacketExtension> extIt = ext.iterator();
					PacketExtension packetEX;
					String  xhtml = "";
					if(extIt.hasNext())
					{
						packetEX = extIt.next();
						System.out.println("message EXTENSION : "+packetEX.toXML());
						//xhtml = packetEX.toXML().replaceAll("\"","\\\\\"");
						xhtml = packetEX.toXML();
					}
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					System.out.println("RECI TIME:" + df.format(new Date()));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date date = null;
					DelayInformation inf = null;
					inf = (DelayInformation) msg.getExtension("x", "jabber:x:delay");
					if (inf != null) {
						date = inf.getStamp();
					} else {
						date = new Date();
					}
					System.out.println("+++++ " + msg.toXML());
					String datetime = sdf.format(date);
					String from = msg.getFrom();
					String name = getUserShowName(from);
					from = Function.getPureId(from);
					if(xhtml.length()>0)
					{
							TypeXHTML newTypeHTML = Function.TransferXHTML(xhtml);
							if((newTypeHTML.getType()!=null)&&(newTypeHTML.getType().equals("IMG")))
							{
								xhtml = newTypeHTML.getXhtml().replaceAll("\"","\\\\\"");
								
								String msgbody = xhtml;
								/*
								if((msg.getBody().substring(0,4) != null)&&(msg.getBody().substring(0,4).toLowerCase().equals("<img")))	//图片,无需转义<>符号
								{
									SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									Date date = null;
									DelayInformation inf = null;
									inf = (DelayInformation) msg
											.getExtension("x", "jabber:x:delay");
									if (inf != null) {
										date = inf.getStamp();
									} else {
										date = new Date();
									}
									System.out.println("+++++ " + msg.toXML());
									String datetime = sdf.format(date);
									String from = msg.getFrom();
									String name = getUserShowName(from);
									from = Function.getPureId(from);
									
									//String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime, transferHTMLImg(msg.getBody()));
									String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime, msg.getBody());
									if (cometdSession != null) {
										cometdSession.deliver(MessageService.getLocalSession(),
												"/kunlun/xmpp/client", messageJson, null);
									}
								}																										//文本,转义HTML
								else
								{
								*/
									
									
								//String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime, transferHTML(msg.getBody()));
								String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime, msgbody ,msg.getBody());
								if (cometdSession != null) {
									cometdSession.deliver(MessageService.getLocalSession(),	"/kunlun/xmpp/client", messageJson, null);
								}
							}
							else
							{
								String msgbody = msg.getBody();
								String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime, msgbody ,msg.getBody());
								if (cometdSession != null) {
									cometdSession.deliver(MessageService.getLocalSession(),	"/kunlun/xmpp/client", messageJson, null);
								}
							}
					}
					else
					{
						
						
							String msgbody = msg.getBody();
							//String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime, transferHTML(msg.getBody()));
							String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime, msgbody ,msg.getBody());
							if (cometdSession != null) {
								cometdSession.deliver(MessageService.getLocalSession(),	"/kunlun/xmpp/client", messageJson, null);
							}
					}
							
						} 
					}
		
			
		};
		smackClient.setMessageListerner(messageListener);
	}

	/**
	 * 监听会话
	 */
	public void setChatManagerListener() {
		ChatManagerListener chatManagerListener = new ChatManagerListener() {
			public void chatCreated(Chat chat, boolean createdLocally) {
				if (!createdLocally) {
					String participant = Function.getPureId(chat.getParticipant());
					smackClient.getParticipantToChatThread().put(participant,
							chat.getThreadID());
					MessageListener messageListerner = getMessageListener();
					chat.addMessageListener(messageListerner);
					// 1s时间，否则无法保留状态，原因比较复杂
					Thread t1 = new Thread();
					try {
						t1.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		};
		// smackClient.getConnection().getChatManager().addChatListener(chatManagerListener);
		smackClient.setChatManagerListener(chatManagerListener);
	}

	/**
	 * 监听群邀请
	 */
	public void setInvitationListener() {
		InvitationListener invitationListener = new InvitationListener() {
			@Override
			public void invitationReceived(Connection connection,String room,
					String inviter,String reason,String password,Message message)//reason就是群名
			{
				// TODO Auto-generated method stub
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String datetime = sdf.format(new Date());
				String inviterJid = Function.getPureId(inviter);
				List<Map> newMucRoster = new ArrayList<Map>();
				try {
					newMucRoster = addNewMuc(smackJid,room);
				} catch (XMPPException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				MultiUserChat muc = smackClient.getMUCInstance(room);
				
				
				
				String mucInvitationJson = JsonHandler.mucInvitationJson(room,smackJid,datetime,reason,newMucRoster);
				
				
				
				try {
					if (cometdSession != null) {
						cometdSession.deliver(MessageService.getLocalSession(),	"/kunlun/xmpp/client", mucInvitationJson, null);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					DiscussionHistory his = new DiscussionHistory();
					his.setMaxStanzas(0);
					
					//muc.join(name, null, his,SmackConfiguration.getPacketReplyTimeout());
					muc.join(Function.getUserName(smackJid), null, his,SmackConfiguration.getPacketReplyTimeout());
					
					//muc.join(name);
					
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Roster roster = smackClient.getRoster();
				String[] groups = { "Tuple" };
				try {
					roster.createEntry(room, reason, groups);
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		smackClient.setInvitationListener(invitationListener);
	}

	public List<Map> addNewMuc(String jid, String tupleJid) throws XMPPException  {
		// TODO Auto-generated method stub
		MultiUserChat muc = smackClient.getMUCInstance(tupleJid);
		// 消息监听
		
		PacketListener mucMessageListener = TupleSV.getMucMessageListener(this);
		// 房间加入退出监听

		ParticipantStatusListener participantStatusListener =TupleSV.getParticipantStatusListener(this, tupleJid, smackClient.getConnection());
		
		UserStatusListener userStatusListener = TupleSV.getUserStatusListener(this, tupleJid, smackClient.getConnection());
		
		
		smackClient.setMessageAndParticipantStatusListener(jid, tupleJid, muc,mucMessageListener, participantStatusListener, userStatusListener);

		

		List<Map> groupRosterList = new ArrayList<Map>();
		Collection<Affiliate> owners = muc.getOwners();
		Collection<Occupant> moderators = muc.getModerators();
		Iterator<Affiliate> ito = owners.iterator();
		while (ito.hasNext()) {
			Affiliate af = ito.next();
			Map map = new HashMap();
			map.put("jid", af.getJid());
			map.put("name", Function.getUserName(af.getJid()));
			map.put("subscription", "");
			map.put("tuple", tupleJid);
			// 是否在房间内,可由nickName来判断，若不为NULL则在房间中，否则不在
			map.put("nickName",Function.getModeratorNick(moderators, af.getJid()));
			groupRosterList.add(map);
		}
		
		return groupRosterList;
	}

	private String transferHTMLImg(String str)
	{
		str = str.replaceAll("\\\\", "\\\\\\\\");
		
		str = str.replaceAll("\"","\\\"");
		
		str = str.replaceAll("\n","<br>");
		str = str.replaceAll("\r","<br>");
		return str;
	}
	
	
	public MessageListener getMessageListener() {
		MessageListener messageListerner = new MessageListener() {
			public void processMessage(Chat ch, Message msg) {
				if(msg.getBody()!=null)
				{
					Collection<PacketExtension> ext = msg.getExtensions();
					Iterator<PacketExtension> extIt = ext.iterator();
					PacketExtension packetEX;
					String  xhtml = "";
					if(extIt.hasNext())
					{
						packetEX = extIt.next();
						System.out.println("message EXTENSION : "+packetEX.toXML());
						//xhtml = packetEX.toXML().replaceAll("\"","\\\\\"");
						
						xhtml = packetEX.toXML();
					}
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					System.out.println("RECI TIME:" + df.format(new Date()));
				
					SimpleDateFormat sdf = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
					Date date = null;
					DelayInformation inf = null;
					inf = (DelayInformation) msg.getExtension("x", "jabber:x:delay");
					if (inf != null) {
						date = inf.getStamp();
					} else {
						date = new Date();
					}
					System.out.println("////// " + msg.toXML());
					String datetime = sdf.format(date);
					String from = msg.getFrom();
					String name = getUserShowName(from);
					from = Function.getPureId(from);
					if(xhtml.length()>0)
					{
						TypeXHTML newTypeHTML = Function.TransferXHTML(xhtml);
						if((newTypeHTML.getType()!=null)&&(newTypeHTML.getType().equals("IMG")))
						{
								xhtml = newTypeHTML.getXhtml().replaceAll("\"","\\\\\"");
								String msgbody = xhtml;
								//String messageJson = JsonHandler.messageJson(from, smackJid, name, datetime, transferHTML(msgbody));
								String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime,msgbody,msg.getBody());
								try 
								{
									if (cometdSession != null) 
									{
										cometdSession.deliver(MessageService.getLocalSession(),	"/kunlun/xmpp/client", messageJson, null);
									}
								} 
								catch (Exception e)
								{
									e.printStackTrace();
								}
						}
						else
						{
							String msgbody = msg.getBody();
							String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime,msgbody,msg.getBody());
							try 
							{
								if (cometdSession != null) 
								{
									cometdSession.deliver(MessageService.getLocalSession(),	"/kunlun/xmpp/client", messageJson, null);
								}
							} 
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else
					{
							
							String msgbody = msg.getBody();
							//String messageJson = JsonHandler.messageJson(from, smackJid, name, datetime, transferHTML(msgbody));
							String messageJson = JsonHandler.messageJson(from, smackJid,name, datetime,msgbody,msg.getBody());
							try 
							{
								if (cometdSession != null) 
								{
									cometdSession.deliver(MessageService.getLocalSession(),	"/kunlun/xmpp/client", messageJson, null);
								}
							} 
							catch (Exception e)
							{
								e.printStackTrace();
							}
					}
				} 
			}
		};
		return messageListerner;
	}

	/**
	 * 获得用户名称
	 * 
	 * @param user
	 * @return
	 */
	private String getUserShowName(String user) {
		String ret = "";
		user = Function.getPureId(user);
		if(rosterCache!=null)
		{
			if(rosterCache.getEntry(user) != null)
			{
				ret =  rosterCache.getEntry(user).getName();
			}
			else
			{
				ret = Function.getUserName(user);
			}
		}
		else
		{
			ret = Function.getUserName(user);
			
		}
		return ret;
	}

	public Date getLastActiveTime() {
		return lastActiveTime;
	}

	public synchronized void setLastActiveTime(Date lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public Date getLoginTime() {
		return loginTime;
	}

	public synchronized void setLoginTime(Date loginTime) {
		this.loginTime = loginTime;
	}

	public String getSmackJid() {
		return smackJid;
	}

	public synchronized void setSmackJid(String smackJid) {
		this.smackJid = smackJid;
	}

	public String getWsId() {
		return wsId;
	}

	public synchronized void setWsId(String wsId) {
		this.wsId = wsId;
	}

	public String getWsSessionId() {
		return wsSessionId;
	}

	public synchronized void setWsSessionId(String wsSessionId) {
		this.wsSessionId = wsSessionId;
	}

	public SmackClient getSmackClient() {
		return smackClient;
	}

	public synchronized void setSmackClient(SmackClient smackClient) {
		this.smackClient = smackClient;
	}

	public ServerSession getCometdSession() {
		return cometdSession;
	}

	public synchronized void setCometdSession(ServerSession cometdSession) {
		this.cometdSession = cometdSession;
	}

	public List<ServerSession> getCometdSessionList() {
		return cometdSessionList;
	}

	public synchronized void setCometdSessionList(
			List<ServerSession> cometdSessionList) {
		this.cometdSessionList = cometdSessionList;
	}

	public int getWsStatus() {
		return wsStatus;
	}

	public synchronized void setWsStatus(int wsStatus) {
		this.wsStatus = wsStatus;
	}

	public int getXmppStatus() {
		return xmppStatus;
	}

	public synchronized void setXmppStatus(int xmppStatus) {
		this.xmppStatus = xmppStatus;
	}

	public List<String> getBrowserSessionList() {
		return browserSessionList;
	}

	public synchronized void setBrowserSessionList(
			List<String> browserSessionList) {
		this.browserSessionList = browserSessionList;
	}

	public Roster getRosterCache() {
		return rosterCache;
	}

	public synchronized void setRosterCache(Roster rosterCache) {
		this.rosterCache = rosterCache;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void getRosterForUpdate ()throws Exception {
		// TODO Auto-generated method stub
		
		Roster rosterForUpdate = smackClient.getRoster();
		//String rosterJson = JsonHandler.rosterJson("", roster);
		//对Roster按首字母进行分组
		String rosterJson = JsonHandler.rosterAlfaSortJsonForUpdate("", rosterForUpdate);
		String presenceInitJson = JsonHandler.presenceInitJson(rosterForUpdate);
		
		
		if (cometdSession != null) {
			System.out.println("srversession2:"+cometdSession);
			cometdSession.deliver(MessageService.getLocalSession(),
					"/kunlun/xmpp/client", rosterJson, null);
			cometdSession.deliver(MessageService.getLocalSession(),
					"/kunlun/xmpp/client", presenceInitJson, null);
			
		}
	}

	public void getAddsbook() {
		// TODO Auto-generated method stub
		
		try {
			TypeAddsbookView[] typeAddsbookView = addsbookDao.addsbookGetAddsbookListView(this.getWsSessionId());
			TypeAddsbook[] typeAddsbooks = addsbookDao.getAddsbookList(this.getWsSessionId());
			for(TypeAddsbook typeAddsbook :typeAddsbooks)
			{
				String addbookXML = addsbookDao.addsbookGet(this.getWsSessionId(), typeAddsbook.getId());
				
				System.out.println(addbookXML);
			}
			if((typeAddsbookView!=null)&&(typeAddsbookView.length>0))
			{
				String addsbookJson =  JsonHandler.AddsbookJson(typeAddsbookView);
			}
		} catch (ErrorResultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void SortXCardList()
	{
		if((xCardList != null)&&(xCardList.size()>0))
		{
			
		}
	}

}
