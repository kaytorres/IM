package com.sj.weixin.web.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class XMLUtility {	
	/** 
	 * 创建新的Document
	 * 
	 * @return Document 新的Document
	 */
	public static Document createDocument() {
		Document doc = null;
		try {
			doc = DocumentHelper.createDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * String -> Document
	 * 
	 * @param str 初始的String
	 * @return Document 转完后的Document
	 */
	public static Document string2Document(String str) {
		Document doc = null;
		try {
			 doc = DocumentHelper.parseText(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * Document -> String
	 * 
	 * @param doc 初始的Document
	 * @return String 转完后的String
	 */
	public static String document2String(Document doc) {
		OutputFormat format = new OutputFormat();
		format.setExpandEmptyElements(true);
		format.setIndentSize(2);
		format.setNewlines(true);
//		format.setTrimText(true);
		format.setSuppressDeclaration(true);
        StringWriter stringOut = new StringWriter();
		XMLWriter writer = new XMLWriter(stringOut, format);
		try {
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringOut.toString();
	}
	
	/**
	 * 从文件中读取document
	 * 
	 * @param path 读取文件的路径
	 * @return 读取的Document
	 */
	public static Document readfromXml(String path) {
		Document doc = null;
		try {
			 SAXReader saxReader = new SAXReader();
			 doc=saxReader.read(new File(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * 把document写入文件
	 * 
	 * @param doc 初始的Document
	 * @param path 写入文件的路径
	 */
	public static void writetoXml(Document doc, String path) {
		try {
			OutputFormat format = new OutputFormat();
			format.setIndentSize(2);
			format.setNewlines(true);
			format.setTrimText(true);
			FileWriter fileOut = new FileWriter(new File(path));
			XMLWriter writer = new XMLWriter(fileOut, format);
			writer.write(doc);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 增加节点
	 * 
	 * @param element 父节点
	 * @param name 增加节点的节点名
	 * @param text 增加节点的节点内容
	 * @throws Exception
	 */
	public static void addElement(Element element, String name, String text) {
		if(text == null) {
			text = "";
		}
		if(element != null) {
			element.addElement(name).setText(text);
		}
	}
	
	/**
	 * 根据路径取得内容
	 * 
	 * @param doc 取得内容的文件
	 * @param path 需要取得内容的节点的路径
	 * @return String 取得内容
	 */
	public static String getText(Document doc, String path) {
		Node node = doc.selectSingleNode(path);
		String result = "";
		if(node != null) {
			result = node.getText();
		}
	    return result.trim();
	}
}
