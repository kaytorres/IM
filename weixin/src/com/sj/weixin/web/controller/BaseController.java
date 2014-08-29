package com.sj.weixin.web.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

public class BaseController {

	protected void outputJsonData(HttpServletResponse response, String json) {
		OutputStream os = null;
		try {
			os = response.getOutputStream();
			os.write(json.getBytes("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
