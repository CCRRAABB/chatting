

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject; 
import org.json.simple.parser.JSONParser;

//import chattingRoom.Server;

public class ConnectClients extends Thread{

	private ServerSocket listeningSocket;
	
	public ConnectClients(ServerSocket server)
	{
		listeningSocket = server;
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		
		Socket clientSocket;
		
	   try{
		   while(true)
		   {
			   clientSocket =listeningSocket.accept();
		    
			   BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
			   String clientMsg = in.readLine();
			   
			
			   JSONParser parser = new JSONParser();
			   JSONObject clientConnection = (JSONObject) parser.parse(clientMsg);
			
			   String clientIdentity = (String) clientConnection.get("identity");
			   String type = (String) clientConnection.get("type");
			   System.out.println("Message from client " + ": " + type+" "+clientIdentity);
			
			   
			   ClientState clientstate = ClientState.getInstance();
			   serverConnection server = serverConnection.getInstance();
			   
			   
			  
			   if(type.equals("newidentity"))
			   {  
				 if(!checkName(clientIdentity))
				 {
					   sendResponse("false",clientSocket);
				 }
				   else {
					if (clientstate.checkClient(clientIdentity))
				   {	
					   clientstate.addLockClient(clientIdentity, server.getLocalId());
					   
					   if(server.sendLockId(clientIdentity))
					   {	
						   System.out.println(clientIdentity+" add into server");
						   clientstate.addClient(clientIdentity, clientSocket, server.getLocalId());
						   sendResponse("true",clientSocket);
					   }
					   else
					   {
						   sendResponse("false",clientSocket);
						   System.out.println("refuse new identity"+clientIdentity);
					   }
					   clientstate.resleaseLock(clientIdentity);   
					   
				   }
				   else
					   sendResponse("false",clientSocket);
				   JSONObject br = new JSONObject();
				   br.put("type", "releaseidentity");
				   br.put("serverid",server.getLocalId());
				   br.put("identity", clientIdentity);
				   server.broadCastServer(br);  
			   }}
			   else if(type.equals("movejoin"))
			   {
				   if (clientstate.checkClient(clientIdentity))
				   {
					   clientstate.addLockClient(clientIdentity, server.getLocalId());
					   if(server.sendLockId(clientIdentity))
					   {
						   String fid = (String) clientConnection.get("former");
						   String roomid = (String) clientConnection.get("roomid");
						   clientstate.addMoveclient(clientIdentity, clientSocket, server.getLocalId());
						   
						   JSONObject re = new JSONObject();
						   re.put("type","serverchange");
						   re.put("approved","true");
						   re.put("serverid",server.getLocalId());
						   clientstate.sendResponse(clientSocket,re);
						   
						   ChatRoomState room = ChatRoomState.getInstance();
						   if(!room.checkRoom(roomid))
							   roomid =  "MainHall-".concat(server.getLocalId());
						   else
							   clientstate.updateRoom(clientIdentity, roomid);
						   
						   room.addMember(clientIdentity, roomid);
						   
						   JSONObject msg = new JSONObject();
						   msg.put("type","roomchange");
						   msg.put("identity", clientIdentity);
						   msg.put("former",fid);
						   msg.put("roomid", roomid);
						   clientstate.sendResponse(clientSocket, msg);
						   room.broadCastMsg(roomid, clientIdentity, msg);
					   }
					   else
					   {
						   JSONObject re = new JSONObject();
						   re.put("type","serverchange");
						   re.put("approved","false");
						   re.put("serverid",server.getLocalId());
						   clientstate.sendResponse(clientSocket,re);
					   }
					   clientstate.resleaseLock(clientIdentity);  
				   }
				   else
				   {
					   JSONObject re = new JSONObject();
					   re.put("type","serverchange");
					   re.put("approved","false");
					   re.put("serverid",server.getLocalId());
					   clientstate.sendResponse(clientSocket,re);
				   }
			   }
			  
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean checkName(String cid) {
		// TODO Auto-generated method stub
		int len = cid.length();
		
		if(!(len>=3 && len<=16))
			return false;
		if(!(cid.charAt(0)>='A'&&cid.charAt(0)<='z'))
			return false;
		int count = 1;
		for(int i = 1;i < len;i++)
			if(cid.charAt(i)>='A'&&cid.charAt(i)<='z')
				if(cid.charAt(i)>='0'&&cid.charAt(i)<='9')
					count ++;
		if(count == len)
			return true;
		else
			return true;
	}

	@SuppressWarnings("unchecked")
	private void sendResponse(String result,Socket client)
	{
		JSONObject response = new JSONObject();
		response.put("type","newidentity");
		response.put("approved",result);
		
		try{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(),"UTF-8"));
		out.write(response.toJSONString());
		System.out.println(response.toJSONString());
		out.newLine();
		out.flush();
		}
		catch(Exception e)
		{
			QuitOperation quit = new QuitOperation();
        	quit.quit(client);
			
		}
	
	}	

}
