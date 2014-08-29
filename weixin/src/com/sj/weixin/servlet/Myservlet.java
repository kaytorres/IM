package com.sj.weixin.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.sj.kunlun.dao.ManageDao;
import com.sj.weixin.web.controller.LoginController;
import com.sj.weixin.web.controller.UserClient;



public class Myservlet extends HttpServlet{  
	  
    private static final long serialVersionUID = 1L;  
    private long MAX_EXPIRE_MINS = 10 ; 				//session最大过期时间(分)
    private long MAX_CONNECTION = 9223372036854775807L;	//session最大连接数
    private boolean CONNECTION_LIMIT = false;			//true:session最大连接数超过时才进行释放  ,false:随时释放过期session连接
    private long SCAN_INTERVAL = 5000;					//扫描间隔(毫秒)
    ManageDao manageDao ;
    public Myservlet(){  
    }  

    public void init(){
    	manageDao = LoginController.dao;
    	Execute e = new Execute();
		e.start();
    }  
   

	public class Execute extends Thread{
		@Override
		public void run() {
			while (true) {
				try {
					if(CONNECTION_LIMIT)
					{
						if(LoginController.connection.size() > MAX_CONNECTION)
						{
							//System.out.println("Exist conn count :" +LoginController.connection.size());
							Date nowTime = new Date();
							Iterator iterator = LoginController.connection.entrySet().iterator();
							while(iterator.hasNext())
							{
					            Map.Entry<String,UserClient> entry =  (Entry<String, UserClient>) iterator.next();
					            UserClient entryvalue = entry.getValue();
					            //System.out.println("Exist session : "+ entry.getKey());
					            if(entryvalue.getLastConn()!=null)
					            {
					            	long temp = nowTime.getTime() - entryvalue.getLastConn().getTime();
					            	long mins = temp /1000 /60;
					            	if(mins > MAX_EXPIRE_MINS)
					            	{
					            		iterator.remove();
					            	}
					            }
							}
						}
					}
					else
					{
						//System.out.println("Exist conn count :" +LoginController.connection.size());
						Date nowTime = new Date();
						Iterator iterator = LoginController.connection.entrySet().iterator();
						while(iterator.hasNext())
						{
				            Map.Entry<String,UserClient> entry =  (Entry<String, UserClient>) iterator.next();
				            UserClient entryvalue = entry.getValue();
				            //System.out.println("Exist session : "+ entry.getKey());
				            if(entryvalue.getLastConn()!=null)
				            {
				            	long temp = nowTime.getTime() - entryvalue.getLastConn().getTime();
				            	long mins = temp /1000 /60;
				            	if(mins > MAX_EXPIRE_MINS)
				            	{
				            		iterator.remove();
				            	}
				            }
						}
					}
					sleep(SCAN_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
    public void doGet(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)  
        throws ServletException, IOException{  
    }  
  
    public void destory(){  
          
    }  
}  
 