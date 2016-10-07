import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ServerCommunication extends Thread {
	public ServerCommunication(){
		
	}
	@SuppressWarnings("unchecked")
	public void run(){
		
		serverConnection server = serverConnection.getInstance();
		ClientState client = ClientState.getInstance();
		ChatRoomState chatroom = ChatRoomState.getInstance();
		//ServerSocket serSocket = null;
		
		try {
			@SuppressWarnings("resource")
			ServerSocket serSocket = new ServerSocket(server.getServerCoport());
			while(true){
			//@SuppressWarnings("resource")
			
			Socket otherServer = serSocket.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(otherServer.getInputStream(),"UTF-8"));
			
			String msg = in.readLine();
			JSONParser parser = new JSONParser();
			JSONObject serverMsg= (JSONObject) parser.parse(msg);
			
			String type = (String) serverMsg.get("type");
			String serverid = (String) serverMsg.get("serverid");
			
			System.out.println("recieve:"+ serverMsg);
			
			if(type.equals( "lockidentity"))
			{	
				String identity = (String) serverMsg.get("identity");
				
				JSONObject response = new JSONObject();
				response.put("type","newidentity");
				response.put("serverid", server.getLocalId());
				response.put("identity",identity );
				
				
				
				if(client.responseLock(identity))
					response.put("locked", "true");
				else
				{
					response.put("locked", "false");
					System.out.println("i am nnnnnn");
				}
					
				client.addLockClient(identity, serverid);
				
				server.response(otherServer, response);
			}
			
			else if(type.equals("releaseidentity"))
			{
				String identity = (String) serverMsg.get("identity");
				client.resleaseLock(identity);
			}
				
			else if(type.equals("lockroomid"))
			{	
				String roomid = (String) serverMsg.get("roomid");
				String result = chatroom.responselock(roomid,serverid);
				JSONObject response = new JSONObject();
				response.put("type","lockroomid");
				response.put("serverid", server.getLocalId());
				response.put("roomid",roomid);
				response.put("locked",result);
				server.response(otherServer, response);
			}
			else if(type.equals("releaseroomid"))
			{
				String roomid = (String) serverMsg.get("roomid");
				String approved = (String) serverMsg.get("approved");
				System.out.println(approved);
				if(approved.equals("true"))
				{
					System.out.println("releaseroomid");
					chatroom.releaselocklist(roomid);
					chatroom.addRoomlist(roomid, serverid);
				}
				else
					chatroom.releaselocklist(roomid);
					
			}
			else if(type.equals("deleteroom"))
			{
				String roomid = (String) serverMsg.get("roomid");
				chatroom.removeRoomList(roomid);
				
			}
			/*else if(type.equals("redirectClietnLock"))
			{
				String cid = (String) serverMsg.get("clientId");
				client.addLockClient(cid, serverid);
			}*/
			otherServer.close();
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
