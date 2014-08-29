package com.sj.weixin.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Test {

	@RequestMapping(value = { "/test","/test/"})
	public String chat(ModelMap model) {
		
		return "/test";
	}
		
}
