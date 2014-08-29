import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.SimplePayload;



public class Pub {  
	private static ConnectionConfiguration conf = new ConnectionConfiguration("192.168.1.23", 5222,"fe.shenj.com");
	private static XMPPConnection conn = new XMPPConnection(conf);
	
    //private static XMPPConnection connection = new XMPPConnection("wang-think");  
    private static String USRE_NAME = "yyyy";  
    private static String PASSWORD = "yyyyyy";  
      
 //   static{  
 //       try {  
  //      	conn.connect();  
   //     	conn.login(USRE_NAME,PASSWORD);  
  //      } catch (Exception e) {  
  //          e.printStackTrace();  
  //      }  
 //   }  
  
    public static void main(String[] args)throws Exception{  
  
        try{  
        	conn.connect();  
        	
        	conn.addPacketWriterListener(new PacketListener() {
				
				@Override
				public void processPacket(Packet packet) {
					// TODO Auto-generated method stub
					System.out.println("send:" + packet.toXML());
				}
			}, null);
			conn.addPacketListener(new PacketListener() {
				@Override
				public void processPacket(Packet packet) {
					// TODO Auto-generated method stub
					System.out.println("reci:" + packet.toXML());
					
				}
			}, null);
        	conn.login(USRE_NAME,PASSWORD);  
        	
            PubSubManager manager = new PubSubManager(conn);  
            String nodeId = "zyf_test2";  
              
            LeafNode myNode = null;  
            try {  
                myNode = manager.getNode(nodeId);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
            if(myNode == null){  
                myNode = manager.createNode(nodeId);  
            }  
              
            String msg = "fsadfasdfsadfasdfd---";  
              
            SimplePayload payload = new SimplePayload("message","pubsub:test:message", "<message xmlns='pubsub:test:message'><body>"+msg+"</body></message>");  
            PayloadItem<SimplePayload> item = new PayloadItem<SimplePayload>(payload);  
  
            myNode.publish(item);  
            System.out.println("-----publish-----------");  
        }  
        catch(Exception E)  
        {E.printStackTrace();}  
          
    }  
  
}  