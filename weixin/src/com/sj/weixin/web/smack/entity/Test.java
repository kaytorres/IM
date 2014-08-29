package com.sj.weixin.web.smack.entity;

import org.jivesoftware.smack.util.StringUtils;

public class Test {
	public static void main(String[] args) {
		String str = "2014052210575610000320140522105953@muc.fe.shenj.com/张三丰";
		System.out.println(StringUtils.parseBareAddress(str).toLowerCase());
	}
}
