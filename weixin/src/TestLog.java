
import java.io.IOException;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.*;
import org.apache.commons.httpclient.methods.*;

public class TestLog {
	static final String LOGON_SITE = "localhost" ;
	   static final int     LOGON_PORT = 8080;
	
	
	public static void main(String[] args) throws HttpException, IOException {
		HttpClient client = new HttpClient();
	      client.getHostConfiguration().setHost(LOGON_SITE, LOGON_PORT);
	      PostMethod post = new PostMethod( "http://localhost:8080/weixin/sendPoll" );
	      NameValuePair uuid = new NameValuePair( "uuid" , "20140801093205100001" );
	      NameValuePair user = new NameValuePair( "jid" , "yyyy@fe.shenj.com" );
	      NameValuePair pass = new NameValuePair( "pass" , "yyyyyy" );
	      post.setRequestBody( new NameValuePair[]{uuid,user,pass});
	      int status = client.executeMethod(post);
	      System.out.println(post.getResponseBodyAsString());
	      post.releaseConnection();

	}
}