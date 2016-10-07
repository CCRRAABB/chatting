import java.net.Socket;

import org.json.simple.JSONObject;

public class Message {

	
	private Socket msgSource;
	private JSONObject msg;
	
	public Message(JSONObject msg, Socket socket){
		this.msg= msg;
		msgSource = socket;
	}
	
	public Socket getSource()
	{
		return msgSource;
	}
	
	public JSONObject getMsg()
	{
		return msg;
	}
}
