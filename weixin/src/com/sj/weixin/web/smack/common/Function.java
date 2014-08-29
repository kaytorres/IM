package com.sj.weixin.web.smack.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jivesoftware.smackx.muc.Occupant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sj.weixin.web.entity.TypeXHTML;

public class Function {
	
	public static String transferHTML(String str)
	{
		str = str.replaceAll("\\\\", "\\\\\\\\");
		str = str.replaceAll("&","&amp;");
		str = str.replaceAll("\"","&quot;");
		str = str.replaceAll("<","&lt;");
		str = str.replaceAll(">","&gt;");
		str = str.replaceAll("\n","<br>");
		str = str.replaceAll("\r","<br>");
		return str;
	}
	

	public static TypeXHTML TransferXHTML(String oriXML)
	{
		TypeXHTML newTypeHTML = new TypeXHTML();
		String newXML = "";
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			
			transformer = tf.newTransformer();
			InputStream in_nocode = new   ByteArrayInputStream(oriXML.getBytes("utf-8"));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
		    DocumentBuilder builder = factory.newDocumentBuilder();  
			Document document = builder.parse(in_nocode);  
			Element element = document.getDocumentElement();  
			
			if(element.getTagName().toLowerCase().equals("html"))
			{
				NodeList xmlBodys = element.getChildNodes();
				for(int i=0;i<xmlBodys.getLength();i++)
				{
					if(xmlBodys.item(i) instanceof Element)
					{
						Element xmlBodyElement = (Element) xmlBodys.item(i);
						if(xmlBodyElement.getTagName().toLowerCase().equals("body"))
						{
							NodeList txts = xmlBodyElement.getChildNodes();
							for(int j=0;j<txts.getLength();j++)
							{
								if(txts.item(j) instanceof Element)
								{
									Element xmlElement = (Element) txts.item(j);
									if(xmlElement.getTagName().toLowerCase().equals("p"))
									{
										xmlElement.getChildNodes();
										for(int k=0;k<xmlElement.getChildNodes().getLength();k++)
										{
											if(xmlElement.getChildNodes().item(k) instanceof Element)
											{
												Element pTagContent = (Element) xmlElement.getChildNodes().item(k);
												if(pTagContent.getTagName().toLowerCase().equals("a"))
												{
													Node newChild = null ;
													String imgURL = pTagContent.getAttribute("href");
													
													if(pTagContent.getChildNodes().item(0) instanceof Element)
													{
														Element aTagContent =  (Element)pTagContent.getChildNodes().item(0);
														if(aTagContent.getTagName().toLowerCase().equals("img"))
														{
															newTypeHTML.SetTypeImg();
															aTagContent.setAttribute("onclick", "chatShowImg(\""+imgURL+"\")");
															aTagContent.setAttribute("onload", "chatScrollRecal()");
															newChild = aTagContent;
														}
														
														DOMSource source = new DOMSource(aTagContent);
														StringWriter sw = new StringWriter();
														StreamResult result = new StreamResult(sw);  
														transformer.transform(source, result);
														
														String resultStr = sw.toString();
														resultStr = resultStr.substring(resultStr.indexOf("<img"));
		
														newXML = newXML + "<p>" + resultStr +"</p>";
													}
													/*
													pTagContent.setAttribute("href", "javascript:void(0)");
													pTagContent.removeChild(pTagContent.getChildNodes().item(0));
													pTagContent.appendChild(newChild);
													DOMSource source = new DOMSource(pTagContent);
													StringWriter sw = new StringWriter();
													StreamResult result = new StreamResult(sw);  
													transformer.transform(source, result);
													
													String resultStr = sw.toString();
													resultStr = resultStr.substring(resultStr.indexOf("<a"));
	
													newXML = newXML + "<p>" + resultStr +"</p>";
													*/
												}
											}
											else
											{
												newXML = newXML + "<p>" + xmlElement.getChildNodes().item(k).getTextContent() +"</p>";
											}
										}
										/*
										DOMSource source = new DOMSource(xmlElement);
										StringWriter sw = new StringWriter();
										StreamResult result = new StreamResult(sw);  
										transformer.transform(source, result);
										String resultStr = sw.toString();
										System.out.println(resultStr); 
										*/
										
									}
									
								}
							}
							break;
						}
					}
				}
			}
			
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
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
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		newTypeHTML.setXhtml(newXML);
		return newTypeHTML;
		
	}
	
	
	
	
	public static String getPureId(String id){
		String ret;
		ret = id.indexOf("/") > 0 ? id.substring(0,id.indexOf("/")) : id;
		return ret;
	}
	
	public static String getNickName(String from){
		String ret;
		ret = from.indexOf("/") > 0 ? from.substring(from.indexOf("/")+1) : null;
		return ret;
	}
	
	public static String getRoomName(String from){
		String ret;
		ret = from.indexOf("/") > 0 ? from.substring(0,from.indexOf("/")) : null;
		return ret;
	}
	
	public static String getUserName(String jid){
		String ret;
		ret = jid.indexOf("@") > 0 ? jid.substring(0,jid.indexOf("@")) : jid;
		return ret;
	}
	
	public static boolean isInModerators(Collection<Occupant> moderators,String jid){
		boolean ret = false;
		for(Occupant op:moderators){
			String tmpJid = Function.getPureId(op.getJid());
			if(jid.equals(tmpJid)){
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	public static String getModeratorNick(Collection<Occupant> moderators,String jid){
		String ret = null;
		for(Occupant op:moderators){
			String tmpJid = Function.getPureId(op.getJid());
			if(jid.equals(tmpJid)){
				ret = op.getNick();
				break;
			}
		}
		return ret;
	}
	
}
