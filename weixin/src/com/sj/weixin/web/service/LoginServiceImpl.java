package com.sj.weixin.web.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;



import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.misc.BASE64Encoder;





import com.sj.kunlun.dao.AddsbookDao;
import com.sj.kunlun.dao.GlobalDao;
import com.sj.kunlun.dao.UserDao;
import com.sj.kunlun.dao.UserProfileDao;
import com.sj.kunlun.entity.type.TypeAddsbook;
import com.sj.kunlun.entity.type.TypeUser;
import com.sj.kunlun.exception.ErrorResultException;
import com.sj.weixin.web.cache.ClientManager;
import com.sj.weixin.web.entity.AddsBook;
import com.sj.weixin.web.entity.Folder;
import com.sj.weixin.web.entity.GlobalClient;
import com.sj.weixin.web.entity.XCard;

public class LoginServiceImpl implements LoginService{

	private GlobalDao globalDao;
	private UserProfileDao userProfileDao;
	private List<XCard> xCardList  = new ArrayList<XCard>();
	private AddsbookDao addsbookDao;

	@Autowired
	public void setAddsbookDao(AddsbookDao addsbookDao) {
		this.addsbookDao = addsbookDao;
	}

	
	@Autowired
	public void setGlobalDao(GlobalDao globalDao) {
		this.globalDao = globalDao;
	}
	
	@Autowired
	public void setUserProfileDao(UserProfileDao userProfileDao) {
		this.userProfileDao = userProfileDao;
	}

	//admin@server08.com
	@Override
	public String login(String jid) throws ErrorResultException, IOException, ParserConfigurationException, SAXException {
		String sessionid = globalDao.login(jid);
		if(sessionid != null)
		{
			GlobalClient client = ClientManager.getClient(jid);
			Date now = new Date();
			client.setLastActiveTime(now);
			TypeUser myProfile = userProfileDao.userProfileSelect(sessionid);
			byte[] avatarByte = myProfile.getAvatar();
			BASE64Encoder encoder = new BASE64Encoder();
			if(avatarByte.length>0)
			{
				client.setAvatarBASE64("data:image/png;base64,"+encoder.encode(avatarByte));//返回Base64编码过的字节
			}
			clientGetAddsbook(sessionid, client);
			client.getxCardList();
			client.setName(myProfile.getFamilyname());
			return sessionid;
		}
		else
		{
			return null;
		}
	}
	
	private void clientGetAddsbook(String sessionid, GlobalClient client)
	{
		client.ClearAddsBook();
		TypeAddsbook[] typeAddsbooks;
		try 
		{
			typeAddsbooks = addsbookDao.getAddsbookList(sessionid);
			for(TypeAddsbook typeAddsbook : typeAddsbooks)
			{
				String addsbookXML = addsbookDao.addsbookGet(sessionid, typeAddsbook.getId());
				
				System.out.println(addsbookXML);
				InputStream   in_nocode   =   new   ByteArrayInputStream(addsbookXML.getBytes("utf-8")); 
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		        DocumentBuilder builder = factory.newDocumentBuilder();  
				Document document = builder.parse(in_nocode);  
				Element element = document.getDocumentElement();  
				NodeList bookNodes = element.getChildNodes();
				AddsBook  addsbook = new AddsBook();
				
				addsbook.setId(element.getAttribute("ID"));
				addsbook.setName(element.getAttribute("Name"));
				for(int i=0;i<bookNodes.getLength();i++)
				{
					if(bookNodes.item(i) instanceof Element)
					{
						Element addsbookElement = (Element) bookNodes.item(i);  
						if(addsbookElement.getTagName().equals("xCard"))
						{
							XCard xCard = ParseXCard(addsbookElement);
							addsbook.addXCard(xCard);
						}
						else if(addsbookElement.getTagName().equals("Folder"))
						{
							Folder folder = ParseFolder(addsbookElement);
							addsbook.addFolder(folder);
						}
					}
				}
				client.AddAddsBook(addsbook);
				client.setxCardList(xCardList);
			}
		} catch (ErrorResultException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private XCard ParseXCard(Element ele)
	{
		XCard xCard = new XCard();
		xCard.setId(ele.getAttribute("ID"));
		xCard.setName(ele.getAttribute("Name"));
		xCard.setJid(ele.getAttribute("JID"));
		xCard.setLinkId(ele.getAttribute("LinkItemID"));
		xCard.setIndex(ele.getAttribute("Index"));
		
		xCardList.add(xCard);
		return xCard;
	}
	
	private Folder ParseFolder(Element ele)
	{
		Folder folder = new Folder();
		folder.setId(ele.getAttribute("ID"));
		folder.setName(ele.getAttribute("Name"));
		folder.setIndex(ele.getAttribute("Index"));
		folder.setParentId(ele.getAttribute("ParentID"));
		NodeList childEle = ele.getChildNodes();
		for(int i=0;i<childEle.getLength();i++)
		{
			if(childEle.item(i) instanceof Element)
			{
				Element childElement = (Element) childEle.item(i);  
				if(childElement.getTagName().equals("xCard"))
				{
					XCard childXCard = ParseXCard(childElement);
					folder.addXCard(childXCard);
				}
				else if(childElement.getTagName().equals("Folder"))
				{
					Folder childFolder = ParseFolder(childElement);
					folder.addFolder(childFolder);
				}
			}
		}
		
		return folder;
		
	}

}
