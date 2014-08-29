package com.sj.weixin.web.service;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import com.sj.kunlun.exception.ErrorResultException;

public interface LoginService {
	public String login(String jid) throws ErrorResultException, ParserConfigurationException, SAXException, IOException, DocumentException;
}
