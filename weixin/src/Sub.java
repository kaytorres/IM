import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

public class Sub {  
	private static ConnectionConfiguration conf = new ConnectionConfiguration("192.168.1.23", 5222,"fe.shenj.com");
	private static XMPPConnection conn = new XMPPConnection(conf);
	
    //private static XMPPConnection connection = new XMPPConnection("wang-think");  
    private static String USRE_NAME = "yar5";  
    private static String PASSWORD = "123456";  
      
 //   static {  
 //       try {  
 //       	conn.connect();  
  //      	conn.login(USRE_NAME, PASSWORD);  
 //       } catch (Exception e) {  
 //           e.printStackTrace();  
  //      }  
  //  }  
  
    public static void main(String[] args) throws Exception {  
    	conn.connect();  
    	conn.login(USRE_NAME, PASSWORD);  
        String nodeId = "http://neekle.com/xmpp/protocol/media/yyyy@fe.shenj.com";  
        PubSubManager manager = new PubSubManager(conn);  
        Node eventNode = manager.getNode(nodeId);  
        eventNode.addItemEventListener(new ItemEventListener<PayloadItem>() {  
            public void handlePublishedItems(ItemPublishEvent evt) {  
                for (Object obj : evt.getItems()) {  
                    PayloadItem item = (PayloadItem) obj;  
                    System.out.println("--:Payload=" + item.getPayload().toString());  
                }  
            }  
        });  
        eventNode.subscribe(conn.getUser());  
        while(true);  
    }  
}  