import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONObject;


public class MessageState {

	private  BlockingQueue<Message> msgQueue = null; 
	private static MessageState singleton = null;
	
	private MessageState()
	{
		msgQueue = new LinkedBlockingQueue<Message>();
	}
	
	public static synchronized MessageState getInstance()
	{
		if(singleton == null)
			singleton = new MessageState();
		return singleton;
	}
	
	public synchronized void addMsg(JSONObject msg, Socket address)
	{
		Message newMsg = new Message(msg,address);
		msgQueue.offer(newMsg);
	}

	public Message getMsg() {
		// TODO Auto-generated method stub
		try {
			return msgQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
