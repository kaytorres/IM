package com.sj.weixin.web.service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cometd.bayeux.server.ServerSession;
import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.UserStatusListener;
import org.jivesoftware.smackx.packet.DelayInformation;

import com.sj.weixin.web.cache.ClientManager;
import com.sj.weixin.web.cometd.server.MessageService;
import com.sj.weixin.web.common.JsonHandler;
import com.sj.weixin.web.entity.GlobalClient;
import com.sj.weixin.web.entity.SuperMucClient;
import com.sj.weixin.web.entity.TypeXHTML;
import com.sj.weixin.web.interfaces.ITupleSV;
import com.sj.weixin.web.smack.common.Function;
import com.sj.weixin.web.smack.entity.SmackClient;

public class TupleSV implements ITupleSV {

	private static final TupleSV instance = new TupleSV();

	private TupleSV() {
	};

	public static TupleSV getInstance() {
		return instance;
	}

	@Override
	public List<Map> getTuples(GlobalClient client) throws Exception {
		List<Map> roomList = new ArrayList<Map>();
		XMPPConnection conn = client.getSmackClient() == null ? null : client
				.getSmackClient().getConnection();

		SmackClient smackClient = client.getSmackClient();
		if (smackClient == null) {
			return null;
		}
		Roster roster = smackClient.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		for (RosterEntry entry : entries) {
			Collection<RosterGroup> groups = entry.getGroups();
			for (RosterGroup group : groups) {
				if ("Tuple".equals(group.getName())) {
					String jid = entry.getUser();
					
					
					MultiUserChat muc = joinTuple(client, jid);


				}
			}
		}

		return roomList;
	}

	@Override
	public void getTuplesJson(String jid) throws Exception {
		GlobalClient client = ClientManager.getClient(jid);
		if (client != null) {
			List<Map> groupList = getTuples(client);
			if (groupList != null) {
				String groupJson = JsonHandler
						.tupleJson(groupList, "", jid, "");
				ServerSession cometdSession = client.getCometdSession();
				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", groupJson, null);
				}
			}
		}
	}

	@Override
	public void joinAllTuples(String jid) throws Exception {
		GlobalClient client = ClientManager.getClient(jid);
		System.out.println("开始加入群");
		if (client != null) {
			XMPPConnection conn = client.getSmackClient() == null ? null
					: client.getSmackClient().getConnection();

			SmackClient smackClient = client.getSmackClient();
			if (smackClient == null) {
				return;
			}
			Roster roster = smackClient.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
				Collection<RosterGroup> groups = entry.getGroups();
				for (RosterGroup group : groups) {
					if ("Tuple".equals(group.getName())) {
						String tmpJid = entry.getUser();
						joinTuple(client, tmpJid);

					}
				}
			}
		}
	}

	@Override
	public List<Map> getTupleRosters(GlobalClient client, String tupleJid)
			throws Exception {
		List<Map> groupRosterList = new ArrayList<Map>();
		SmackClient smackClient = client.getSmackClient();
		if (smackClient == null) {
			return null;
		}

		MultiUserChat muc = smackClient.getMUCInstance(tupleJid);
		System.out.println(muc.getRoom());
		try{
		Collection<Affiliate> owners = muc.getOwners();
		Collection<Occupant> moderators = muc.getModerators();
		Iterator<Affiliate> ito = owners.iterator();
		while (ito.hasNext()) {
			Affiliate af = ito.next();
			Map map = new HashMap();
			map.put("jid", af.getJid());
			map.put("name", Function.getUserName(af.getJid()));// af.getNick()！！！！！！！！！！！！！！！！！！！！！！
			map.put("subscription", "");
			map.put("tuple", tupleJid);
			// 是否在房间内,可由nickName来判断，若不为NULL则在房间中，否则不在
			map.put("nickName",Function.getModeratorNick(moderators, af.getJid()));

			groupRosterList.add(map);
		}

		
		}
		catch(XMPPException e)
		{
			
		}
		return groupRosterList;
	}

	@Override
	public void getTupleRostersJson(String jid, String tupleJid)
			throws Exception {
		GlobalClient client = ClientManager.getClient(jid);
		if (client != null) {
			List<Map> groupRosterList = getTupleRosters(client, tupleJid);
			if (groupRosterList != null) {
				String groupRosterJson = JsonHandler
						.tupleRosterJson(groupRosterList);
				ServerSession cometdSession = client.getCometdSession();
				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", groupRosterJson, null);
				}

			}
		}

	}

	public void getAllTupleRostersJson(String jid) throws Exception {
		GlobalClient client = ClientManager.getClient(jid);
		SmackClient smackClient = client.getSmackClient();
		List<Map> allTupleRosterList = new ArrayList<Map>();
		if (client != null) {
			Roster roster = smackClient.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
				Collection<RosterGroup> groups = entry.getGroups();
				for (RosterGroup group : groups) {
					if ("Tuple".equals(group.getName())) {
						String tupleJid = entry.getUser();
						List<Map> tupleRosterList = getTupleRosters(client,
								tupleJid);
						allTupleRosterList.addAll(tupleRosterList);
					}
				}
			}
			if (allTupleRosterList != null) {
				String groupRosterJson = JsonHandler
						.tupleRosterJson(allTupleRosterList);
				ServerSession cometdSession = client.getCometdSession();
				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", groupRosterJson, null);
				}

				// 所有成员当前状态
				String allMucPresenceJson = JsonHandler.mucPresenceInitJson(
						jid, allTupleRosterList);
				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", allMucPresenceJson, null);
				}

			}
		}

	}

	/*
	@Override
	public void joinTuple(String jid, String nickname, String password,
			String roomJid) throws Exception {
		final GlobalClient client = ClientManager.getClient(jid);
		if (client != null) {
			SmackClient smackClient = client.getSmackClient();
			if (smackClient != null) {
				XMPPConnection connection = smackClient.getConnection();
				MultiUserChat muc = smackClient.getMUCInstance(roomJid);
				muc.join(nickname, password);
				// 消息监听
				PacketListener mucMessageListener = this
						.getMucMessageListener(client);
				// 房间加入退出监听
				ParticipantStatusListener participantStatusListener = this
						.getParticipantStatusListener(client, roomJid, connection);
				smackClient.setMessageAndParticipantStatusListener(roomJid,
						muc, mucMessageListener, participantStatusListener);
			}
		}

	}
*/

	

	@Override
	public void sendMUCImg(String jid, String roomJid, String imgURL,
			String thumbnailURL) throws Exception {
		GlobalClient client = ClientManager.getClient(jid);
		if (client != null) {
			SmackClient smackClient = client.getSmackClient();
			if (smackClient != null) {
				MultiUserChat muc = smackClient.getMUCInstance(roomJid);
				String message = "[图片]";
				String exXML = "<html xmlns='http://jabber.org/protocol/xhtml-im'>"+
				"<body xmlns='http://www.w3.org/1999/xhtml'>"+
				"<p><a href=\""+imgURL+"\"><img src=\""+thumbnailURL+"\"/></a></p>"+
				"</body>"+
				"</html>";
				Message messageXML = new Message(muc.getRoom(), Message.Type.groupchat,message,exXML);
				
				muc.sendMessage(messageXML);
			}

		}
		
	}

	@Override
	public void sendMUCMessage(String jid, String roomJid, String message)
			throws Exception {
		GlobalClient client = ClientManager.getClient(jid);
		if (client != null) {
			SmackClient smackClient = client.getSmackClient();
			if (smackClient != null) {
				MultiUserChat muc = smackClient.getMUCInstance(roomJid);
				
				String exXML = "<html xmlns='http://jabber.org/protocol/xhtml-im'>"+
				"<body xmlns='http://www.w3.org/1999/xhtml'>"+
				"<p>"+message+"</p>"+
				"</body>"+
				"</html>";
				
				Message messageXML = new Message(muc.getRoom(), Message.Type.groupchat,message,exXML);
				
				muc.sendMessage(messageXML);
			}

		}
	}

	@Override
	public MultiUserChat joinTuple(GlobalClient client, String roomJid)
			throws Exception {
		MultiUserChat muc = null;
		if (client != null) {
			SmackClient smackClient = client.getSmackClient();
			if (smackClient != null) {
				XMPPConnection connection = smackClient.getConnection();
				muc = smackClient.getMUCInstance(roomJid);

				DiscussionHistory his = new DiscussionHistory();
				// his.setSince(since);
				his.setMaxStanzas(0);
				// his.setSeconds(60);

				// JUST FOR TESTING
				//client.setName(Function.getUserName(client.getSmackJid()));
				if (smackClient.getConnection().isConnected()) {
					if (!muc.isJoined()) {
						//muc.join(URLEncoder.encode(client.getName(), "utf-8"), null, his,SmackConfiguration.getPacketReplyTimeout());// muc.join(client.getName())
						muc.join(Function.getUserName(client.getSmackJid()), null, his,SmackConfiguration.getPacketReplyTimeout());// muc.join(client.getName())
						
						//muc.join(Function.getUserName(client.getSmackJid()), null, his,SmackConfiguration.getPacketReplyTimeout());// muc.join(client.getName())
						//muc.join("yyyy", null, his,SmackConfiguration.getPacketReplyTimeout());// muc.join(client.getName())
						
					}
				}
				Iterator<String> iter = muc.getOccupants();
				while(iter.hasNext())
				{
					System.out.println("--------------");
					System.out.println(iter.next());
					System.out.println("--------------");
				}
				// 消息监听
				PacketListener mucMessageListener = this.getMucMessageListener(client);
				// 房间加入退出监听
				ParticipantStatusListener participantStatusListener = this.getParticipantStatusListener(client, roomJid, connection);
				UserStatusListener userStatusListener = this.getUserStatusListener(client, roomJid, connection);
				smackClient.setMessageAndParticipantStatusListener(client.getSmackJid(), roomJid,	muc, mucMessageListener, participantStatusListener, userStatusListener);

			}
		}
		return muc;
	}

	public void createTupleJson(String jid, String tupleJid, String tupleName,
			String tupleSubject, List<String> memberList) throws Exception {
		GlobalClient client = ClientManager.getClient(jid);
		int success = this.createTuple(client, tupleJid, tupleName,
				tupleSubject, memberList);
		if (success == 0) {
			ServerSession cometdSession = client.getCometdSession();
			String createTupleJsonStr = JsonHandler.createTupleJson("true",
					tupleJid, jid);
			if (cometdSession != null) {
				cometdSession.deliver(MessageService.getLocalSession(),
						"/kunlun/xmpp/client", createTupleJsonStr, null);
			}
		} else {
			ServerSession cometdSession = client.getCometdSession();
			String createTupleJsonStr = JsonHandler.createTupleJson("false",
					tupleJid, jid);
			if (cometdSession != null) {
				cometdSession.deliver(MessageService.getLocalSession(),
						"/kunlun/xmpp/client", createTupleJsonStr, null);
			}
		}
	}

	@Override
	public int createTuple(GlobalClient client, String tupleJid,
			String tupleName, String tupleSubject, List<String> memberList)
			throws Exception {
		System.out.println("开始创建群");
		// TODO Auto-generated method stub
		if (client == null) {
			return 1;
		}
		SmackClient smackClient = client.getSmackClient();
		if (smackClient == null) {
			return 1;
		}
		XMPPConnection connection = smackClient.getConnection();
		if (connection == null) {
			return 1;
		}
		try {
			
			MultiUserChat muc = new MultiUserChat(connection, tupleJid);
			System.out.println(muc.hashCode());
			
			//muc.create(URLEncoder.encode(client.getName(), "utf-8"));
			//muc.create("yyyy");
			muc.create(Function.getUserName(client.getSmackJid()));
			Form form = muc.getConfigurationForm();
			Form submitForm = form.createAnswerForm();

			for (Iterator<FormField> fields = form.getFields(); fields
					.hasNext();) {
				FormField field = (FormField) fields.next();
				if (!FormField.TYPE_HIDDEN.equals(field.getType())
						&& field.getVariable() != null) {
					submitForm.setDefaultAnswer(field.getVariable());
				}
			}
			
			
			submitForm.setAnswer("muc#roomconfig_roomname", tupleName);
			submitForm.setAnswer("muc#roomconfig_membersonly", true);
			//submitForm.setAnswer("muc#roomconfig_roomsecret", "reserved");
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			//submitForm.setAnswer("muc#roomconfig_allowinvites", true);
			List<String> presencebroadcast = new ArrayList<String>();
			presencebroadcast.add("Moderator");
			presencebroadcast.add("Participant");
			//submitForm.setAnswer("muc#roomconfig_presencebroadcast", presencebroadcast);
			//submitForm.setAnswer("muc#roomconfig_whois", "anyone");
			
			muc.sendConfigurationForm(submitForm);

			
			
			
			
			//smackClient.putMUCInstance(tupleJid, muc);
			// ////////////////////////--等待房间创建--/////////////////////////////////
			int times = 0;
			Thread t1 = new Thread();
			while (times < 1) {
				if (muc.getSubject() != null) {
					break;
				}
				t1.sleep(1000);
				times++;
			}

			
			
	
			
			
			// 增加邀请拒绝监听器
			/*
			muc.addInvitationRejectionListener(new InvitationRejectionListener() {
				public void invitationDeclined(String invitee, String reason) {
					try {
						muc.revokeOwnership(invitee);
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
*/
			muc.changeSubject(tupleSubject);
			for(String memJid : memberList)
			{
				muc.invite(memJid, tupleName);
			}
			
			muc.grantOwnership(memberList);// 会自动执行邀请。
			//
			// for(String user:memberList){
			// muc.invite(user, null);
			// System.out.println("YAO QING "+user);
			// }

			
			
			joinTuple(client, tupleJid);
			/*
			
			muc.join(client.getName());
			PacketListener mucMessageListener = this.getMucMessageListener(client);
			//muc.addMessageListener(mucMessageListener);

			ParticipantStatusListener participantStatusListener = this.getParticipantStatusListener(client, tupleJid, connection);
			//muc.addParticipantStatusListener(participantStatusListener);

			
			muc = new MultiUserChat(connection, tupleJid);
			
			Occupant op = muc.getOccupant("yyy");
			
			Iterator<String> iter = muc.getOccupants();
			while(iter.hasNext())
			{
				
				System.out.println((String)iter.next());
			}
			
			
			
			smackClient.setMessageAndParticipantStatusListener(tupleJid,muc, mucMessageListener, participantStatusListener);
			
			*/
			
			
			Roster roster = smackClient.getRoster();
			String[] groups = { "Tuple" };
			roster.createEntry(tupleJid, tupleName, groups);
			return 0;
		} catch (Exception e) {
			throw e;
		}

	}

	public void mucInvite(GlobalClient client, String tupleJid)
	{
		SmackClient smackClient = client.getSmackClient();
		MultiUserChat muc = smackClient.getMUCInstance(tupleJid);
		muc.invite(tupleJid,"invite");
	}
	
	
	
	// 群消息监听
	public static PacketListener getMucMessageListener(final GlobalClient client) {
		PacketListener packetListener = new PacketListener() {
			public void processPacket(Packet packet) {
				Message message = (Message) packet;
				// System.out.println(message.getBody());
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date date = null;
				DelayInformation inf = null;
				inf = (DelayInformation) packet.getExtension("x",
						"jabber:x:delay");
				if (inf != null) {
					date = inf.getStamp();
				} else {
					date = new Date();
				}
				// System.out.println("--- "+packet.toXML());

				String datetime = sdf.format(date);
				String from = message.getFrom();
				String name = Function.getNickName(from);
				String to = Function.getPureId(from);
				String body = message.getBody();
				if(body != null)
				{
					Collection<PacketExtension> ext = message.getExtensions();
					Iterator<PacketExtension> extIt = ext.iterator();
					PacketExtension packetEX;
					String  xhtml = "";
					if(extIt.hasNext())
					{
						packetEX = extIt.next();
						System.out.println("message EXTENSION : "+packetEX.toXML());
						xhtml = packetEX.toXML();
					}
					
					if(xhtml.length()>0)
					{
						TypeXHTML newTypeHTML = Function.TransferXHTML(xhtml);
						if((newTypeHTML.getType()!=null)&&(newTypeHTML.getType().equals("IMG")))
						{
							xhtml = newTypeHTML.getXhtml().replaceAll("\"","\\\\\"");
							String messageBody = xhtml;
							String messageJson = JsonHandler.MUCMessageJson(from, to, name,	datetime, messageBody,message.getBody());
							ServerSession cometdSession = client.getCometdSession();
	
							if (cometdSession != null) {
								cometdSession.deliver(MessageService.getLocalSession(),
										"/kunlun/xmpp/client", messageJson, null);
							}
						}
						else
						{
							String messageBody = message.getBody();
							String messageJson = JsonHandler.MUCMessageJson(from, to, name,	datetime, messageBody,message.getBody());
							ServerSession cometdSession = client.getCometdSession();

							if (cometdSession != null) {
								cometdSession.deliver(MessageService.getLocalSession(),
										"/kunlun/xmpp/client", messageJson, null);
							}
						}

					}
					else
					{
						String messageBody = message.getBody();
						String messageJson = JsonHandler.MUCMessageJson(from, to, name,	datetime, messageBody,message.getBody());
						ServerSession cometdSession = client.getCometdSession();

						if (cometdSession != null) {
							cometdSession.deliver(MessageService.getLocalSession(),
									"/kunlun/xmpp/client", messageJson, null);
						}
					}
				}
			}
		};
		return packetListener;
	}

	// 群成员状态监听
	public static ParticipantStatusListener getParticipantStatusListener(
			final GlobalClient client, final String  tupleJid, final XMPPConnection connection) {
		ParticipantStatusListener participantStatusListener = new DefaultParticipantStatusListener() {

			@Override
			public void joined(String participant) {
				super.joined(participant);
				
				MultiUserChat muc =   ClientManager.getClient(client.getSmackJid()).getSmackClient().getMUCInstance(tupleJid);
				
				System.out.println(muc.getRoom());
				System.out.println(muc.hashCode());

				System.out.println("OccupantsCount: " + muc.getOccupantsCount());
				//MultiUserChat mucNow = new MultiUserChat(connection, muc.getRoom());
				try {
					Iterator<String> iter = muc.getOccupants();
					while(iter.hasNext())
					{
						System.out.println("--------------");
						System.out.println(iter.next());
						System.out.println("--------------");
					}
					
					Collection<Occupant> moderators = muc.getModerators();
					for(Occupant moderator : moderators)
					{
						if(moderator.getNick().equals(Function.getNickName(participant)))
						{
							String memberJid = moderator.getJid();
							String from = muc.getRoom();
							String to = Function.getPureId(client.getSmackJid());
							String nickName = Function.getNickName(participant);
							String type = "available";
							String mucPresenceListenJson = JsonHandler
									.mucPresenceListenJson(from, to, memberJid,
											nickName, type);

							ServerSession cometdSession = client.getCometdSession();

							if (cometdSession != null) {
								cometdSession.deliver(MessageService.getLocalSession(),
										"/kunlun/xmpp/client", mucPresenceListenJson,
										null);
							}
						}
					}
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*
				
				Iterator<String> iter = mucNow.getOccupants();
				while(iter.hasNext())
				{
					System.out.println(iter.next());
				}
				
				Occupant op = mucNow.getOccupant(participant);
				
				
				
				
				
				
				if (op != null) {
					String memberJid = Function.getPureId(op.getJid());
					String from = muc.getRoom();
					String to = Function.getPureId(client.getSmackJid());
					String nickName = Function.getNickName(participant);
					String type = "available";
					String mucPresenceListenJson = JsonHandler
							.mucPresenceListenJson(from, to, memberJid,
									nickName, type);

					ServerSession cometdSession = client.getCometdSession();

					if (cometdSession != null) {
						cometdSession.deliver(MessageService.getLocalSession(),
								"/kunlun/xmpp/client", mucPresenceListenJson,
								null);
					}
				}
				else
				{
					String memberJid = null;
					String from = Function.getRoomName(participant);
					String to = Function.getPureId(client.getSmackJid());
					String nickName = Function.getNickName(participant);
					String type = "available";
					String mucPresenceListenJson = JsonHandler
							.mucPresenceListenJson(from, to, memberJid,
									nickName, type);

					ServerSession cometdSession = client.getCometdSession();

					if (cometdSession != null) {
						cometdSession.deliver(MessageService.getLocalSession(),
								"/kunlun/xmpp/client", mucPresenceListenJson,
								null);
					}
				}
				*/
				System.out.println(participant + "加入");
			}

			@Override
			public void left(String participant) {
				
				super.left(participant);
				
				
				MultiUserChat muc =   ClientManager.getClient(client.getSmackJid()).getSmackClient().getMUCInstance(tupleJid);
				
				Occupant op = muc.getOccupant(participant);
				if (op != null) {
					String memberJid = Function.getPureId(op.getJid());
					String from = muc.getRoom();
					String to = Function.getPureId(client.getSmackJid());
					String nickName = Function.getNickName(participant);
					String type = "unavailable";
					String mucPresenceListenJson = JsonHandler
							.mucPresenceListenJson(from, to, memberJid,
									nickName, type);

					ServerSession cometdSession = client.getCometdSession();

					if (cometdSession != null) {
						cometdSession.deliver(MessageService.getLocalSession(),
								"/kunlun/xmpp/client", mucPresenceListenJson,
								null);
					}
				} else {
					String memberJid = null;
					String from = Function.getRoomName(participant);
					String to = Function.getPureId(client.getSmackJid());
					String nickName = Function.getNickName(participant);
					String type = "unavailable";
					String mucPresenceListenJson = JsonHandler
							.mucPresenceListenJson(from, to, memberJid,
									nickName, type);

					ServerSession cometdSession = client.getCometdSession();

					if (cometdSession != null) {
						cometdSession.deliver(MessageService.getLocalSession(),
								"/kunlun/xmpp/client", mucPresenceListenJson,
								null);
					}
				}

				System.out.println(participant + "离开");
			}
			
			
			@Override
			public void kicked(String paramString1, String paramString2, String paramString3 )
			{
				MultiUserChat muc =   ClientManager.getClient(client.getSmackJid()).getSmackClient().getMUCInstance(tupleJid);
				
			}


			@Override
			  public  void voiceGranted(String paramString)
			{
				System.out.println("voiceGranted");
			}
			
			@Override
			  public  void voiceRevoked(String paramString)
			{
				System.out.println("voiceRevoked");
			}
			
			@Override
			  public  void banned(String paramString1, String paramString2, String paramString3)
			{
				System.out.println("banned");
			}
			@Override
			  public  void membershipGranted(String paramString)
			{
				System.out.println("membershipGranted");
			}
			@Override
			  public  void membershipRevoked(String paramString)
			{
				System.out.println("membershipRevoked");
			}
			@Override
			  public  void moderatorGranted(String paramString)
			{
				System.out.println(paramString +"        moderatorGranted");
			}
			@Override
			public  void moderatorRevoked(String participant)
			{
				//MultiUserChat muc =   ClientManager.getClient(client.getSmackJid()).getSmackClient().getMUCInstance(tupleJid);
				
				String memberJid = null;
				String from = Function.getRoomName(participant);
				String to = Function.getPureId(client.getSmackJid());
				String nickName = Function.getNickName(participant);
				String type = "moderatorRevoked";
				String mucPresenceListenJson = JsonHandler.mucPresenceListenJson(from, to, memberJid,nickName, type);
				
				
				ServerSession cometdSession = client.getCometdSession();

				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", mucPresenceListenJson,
							null);
				}
				
				
				
				
				System.out.println(participant+"          moderatorRevoked");
			}
			@Override
			  public  void ownershipGranted(String paramString)
			{
				System.out.println("ownershipGranted");
			}
			@Override
			  public  void ownershipRevoked(String paramString)
			{
				System.out.println("ownershipRevoked");
			}
			@Override
			  public  void adminGranted(String paramString)
			{
				System.out.println("adminGranted");
			}
			@Override
			  public  void adminRevoked(String paramString)
			{
				System.out.println("adminRevoked");
			}
			@Override
			  public  void nicknameChanged(String paramString1, String paramString2)
			{
				System.out.println("nicknameChanged");
			}
			

		};
		return participantStatusListener;
	}
	
	
	// 群成员状态监听
	public static UserStatusListener getUserStatusListener(
			final GlobalClient client, final String  tupleJid, final XMPPConnection connection) {
		UserStatusListener userStatusListener = new DefaultUserStatusListener() {
			@Override
			public void kicked(String actor, String reason)
			  {
				String to = Function.getPureId(client.getSmackJid());
				String type = "moderatorRevoked";
				String mucModeratorRevokedJson = JsonHandler.mucModeratorRevokedJson(tupleJid, to,  type);
				
				client.getSmackClient().MUCMapRemove(tupleJid);
				client.getSmackClient().RosterRemove(tupleJid);
				
				ServerSession cometdSession = client.getCometdSession();

				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", mucModeratorRevokedJson,
							null);
				}
				
				
				
				
				
				System.out.println("I am  kicked");
			  }
			@Override
			  public void voiceGranted()
			  {
				System.out.println("I am  voiceGranted");
			  }
			@Override
			  public void voiceRevoked()
			  {
				System.out.println("I am  voiceRevoked");
			  }
			@Override
			  public void banned(String actor, String reason)
			  {
				System.out.println("I am  banned");
			  }
			@Override
			  public void membershipGranted()
			  {
				System.out.println("I am  membershipGranted");
			  }
			@Override
			  public void membershipRevoked()
			  {
				System.out.println("I am  membershipRevoked");
			  }
			@Override
			  public void moderatorGranted()
			  {
				System.out.println("I am  moderatorGranted");
			  }
			@Override
			  public void moderatorRevoked()
			  {
				String to = Function.getPureId(client.getSmackJid());
				String type = "moderatorRevoked";
				String mucModeratorRevokedJson = JsonHandler.mucModeratorRevokedJson(tupleJid, to,  type);
				
				client.getSmackClient().MUCMapRemove(tupleJid);
				client.getSmackClient().RosterRemove(tupleJid);
				
				ServerSession cometdSession = client.getCometdSession();

				if (cometdSession != null) {
					cometdSession.deliver(MessageService.getLocalSession(),
							"/kunlun/xmpp/client", mucModeratorRevokedJson,
							null);
				}
				
				
				
				
				
				
				
				
				
				
				System.out.println("I am  moderatorRevoked");
			  }
			@Override
			  public void ownershipGranted()
			  {
				
				
				
				
				
				
				System.out.println("I am  ownershipGranted");
			  }
			@Override
			  public void ownershipRevoked()
			  {
				System.out.println("I am  ownershipRevoked");
			  }
			@Override
			  public void adminGranted()
			  {
				System.out.println("I am  adminGranted");
			  }
			@Override
			  public void adminRevoked()
			  {
				System.out.println("I am  adminRevoked");
			  }
		};
		return userStatusListener;
	}
	
	
	

	@Override
	public void invitationResponse(String jid, String inviter, String tupleJid,
			String response, String message) throws Exception {
		// TODO Auto-generated method stub
		GlobalClient client = ClientManager.getClient(jid);
		SmackClient smackClient = client.getSmackClient();
		if (smackClient == null) {
			return;
		}
		XMPPConnection connection = smackClient.getConnection();
		if (connection == null) {
			return;
		}
		if ("yes".equals(response)) {
		/*	MultiUserChat muc = smackClient.getMUCInstance(tupleJid);
			// 消息监听
			PacketListener mucMessageListener = this
					.getMucMessageListener(client);
			// 房间加入退出监听
			ParticipantStatusListener participantStatusListener = this
					.getParticipantStatusListener(client, tupleJid, connection);
			smackClient.setMessageAndParticipantStatusListener(tupleJid, muc,
					mucMessageListener, participantStatusListener);

			muc.join(client.getName());
			Roster roster = smackClient.getRoster();
			String[] groups = { "Tuple" };
			roster.createEntry(tupleJid, muc.getSubject(), groups);*/
		} else {
			MultiUserChat.decline(connection, tupleJid, inviter, message);
		}
	}

	
	@Override
	public int TupleInvite(GlobalClient client, String tupleJid,String tupleName, List<String> memberList)
	throws Exception 
	{
		System.out.println("开始群内添加成员");
		// TODO Auto-generated method stub
		if (client == null) {
			return 1;
		}
		SmackClient smackClient = client.getSmackClient();
		if (smackClient == null) {
			return 1;
		}
		XMPPConnection connection = smackClient.getConnection();
		if (connection == null) {
			return 1;
		}
		try {
			
			MultiUserChat muc = smackClient.getMUCInstance(tupleJid);
			for(String member : memberList)
			{
				muc.invite(member, tupleName);
			}
			muc.grantOwnership(memberList);// 会自动执行邀请。
		
			return 0;
		} catch (Exception e) {
			throw e;
		}
		
		
	}
	
	
	@Override
	public void TupleInviteJson(String jid, String tupleJid,String tupleName,
			List<String> memberList) {
		// TODO Auto-generated method stub
		GlobalClient client = ClientManager.getClient(jid);
		try {
			int success = this.TupleInvite(client, tupleJid, tupleName , memberList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
	@Override
	public int  TupleKick(GlobalClient client, String tupleJid, String memJid) throws Exception
	{
		System.out.println("开始踢除成员");
		if (client == null) {
			return 1;
		}
		SmackClient smackClient = client.getSmackClient();
		if (smackClient == null) {
			return 1;
		}
		XMPPConnection connection = smackClient.getConnection();
		if (connection == null) {
			return 1;
		}
		try {
			
			
			MultiUserChat muc = smackClient.getMUCInstance(tupleJid);
			Collection<Occupant> occupants = muc.getModerators();
			
		
			
			for(Occupant occupant : occupants)
			{
				if(occupant.getJid().equals(memJid))
				{
					muc.revokeModerator(occupant.getNick());
					
					muc.kickParticipant(occupant.getNick(), "kick");
					break;
				}
			}
			muc.revokeOwnership(memJid);
			
			
			muc.revokeMembership(memJid);
			
			
			return 0;
		} catch (Exception e) {
			throw e;
		}
		
		
		
		
	}
	
	
	@Override
	public void TupleKickJson(String jid, String tupleJid, String memJid) {
		// TODO Auto-generated method stub
		GlobalClient client = ClientManager.getClient(jid);
		try {
			int success = this.TupleKick(client, tupleJid,   memJid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
