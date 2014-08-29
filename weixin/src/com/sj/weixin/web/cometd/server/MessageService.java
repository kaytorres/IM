package com.sj.weixin.web.cometd.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.coobird.thumbnailator.Thumbnails;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.jivesoftware.smack.XMPPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import sun.misc.BASE64Decoder;

import com.sj.kunlun.dao.AddsbookDao;
import com.sj.weixin.web.cache.ClientManager;
import com.sj.weixin.web.entity.GlobalClient;
import com.sj.weixin.web.interfaces.ITupleSV;
import com.sj.weixin.web.service.TupleSV;
import com.sj.weixin.web.smack.common.Function;
import com.sj.weixin.web.smack.entity.SmackClient;

@Service
public class MessageService {
	@Inject
	private static BayeuxServer bayeuxServer;
	@Session
	private static LocalSession sender;
	
	private static String TEMP_IMG_FOLD = "D:\\temp";
	private static String TEMP_IMG_NAME = "temp_";
	private static String THUMBNAIL_IMG_NAME = "thumnail_";
	private static String IMG_UPLOAD_URL = "http://172.30.3.107:3080/kunlunMedia/uploadFiles.htm"; 
	

	@Listener("/kunlun/xmpp/server")
	public void processClientHello(ServerSession session, ServerMessage message)
			throws Exception {
		System.out.printf("Received '%s' from remote client %s%n", message.getData(), session.getId());
		
		
		String messageTrans =message.getData().toString().replaceAll("\\n", "\\\\n");
//		for(int i=0;i<message.getData().toString().length();i++){
//			char ch = message.getData().toString().charAt(i);
//			if(ch=='r'){
//				System.out.println(ch);
//				
//			}
//		}
		
		JSONObject json = JSONObject.fromObject(messageTrans);
//		JSONObject json = JSONObject.fromObject(message.getData());
		
		String type = json.getString("packagetype");
		if ("login".equals(type)) {
			handleLogin(session, json);
		}
		if ("message".equals(type)) {
			try {
			handleMessage(session, json);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		if("chatImg".equals(type))
		{
			handleChatImg(session,json);
		}
		if ("presence".equals(type)) {
			try {
			handlePresence(session, json);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		if ("status".equals(type)) {
			try {
			handleStatus(session, json);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
		if("getroster".equals(type))
		{
			try {
				handleGetRoster(session, json);
				} catch(Exception e) {
					e.printStackTrace();
					throw e;
				}
		}
		if ("muciq".equals(type)) {
			handleMUCIq(session, json);
		}
		if ("mucroster".equals(type)) {
			handleMUCRoster(session, json);
		}
		if ("mucmessage".equals(type)) {
			handleMUCMessage(session, json);
		}
		if("mucImg".equals(type))
		{
			handleMUCImg(session,json);
		}
	}

	
	private String transferHTMLImg(String str)
	{
		
		str = str.replaceAll("\\\\", "\\\\\\\\");
		str = str.replaceAll("\"","\\\\\"");
		str = str.replaceAll("\n","<br>");
		str = str.replaceAll("\r","<br>");
		return str;
	}
	
	private void handleMUCMessage(ServerSession session, JSONObject json) {
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String fromJid = Function.getPureId(info.getString("from"));
		String toJid = Function.getPureId(info.getString("to"));
		String msg = info.getString("body");
		//msg = transferHTML(msg);
		ITupleSV groupSV = TupleSV.getInstance();
		try {
			groupSV.sendMUCMessage(fromJid, toJid, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleMUCImg(ServerSession session,JSONObject json) throws IOException
	{
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String fromJid = Function.getPureId(info.getString("from"));
		String fromName = Function.getUserName(fromJid);
		String toJid = Function.getPureId(info.getString("to"));
		String BASE64CODE = info.getString("body");
		String imgType = info.getString("imgtype");
		String outFilePath = TEMP_IMG_FOLD;  
	    String outFileName = TEMP_IMG_NAME+fromName+"."+imgType;
	    String thumbnailName = THUMBNAIL_IMG_NAME+fromName+"."+imgType;
	    
	    GenerateImage(BASE64CODE,outFilePath+"\\"+outFileName);
	    Thumbnails.of(outFilePath+"\\"+outFileName).size(240, 320).toFile(outFilePath+"\\"+thumbnailName);
	    File file = new File(outFilePath+"\\"+outFileName);
	    File thumbnailfile = new File(outFilePath+"\\"+thumbnailName);
	    PostMethod filePost = new PostMethod(IMG_UPLOAD_URL);
	    try 
	    {
			Part[] parts = { new FilePart(file.getName(), file), new FilePart(thumbnailfile.getName(), thumbnailfile) };
			filePost.setRequestEntity(new MultipartRequestEntity(parts,	filePost.getParams()));
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(filePost);
			if (status == HttpStatus.SC_OK) 
			{
				System.out.println("上传成功");// 上传成功
				String uploadXML = filePost.getResponseBodyAsString();
				System.out.println(uploadXML);	
				InputStream   in_nocode   =   new   ByteArrayInputStream(uploadXML.getBytes("GBK")); 
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		        DocumentBuilder builder = factory.newDocumentBuilder();  
				Document document = builder.parse(in_nocode);  
				Element element = document.getDocumentElement();  
				NodeList fileNodes = element.getChildNodes();
				String imgURL = "";
				String thumbnailURL = "";
				int imgSize = 0;
				int thumbnailSize = 0;
				int ind = 0;
				for(int i=0;i<fileNodes.getLength();i++)
				{
					if(fileNodes.item(i) instanceof Element)
					{
						if(ind == 0)
						{
							Element fileElement = (Element) fileNodes.item(i);  
							NodeList childEle = fileElement.getChildNodes();
							for(int j=0;j<childEle.getLength();j++)
							{
								if(childEle.item(j) instanceof Element)
								{
									Element childElement = (Element) childEle.item(j);  
									if(childElement.getTagName().equals("path"))
									{
										Node val = childElement.getChildNodes().item(0);
										imgURL = val.getNodeValue();
									}
									if(childElement.getTagName().equals("size"))
									{
										Node val = childElement.getChildNodes().item(0);
										imgSize = Integer.parseInt(val.getNodeValue());
									}
								}
							}
							ind++;
						}
						else
						{
							Element fileElement = (Element) fileNodes.item(i);  
							NodeList childEle = fileElement.getChildNodes();
							for(int j=0;j<childEle.getLength();j++)
							{
								if(childEle.item(j) instanceof Element)
								{
									Element childElement = (Element) childEle.item(j);  
									if(childElement.getTagName().equals("path"))
									{
										Node val = childElement.getChildNodes().item(0);
										thumbnailURL = val.getNodeValue();
									}
									if(childElement.getTagName().equals("size"))
									{
										Node val = childElement.getChildNodes().item(0);
										thumbnailSize = Integer.parseInt(val.getNodeValue());
									}
								}
							}
							break;
						}
					}
				}
				
				if( imgSize > 0)			//有数据的图片
				{
					//String msg = "<img class=\"chatImg\" src=\""+imgURL+"\" />";
					//msg = transferHTMLImg(msg);
					
					ITupleSV groupSV = TupleSV.getInstance();
					try {
						groupSV.sendMUCImg(fromJid, toJid, imgURL,thumbnailURL );
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
				}
				
				
			} 
			else 
			{
				System.out.println("上传失败");
					// 上传失败
			}
			
			file.delete();
			thumbnailfile.delete();
	    } catch (Exception ex) 
	    {
			ex.printStackTrace();
		} finally 
		{
			filePost.releaseConnection();
		}
		
		
		
		/*
		msg = transferHTMLImg(msg);
		ITupleSV groupSV = TupleSV.getInstance();
		try {
			groupSV.sendMUCMessage(fromJid, toJid, msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
	
	private void handleChatImg(ServerSession session,JSONObject json) throws IOException
	{
		
		
		
		
		
		
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String fromJid = Function.getPureId(info.getString("from"));
		String fromName = Function.getUserName(fromJid);
		String toJid = Function.getPureId(info.getString("to"));
		String imgType = info.getString("imgtype");
		String BASE64CODE = info.getString("body");
		String outFilePath = TEMP_IMG_FOLD;  
	    String outFileName = TEMP_IMG_NAME+fromName+"."+imgType;
	    String thumbnailName = THUMBNAIL_IMG_NAME+fromName+"."+imgType;
	    
	    GenerateImage(BASE64CODE,outFilePath+"\\"+outFileName);
	    Thumbnails.of(outFilePath+"\\"+outFileName).size(240, 320).toFile(outFilePath+"\\"+thumbnailName);
	    File file = new File(outFilePath+"\\"+outFileName);
	    File thumbnailfile = new File(outFilePath+"\\"+thumbnailName);
	    PostMethod filePost = new PostMethod(IMG_UPLOAD_URL);
	    try 
	    {
			Part[] parts = { new FilePart(file.getName(), file), new FilePart(thumbnailfile.getName(), thumbnailfile) };
			filePost.setRequestEntity(new MultipartRequestEntity(parts,	filePost.getParams()));
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(filePost);
			if (status == HttpStatus.SC_OK) 
			{
				System.out.println("上传成功");// 上传成功
				String uploadXML = filePost.getResponseBodyAsString();
				System.out.println(uploadXML);	
				InputStream   in_nocode   =   new   ByteArrayInputStream(uploadXML.getBytes("GBK")); 
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		        DocumentBuilder builder = factory.newDocumentBuilder();  
				Document document = builder.parse(in_nocode);  
				Element element = document.getDocumentElement();  
				NodeList fileNodes = element.getChildNodes();
				String imgURL = "";
				String thumbnailURL = "";
				int imgSize = 0;
				int thumbnailSize = 0;
				int ind = 0;
				for(int i=0;i<fileNodes.getLength();i++)
				{
					if(fileNodes.item(i) instanceof Element)
					{
						if(ind == 0)
						{
							Element fileElement = (Element) fileNodes.item(i);  
							NodeList childEle = fileElement.getChildNodes();
							for(int j=0;j<childEle.getLength();j++)
							{
								if(childEle.item(j) instanceof Element)
								{
									Element childElement = (Element) childEle.item(j);  
									if(childElement.getTagName().equals("path"))
									{
										Node val = childElement.getChildNodes().item(0);
										imgURL = val.getNodeValue();
									}
									if(childElement.getTagName().equals("size"))
									{
										Node val = childElement.getChildNodes().item(0);
										imgSize = Integer.parseInt(val.getNodeValue());
									}
								}
							}
							ind++;
						}
						else
						{
							Element fileElement = (Element) fileNodes.item(i);  
							NodeList childEle = fileElement.getChildNodes();
							for(int j=0;j<childEle.getLength();j++)
							{
								if(childEle.item(j) instanceof Element)
								{
									Element childElement = (Element) childEle.item(j);  
									if(childElement.getTagName().equals("path"))
									{
										Node val = childElement.getChildNodes().item(0);
										thumbnailURL = val.getNodeValue();
									}
									if(childElement.getTagName().equals("size"))
									{
										Node val = childElement.getChildNodes().item(0);
										thumbnailSize = Integer.parseInt(val.getNodeValue());
									}
								}
							}
							break;
						}
					}
				}
				
				if( imgSize > 0)			//有数据的图片
				{
					
					
					
					//String msg = "<img class=\"chatImg\" src=\""+imgURL+"\" />";
					//msg = transferHTMLImg(msg);
					GlobalClient globalClient = ClientManager.getClient(fromJid);
					try {
						globalClient.sendImg(toJid, imgURL,thumbnailURL);
						//globalClient.sendMessage(toJid, msg);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
				
			} 
			else 
			{
				System.out.println("上传失败");
					// 上传失败
			}
			
			
			file.delete();
			thumbnailfile.delete();
			
	    } catch (Exception ex) 
	    {
			ex.printStackTrace();
		} finally 
		{
			filePost.releaseConnection();
		}
		
		/*
		msg = transferHTMLImg(msg);
		GlobalClient client = ClientManager.getClient(fromJid);
		try {
			client.sendMessage(toJid, msg);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public static boolean GenerateImage(String imgStr, String imgFilePath) {// 对字节数组字符串进行Base64解码并生成图片  
		if (imgStr == null) // 图像数据为空  
		return false;  
		BASE64Decoder decoder = new BASE64Decoder();  
		try {  
		// Base64解码  
		byte[] bytes = decoder.decodeBuffer(imgStr);  
		for (int i = 0; i < bytes.length; ++i) {  
		if (bytes[i] < 0) {// 调整异常数据  
		bytes[i] += 256;  
		}  
		}  
		// 生成jpeg图片  
		OutputStream out = new FileOutputStream(imgFilePath);  
		out.write(bytes);  
		out.flush();  
		out.close();  
		return true;  
		} catch (Exception e) {  
		return false;  
		}  
		}  
	
	private void handleMessage(ServerSession session, JSONObject json) {
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String fromJid = Function.getPureId(info.getString("from"));
		String toJid = Function.getPureId(info.getString("to"));
		String msg = info.getString("body").replaceAll("\\r","\r");
		//msg = transferHTML(msg);
		GlobalClient client = ClientManager.getClient(fromJid);
		try {
			client.sendMessage(toJid, msg);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}
	
	private void handleStatus(ServerSession session, JSONObject json)
	{
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String smackJid = Function.getPureId(info.getString("from"));
		GlobalClient client = ClientManager.getClient(smackJid);
		SmackClient smackClent = client.getSmackClient();
		String status = info.getString("status");
		smackClent.UpdateStatus(status);
	}
	
	private void handlePresence(ServerSession session, JSONObject json)
	{
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String smackJid = Function.getPureId(info.getString("from"));
		GlobalClient client = ClientManager.getClient(smackJid);
		SmackClient smackClent = client.getSmackClient();
		String presence = info.getString("presence");
		
		smackClent.UpdatePresence(presence);
		
	}
	
	private void handleGetRoster(ServerSession session, JSONObject json)
	{
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String smackJid = Function.getPureId(info.getString("jid"));
		GlobalClient client = ClientManager.getClient(smackJid);
		try {
			client.getRosterForUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	private void handleLogin(ServerSession session, JSONObject json) {
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String smackJid = Function.getPureId(info.getString("jid"));
		GlobalClient client = ClientManager.getClient(smackJid);
		if(client.getName()==null)
		{
			client.setName(Function.getUserName(client.getSmackJid()));
		}
		client.setCometdSession(session);
		
		try {
			client.getRoster();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ITupleSV tupleSV = TupleSV.getInstance();
		try {
			tupleSV.joinAllTuples(smackJid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
		try{
			client.getAddsbook();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		*/
		
		
	}

	private void handleMUCIq(ServerSession session, JSONObject json) {
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String jid = Function.getPureId(info.getString("from"));
		String type = info.getString("type");
		ITupleSV tupleSV = TupleSV.getInstance();
		if ("login".equals(type)) {
			try {
			//	tupleSV.getTuplesJson(jid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if ("create".equals(type)) {
			JSONArray bodyArray = info.getJSONArray("body");
			JSONObject body = bodyArray.getJSONObject(0);
			String tupleJid = body.getString("id");
			String tupleSubject = body.getString("title");
			String tupleName = body.getString("name");
			List<String> memberList = new ArrayList<String>();
			JSONArray members = body.getJSONArray("roster");

			for (int i = 0; i < members.size(); i++) {
				JSONObject member = members.getJSONObject(i);
				String memberJid = member.getString("jid");
				memberList.add(memberJid);
			}

			try {
				tupleSV.createTupleJson(jid, tupleJid, tupleName, tupleSubject,
						memberList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if ("invite".equals(type)) {
			JSONArray bodyArray = info.getJSONArray("body");
			JSONObject body = bodyArray.getJSONObject(0);
			String tupleJid = body.getString("id");
			String tupleName = body.getString("name");
			List<String> memberList = new ArrayList<String>();
			JSONArray members = body.getJSONArray("roster");
			for (int i = 0; i < members.size(); i++) {
				JSONObject member = members.getJSONObject(i);
				String memberJid = member.getString("jid");
				memberList.add(memberJid);
			}
			
			try {
				tupleSV.TupleInviteJson(jid, tupleJid,tupleName,	memberList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		if("kick".equals(type)) {
			JSONArray bodyArray = info.getJSONArray("body");
			JSONObject body = bodyArray.getJSONObject(0);
			String tupleJid = body.getString("id");
			String memJid = body.getString("memJid");
			try {
				tupleSV.TupleKickJson(jid, tupleJid,	memJid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		
		
		if ("invitationResponse".equals(type)) {
			// 处理邀请回复
			String tupleJid = info.getString("tuple");
			String response = info.getString("response");
			String message = info.getString("message");
			String inviter = info.getString("to");

			try {
				tupleSV.invitationResponse(jid, inviter, tupleJid, response,
						message);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void handleMUCRoster(ServerSession session, JSONObject json) {
		JSONArray data = json.getJSONArray("data");
		JSONObject info = data.getJSONObject(0);
		String jid = Function.getPureId(info.getString("from"));
		ITupleSV groupSV = TupleSV.getInstance();
		try {
			groupSV.getAllTupleRostersJson(jid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendToChannle(Map data, String channelName)
			throws Exception {
		ServerChannel channel = bayeuxServer.getChannel(channelName);
		channel.publish(sender, data, null);
	}

	public static BayeuxServer getBayeuxServer() {
		return bayeuxServer;
	}

	public static LocalSession getLocalSession() {
		return sender;
	}
}