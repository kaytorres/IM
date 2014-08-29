package com.sj.weixin.web.interceptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
	private static final int AUTH_LOGIN = 90;
	private static final int AUTH_ANONYMOUS = 99;
	private Map<String, Integer> authMap = new HashMap<String, Integer>();
	
	public AuthorizationInterceptor() {
		authMap.put("\\/(?:login(?:\\/.*)?)?", AUTH_ANONYMOUS);
		authMap.put("\\/(?:getUUid(?:\\/.*)?)?", AUTH_ANONYMOUS);
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		String path = request.getServletPath();
		if("/".equals(path)) {
			path = "/login";
		}
		HttpSession session = request.getSession();
		if(doHandlerSession(path, session)) {
			return true;
		} else {
			String root = request.getContextPath();
			response.sendRedirect(root + "/deny");
			return false;
		}
	}
	
	private boolean doHandlerSession(String path, HttpSession session) {
		Iterator<String> it = authMap.keySet().iterator();
		Integer auth = AUTH_LOGIN;
		while (it.hasNext()) {
			String reg = it.next();
			if (path.matches(reg)) {
				auth = authMap.get(reg);
				break;
			}
		}

		int role = AUTH_ANONYMOUS;
		
//		return role <= auth;
		return true;
	}
}
