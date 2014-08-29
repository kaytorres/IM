package com.sj.weixin.web.controller;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;




import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.sj.kunlun.dao.AddsbookDao;
import com.sj.kunlun.dao.GlobalDao;
import com.sj.kunlun.dao.ManageDao;
import com.sj.kunlun.entity.type.TypeReturn;
import com.sj.kunlun.entity.type.TypeWebSession;
import com.sj.weixin.web.service.LoginService;
import com.sj.weixin.web.smack.common.Function;
import com.sj.weixin.web.smack.interfaces.ISmackService;
import com.sj.weixin.web.TesnsStatic;
import com.sj.weixin.web.cache.ClientManager;
import com.sj.weixin.web.common.JsonHandler;
import com.sj.weixin.web.entity.GlobalClient;
import com.sj.weixin.web.form.LoginForm;
import com.sj.weixin.web.model.dictionary.LoginStatus;
import com.sj.weixin.web.utils.DESUtility;
import com.sj.weixin.web.utils.JsonUtil;
import com.sj.weixin.web.utils.MatrixToImageWriter;
import com.sj.weixin.web.utils.TwoDimensionCode;

@Controller
public class LoginController extends BaseController{
	
	static String  TDC_TAG  = "Kunlun:";
	private static String xmppServer ="192.168.1.23";
	
	ManageDao manageDao ;
	private ISmackService iSmackService;

	@Autowired
	public void setISmackService(ISmackService iSmackService){
		this.iSmackService = iSmackService;
	}
	
	@Autowired
	public void setManageDao(ManageDao manageDao) {
		this.manageDao = manageDao;
		dao = manageDao;
	}
	
	private LoginService loginService;
	
	private static final Logger logger = Logger.getLogger(LoginController.class);
	public static String fullJid;
	public static String userid;
	public static String smackjid;
	
	private static final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	
	
	
	@Autowired
	public void setLoginService(LoginService loginService) {
		this.loginService = loginService;
	}
	
	//private static Map<String, String> user = new HashMap<String, String>();
	public static Map<String, UserClient> connection = new HashMap<String, UserClient>();
	public static ManageDao dao = null;
	
	@RequestMapping(value = { "", "/" ,"/login","/login/"})
	public String index(@RequestParam( value="lang",required = false) String lang) {
		if((lang != null)&&(lang.equals("zh_CN")))
		{
			return "/indexCN";
		}
		else if((lang != null)&&(lang.equals("zh_TW")))
		{
			return "/indexTW";
		}
		else if((lang != null)&&(lang.equals("en_US")))
		{
			return "/indexUS";
		}
		else
		{
			return "/indexCN";
		}
	}
	
	@RequestMapping(value = { "/getUUid"})
	public void getUUid(HttpServletResponse response,HttpServletRequest request,HttpSession session) throws IOException, Exception{
		
		String sessionId = request.getSession().getId();
		boolean userExistFlag = false;
		
		for(Entry<String, UserClient> entry:connection.entrySet())
		{
			if(entry.getKey().equals(sessionId))
			{
				switch(manageDao.manageWebSessionStatus(entry.getValue().getUuId()))
				{
					case 0:		
						break;
					case 1:
						break;
					case 2:		{
									userExistFlag = true;
									TypeWebSession typeSession = manageDao.manageWebSessionInfo(entry.getValue().getUuId());
									
									String loginToken = typeSession.getToken();
									//String loginInfo = DESUtility.decrypt(loginToken, entry.getValue().getUuId());
									String keyText = entry.getValue().getUuId();
									keyText = DESUtility.getMD5(keyText);
									keyText = keyText.substring(0, 8);
									String loginInfo = DESUtility.decryptDES(DESUtility.parseHexStr2Byte(loginToken),keyText );
									String loginJid = loginInfo.split(" ")[0];
									String loginPass = loginInfo.split(" ")[1];
									
									//String loginJid = "yar5@fe.shenj.com";
									//String loginPass = "123456";
									
									try {
										if(ClientManager.getClient(loginJid)!=null)
										{
											//ClientManager.getClient(loginJid).logout();
											//ClientManager.removeClient(loginJid);
											this.login(loginJid, loginPass, xmppServer, 0, 1, 1, sessionId,session);
										}
										else
										{
											this.login(loginJid, loginPass, xmppServer, 1, 1, 1, sessionId,session);
										}
									} catch (Exception e) {
										
										e.printStackTrace();
									}
									UserClient value = connection.get(sessionId);
									value.setSmackJid(loginJid);
									if((ClientManager.getClient(loginJid).getName()!=null)&&(ClientManager.getClient(loginJid).getName().length()>0))
									{
										value.setLastConn(new Date());
										value.setSmackName(ClientManager.getClient(loginJid).getName());
										value.setAddsbookJson(JsonHandler.AddsBookListJson(ClientManager.getClient(loginJid).getAddsbookList()));
										value.setAvatarBASE64(ClientManager.getClient(loginJid).getAvatarBASE64());
										value.setxCardSortJson(JsonHandler.XCardListSortJson(ClientManager.getClient(loginJid).getxCardList()));
									}
									else
									{
										value.setSmackName(Function.getNickName(loginJid));
									}
									connection.put(sessionId, value);
									Map<String, Object> map = new HashMap<String, Object>();
									map.put("smackjid", entry.getValue().getSmackJid());
									map.put("smackname", entry.getValue().getSmackName());
									map.put("status", "2");
									map.put("uuid", entry.getValue().getUuId());
									map.put("addsbook", entry.getValue().getAddsbookJson());
									map.put("avatar", entry.getValue().getAvatarBASE64());
									map.put("xcardsort",entry.getValue().getxCardSortJson());
									System.out.println(session);
									String json = "";
									json = JsonUtil.map2json(map);
									this.outputJsonData(response, json);
								}
						break;
					case 3:
						break;
				}
				
				break;
			}
			
			
			
			
			/*
			if(entry.getKey().equals(sessionId)&&(entry.getValue().getStatus().equals("2")))
			{
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("smackjid", entry.getValue().getSmackJid());
				map.put("smackname", entry.getValue().getSmackName());
				map.put("status", "2");
				map.put("uuid", entry.getValue().getUuId());
				map.put("addsbook", entry.getValue().getAddsbookJson());
				map.put("avatar", entry.getValue().getAvatarBASE64());
				map.put("xcardsort",entry.getValue().getxCardSortJson());
				System.out.println(session);
				String json = "";
				json = JsonUtil.map2json(map);
				try {
					ClientManager.getClient(entry.getValue().getSmackJid()).logout();
					ClientManager.removeClient(entry.getValue().getSmackJid());
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(entry.getValue().getSmackJid() != null)
				{
					String loginJid = entry.getValue().getSmackJid();
					String pwd =entry.getValue().getSmackPassword();
					
					this.login(loginJid, pwd, xmppServer, 1, 1, 1, sessionId,session);
					UserClient value = connection.get(sessionId);
					if((ClientManager.getClient(loginJid).getName()!=null)&&(ClientManager.getClient(loginJid).getName().length()>0))
					{
						value.setSmackName(ClientManager.getClient(loginJid).getName());
						value.setAddsbookJson(JsonHandler.AddsBookListJson(ClientManager.getClient(loginJid).getAddsbookList()));
						value.setAvatarBASE64(ClientManager.getClient(loginJid).getAvatarBASE64());
						value.setxCardSortJson(JsonHandler.XCardListSortJson(ClientManager.getClient(loginJid).getxCardList()));
					}
					else
					{
						value.setSmackName(Function.getNickName(loginJid));
						
					}
					
					value.setSession(session);
					connection.put(sessionId, value);
				}
			
				this.outputJsonData(response, json);
				userExistFlag = true;
				break;
				
			}
			*/
		}
		
		if(!userExistFlag)
		{
			Iterator iterator = connection.entrySet().iterator();
			while(iterator.hasNext())
			{
	            Map.Entry<String,UserClient> entry =  (Entry<String, UserClient>) iterator.next();
	            String key = entry.getKey();
	            if(key.equals(sessionId))
	            {
	            	iterator.remove();
	            }
			}
		
			//user.put("sessionId", sessionId);
			
			//String uuId = df.format(new Date()) + String.valueOf(++lastId);
			String uuId = manageDao.manageWebSessionGet();
			System.out.println(uuId);
			String json = "";
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("UUID", uuId);
			UserClient userClient = new UserClient();
			userClient.setLastConn(new Date());
			userClient.setUuId(uuId);
			userClient.setStatus("0");
			userClient.setSession(session);
			System.out.println(session);
			connection.put(sessionId, userClient);
			
			json = JsonUtil.map2json(map);
			this.outputJsonData(response, json);
			
		}
	}
	
	@RequestMapping(value = { "/getQRImg" })
	public void getQRImg(@RequestParam("uuid") String uuid,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session) throws WriterException {
		String content  =  TDC_TAG  + uuid;
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		//Map hints = new HashMap();
		//Hashtable hints = new Hashtable();
		
		//hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();     

        // 指定纠错等级     

        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);     

        // 指定编码格式     

        hints.put(EncodeHintType.CHARACTER_SET, "GBK");    
		BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 299, 299);
		BufferedImage buffimag = MatrixToImageWriter.toBufferedImage(bitMatrix);
		
		//BufferedImage buffimag = TwoDimensionCode.qRCodeCommon(uuid,"png",3);
		ByteArrayOutputStream out = new ByteArrayOutputStream();  
		try {
			ImageIO.write(buffimag, "gif", out);
			byte[] image = out.toByteArray();  
			response.getOutputStream().write(image);
		} catch (IOException e1) {
			e1.printStackTrace();
		}  
   
	}
	
	@RequestMapping(value = { "/getPoll"})
	public void getPoll(@RequestParam("uuid") String uuid,
			@RequestParam("tip") String tip,
			HttpServletResponse response,HttpSession session,HttpServletRequest request){
		String sessionId = request.getSession().getId();
		Map<String, Object> map = new HashMap<String, Object>();
		
		String status = "";
		
		if(connection.get(sessionId) != null)
		{
			int count =0;
			while(true){
				try 
				{
					if("1".equals(tip))
					{
						if((connection.get(sessionId) != null)&&(connection.get(sessionId).getUuId()!=null)&&(!connection.get(sessionId).getUuId().equals("")))
						{
							if(manageDao.manageWebSessionStatus(connection.get(sessionId).getUuId()).equals(1))	//已扫描
							{
								status = "1";
								UserClient temp = connection.get(sessionId);
								temp.setLastConn(new Date());
								temp.setStatus("1");
								connection.put(sessionId, temp);
								//status = connection.get(sessionId).getStatus();
								
								map.put("status", status);
								
								String json = JsonUtil.map2json(map);
								this.outputJsonData(response, json);
								
								break;
							}else if(count == 11)
							{
								break;
							}
							
						}
						
						/*
						if((connection.get(sessionId).getUuId()!=null)&&(!connection.get(sessionId).getUuId().equals(""))&&(connection.get(sessionId).getUuId().equals(uuid))&&(connection.get(sessionId).getStatus().equals("1"))	)
						{
							status = connection.get(sessionId).getStatus();
						
							break;
						
						}else if(count==11){
								break;
							}
						*/
						Thread.sleep(1000L);
						count++;
					}else if("0".equals(tip))
					{
						
						if((connection.get(sessionId)!=null)&&(connection.get(sessionId).getUuId()!=null)&&(!connection.get(sessionId).getUuId().equals("")))
						{
							if(manageDao.manageWebSessionStatus(connection.get(sessionId).getUuId()).equals(2))//已验证
							{
								status = "2";
								TypeWebSession typeSession = manageDao.manageWebSessionInfo(connection.get(sessionId).getUuId());
								
								String loginToken = typeSession.getToken();
								//String loginInfo = DESUtility.decrypt(loginToken, connection.get(sessionId).getUuId());
								String keyText = connection.get(sessionId).getUuId();
								keyText = DESUtility.getMD5(keyText);
								keyText = keyText.substring(0, 8);
								String loginInfo = DESUtility.decryptDES(DESUtility.parseHexStr2Byte(loginToken),keyText );
								String loginJid = loginInfo.split(" ")[0];
								String loginPass = loginInfo.split(" ")[1];

								//String loginJid = "yar5@fe.shenj.com";
								//String loginPass = "123456";
								
								try {
									if(ClientManager.getClient(loginJid)!=null)
									{
										//ClientManager.getClient(loginJid).logout();
										//ClientManager.removeClient(loginJid);
										this.login(loginJid, loginPass, xmppServer, 0, 1, 1, sessionId,session);
									}
									else
									{
										this.login(loginJid, loginPass, xmppServer, 1, 1, 1, sessionId,session);
									}
								} catch (Exception e) {
									
									e.printStackTrace();
								}
								
								UserClient value = connection.get(sessionId);
								value.setSmackJid(loginJid);
								if((ClientManager.getClient(loginJid).getName()!=null)&&(ClientManager.getClient(loginJid).getName().length()>0))
								{
									value.setStatus("2");
									value.setLastConn(new Date());
									value.setSmackName(ClientManager.getClient(loginJid).getName());
									value.setAddsbookJson(JsonHandler.AddsBookListJson(ClientManager.getClient(loginJid).getAddsbookList()));
									value.setAvatarBASE64(ClientManager.getClient(loginJid).getAvatarBASE64());
									value.setxCardSortJson(JsonHandler.XCardListSortJson(ClientManager.getClient(loginJid).getxCardList()));
								}
								else
								{
									value.setSmackName(Function.getNickName(loginJid));
								}
								connection.put(sessionId, value);
								Iterator iterator = connection.entrySet().iterator();
								while(iterator.hasNext())
								{
	
						             Map.Entry<String,UserClient> entry =  (Entry<String, UserClient>) iterator.next();
						             String key = entry.getKey();
						             UserClient entryvalue = entry.getValue();
						             if((entryvalue.getSmackJid()!=null)&&(entryvalue.getSmackJid().equals(loginJid)))
						             {
						            	 if(!key.equals(sessionId))
						            	 {
						            		 iterator.remove();
						            	 }
						             }
								}
								
								map.put("status", status);
								
								if(connection.get(sessionId).getSmackJid()!=null)
								{
									map.put("smackjid", connection.get(sessionId).getSmackJid());
								}
								if(connection.get(sessionId).getSmackName()!=null)
								{
									map.put("smackname", connection.get(sessionId).getSmackName());
								}
								if(connection.get(sessionId).getAddsbookJson()!=null)
								{
									map.put("addsbook", connection.get(sessionId).getAddsbookJson());
								}
								if(connection.get(sessionId).getAvatarBASE64()!=null)
								{
									map.put("avatar", connection.get(sessionId).getAvatarBASE64());
								}
								if(connection.get(sessionId).getxCardSortJson()!=null)
								{
									map.put("xcardsort", connection.get(sessionId).getxCardSortJson());
								}
								//user.remove(uuid);
								String json = JsonUtil.map2json(map);
								this.outputJsonData(response, json);
								break;
							}
						
						
						
						/*
						if((connection.get(sessionId).getUuId()!=null)&&(!connection.get(sessionId).getUuId().equals(""))&&(connection.get(sessionId).getUuId().equals(uuid))&&(connection.get(sessionId).getStatus().equals("2")))
						{
							status = connection.get(sessionId).getStatus();
							
							String loginJid = connection.get(sessionId).getSmackJid();
							String loginPass = connection.get(sessionId).getSmackPassword();
							
							try {
								if(ClientManager.getClient(loginJid)!=null)
								{
									ClientManager.getClient(loginJid).logout();
									ClientManager.removeClient(loginJid);
								}
							} catch (Exception e) {
								
								e.printStackTrace();
							}
							
							this.login(loginJid, loginPass, xmppServer, 1, 1, 1, sessionId,session);
							UserClient value = connection.get(sessionId);
							value.setSmackJid(loginJid);
							if((ClientManager.getClient(loginJid).getName()!=null)&&(ClientManager.getClient(loginJid).getName().length()>0))
							{
								value.setSmackName(ClientManager.getClient(loginJid).getName());
								value.setAddsbookJson(JsonHandler.AddsBookListJson(ClientManager.getClient(loginJid).getAddsbookList()));
								value.setAvatarBASE64(ClientManager.getClient(loginJid).getAvatarBASE64());
								value.setxCardSortJson(JsonHandler.XCardListSortJson(ClientManager.getClient(loginJid).getxCardList()));
							}
							else
							{
								value.setSmackName(Function.getNickName(loginJid));
							}
							connection.put(sessionId, value);
							
							
								 Iterator iterator = connection.entrySet().iterator();
								 while(iterator.hasNext())
								 {
	
						              Map.Entry<String,UserClient> entry =  (Entry<String, UserClient>) iterator.next();
						              String key = entry.getKey();
						              UserClient entryvalue = entry.getValue();
						              if((entryvalue.getSmackJid()!=null)&&(entryvalue.getSmackJid().equals(loginJid)))
						              {
						            	  if(!key.equals(sessionId))
						            	  {
						            		  iterator.remove();
						            	  }
						              }
								 }
								
							*/
							
							
							
						}else if(count==11){
							break;
						}
						Thread.sleep(1000L);
						count++;
					}
					
					
					
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		}
		else
		{
			map.put("status", 4);								//session超时，页面刷新
			String json = JsonUtil.map2json(map);
			this.outputJsonData(response, json);
		}
	}
	
	@RequestMapping(value = {"/confirmConn"})
	public void confirmConn(@RequestParam("uuid") String uuid,HttpServletRequest request,HttpServletResponse response) throws Exception//网页确认链接
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		String sessionId = request.getSession().getId();
		if(connection.get(sessionId)!=null)
		{
			if((manageDao.manageWebSessionStatus(uuid)!=null)&&(manageDao.manageWebSessionStatus(uuid).equals(2)))
			{
				map.put("status", "connect");
				UserClient value = connection.get(sessionId);
				value.setLastConn(new Date());
				connection.put(sessionId, value);
			}
			else
			{
				map.put("status", "disconnect");
				Iterator iterator = connection.entrySet().iterator();
				while(iterator.hasNext())
				{

		            Map.Entry<String,UserClient> entry =  (Entry<String, UserClient>) iterator.next();
		            String key = entry.getKey();
		            UserClient entryvalue = entry.getValue();
		            if(key.equals(sessionId))
		            {
		            	iterator.remove();
		            }
		             
				}
			}
		}
		else
		{
			map.put("status", "disconnect");
		}
		
		
		
		/*
		String sessionId = request.getSession().getId();
		boolean sessionExist = false;
		if(connection.get(sessionId)!=null)
		{
			sessionExist = true;
			if(connection.get(sessionId).getStatus().equals("2"))
			{
				map.put("status", "connect");
			}
			else
			{
				map.put("status", "disconnect");
			}
		}
		else
		{
			map.put("status", "disconnect");
		}*/
		String json = JsonUtil.map2json(map);
		this.outputJsonData(response, json);
	}
	
	@RequestMapping(value = { "/confirmPoll"}, method=RequestMethod.POST)
	public void confirmPoll(//手机确认登录
			@RequestParam("uuid") String uuid,
			@RequestParam("jid") String id,
			HttpServletResponse response) throws IOException{
		
		for(Entry<String, UserClient> entry:connection.entrySet())
		{
			if(entry.getValue().getUuId().equals(uuid))
			{
				if(entry.getValue().getSmackJid().equals(id))
				{
					if(entry.getValue().getStatus().equals("1"))
					{
						UserClient value = entry.getValue();
						value.setStatus("2");
						connection.put(entry.getKey(), value);
						PrintWriter out = response.getWriter();
						out.print("Confirm Valid");
		                out.close();
						break;
						
					}
				}
				else
				{
					PrintWriter out = response.getWriter();
					out.print("Confirm Invalid");
	                out.close();
				}
			}
		}
	}
	
	@RequestMapping(value = { "/sendPoll"}, method=RequestMethod.POST)
	public void sendPoll(@RequestParam("uuid") String uuid,
			@RequestParam("jid") String id,
			@RequestParam("pass") String pass,
			HttpServletResponse response) throws IOException{//手机扫描
		//user.put(uuid, id);
		boolean loginSuccessFlag = false;
		String loginSuccessSessionId = null;
		String loginSuccessJid = null;
		for(Entry<String, UserClient> entry:connection.entrySet())
		{
			if(entry.getValue().getUuId().equals(uuid))
			{
				if(entry.getValue().getStatus().equals("0"))
				{
					UserClient value = entry.getValue();
					value.setSmackJid(id);
					value.setStatus("1");
					value.setSmackPassword(pass);
					connection.put(entry.getKey(), value);
					PrintWriter out = response.getWriter();
					out.print("Scan Valid");
	                out.close();
					/*
					if(id.equals("1"))
					{
						UserClient value = entry.getValue();
						value.setSmackJid("yyyy@fe.shenj.com");
						value.setSmackName("yyyy");
						value.setStatus("1");
						connection.put(entry.getKey(), value);
					}
					else
					{
						UserClient value = entry.getValue();
						value.setSmackJid("yar5@fe.shenj.com");
						value.setSmackName("yar5");
						value.setStatus("1");
						connection.put(entry.getKey(), value);
					}
					*/
					
					break;
				}
				else
				{
					if(entry.getValue().getStatus().equals("1"))
					{
						UserClient value = entry.getValue();
						if(value.getSmackJid().equals(id))
						{
							PrintWriter out = response.getWriter();
							out.print("Scan Valid");
			                out.close();
			                break;
						}
						else
						{
							PrintWriter out = response.getWriter();
							out.print("Scan Invalid");
			                out.close();
			                break;
						}
						
						
						
						
						/*
						UserClient value = entry.getValue();
						value.setStatus("2");
						connection.put(entry.getKey(), value);
						break;
						*/
					}
				}
			}
		}
	}
		

	//登录函数
	/**
	 * @param jid 用户jid
	 * @param pwd 用户密码
	 * @param xmppServer xmpp服务器地址
	 * @param isCreateClientint 是否新建GlobalClient   0-不新建  1-新建
	 * @param isConnWs  是否登录ws服务器  0-不登录  1-登录
	 * @param isConnSmack 是否登录xmpp服务器 0-不登录  1-登录
	 * @throws Exception 
	 * return LoginStatus.DISCONN.getValue()-登录失败  LoginStatus.HASLOGIN.getValue()- 登陆成功
	 */
	public String login(String jid, String pwd,String xmppServer,int isCreateClientint ,int isConnWs,int isConnSmack,String sessionId,HttpSession session){
		
		String id = null;
		String smackJid = null;
		GlobalClient client;		
		//xmppServer ="fe.shenj.com";
		
		
		//处理GlobalClient
		if(isCreateClientint == 1){
			client = ClientManager.createClient(jid);
		}else{
			client = ClientManager.getClient(jid);	
		}
		
		//如果登录ws服务器
		
		if(isConnWs == 1){
			try {
				client.setWsStatus(1);
				id = loginService.login(jid);
				//id = loginService.login("yyy@server09.shenj.com");
				if(id == null){
					client.setWsStatus(LoginStatus.DISCONN.getValue());	//登录失败
				}else{					
					client.setWsStatus(LoginStatus.HASLOGIN.getValue());	//登录成功
					client.setWsSessionId(id);
				}

			} catch (Exception e) {
				client.setWsStatus(LoginStatus.DISCONN.getValue());	//登录失败
				e.printStackTrace();
			}
			
		}	
		
		
		//如果登录xmpp服务器
		if(isConnSmack == 1){
			try {
				client.setXmppStatus(LoginStatus.CONNECTING.getValue());
				smackJid = iSmackService.login(jid, pwd, xmppServer);
				
				if(smackJid == null){
					client.setXmppStatus(LoginStatus.DISCONN.getValue()); //登录失败
				}else{
					client.setXmppStatus(LoginStatus.HASLOGIN.getValue());//登录成功
					
					System.out.print("XMPP LOGIN SUCCESS");
				}
			} catch (Exception e) {
				client.setXmppStatus(LoginStatus.DISCONN.getValue()); //登录失败
				e.printStackTrace();
			}		
		}	
		
		//如果登录成功 添加http的sessionid 返回状态LoginStatus.HASLOGIN.getValue()
		if(client.getWsStatus() ==LoginStatus.HASLOGIN.getValue() && client.getXmppStatus() == LoginStatus.HASLOGIN.getValue()){
			//if(client.getXmppStatus() == LoginStatus.HASLOGIN.getValue()){	
			List<String> list = new ArrayList<String>(); 
			list = client.getBrowserSessionList();
			list.add(sessionId);	
			client.setBrowserSessionList(list);
			Date now = new Date();
			
			//设置登录时间和最后活跃时间
			client.setLoginTime(now);
			client.setLastActiveTime(now);
			
			session.setAttribute(TesnsStatic.AUTHORIZATION_KEY, id);
			session.setAttribute(TesnsStatic.AUTHORIZATION_LOGIN_ID, id);
			session.setAttribute(TesnsStatic.SMACK_JID, smackJid);	
			String smackName = null;
			try {
				smackName = iSmackService.getName(smackJid);
				
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			session.setAttribute(TesnsStatic.SMACK_NAME,smackName);
			
			
			/*	//开启线程，监控最后活跃时间
			ActiveMonitor activeMonitor = new ActiveMonitor(jid,session);
			Thread t = new Thread(activeMonitor);
			t.start();
			
			TypeUser user;
			try {
				if(jid!=null){
					user = userProfileService.select(jid);
					client.setName(user.getNickname());		
				}
							
			} catch (ErrorResultException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}		*/	
			userid = jid;
			smackjid =smackJid;
			return jid+"&"+smackJid;
		}
		//登录失败 返回状态LoginStatus.DISCONN.getValue()
		else{
			return null;
		}
	}
}
