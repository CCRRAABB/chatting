import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;


@SuppressWarnings("unchecked")
public class MessageHandler extends Thread {
	

	public MessageHandler(){
		
	}
	public void run(){
		
		MessageState msgState = MessageState.getInstance();
		
		while(true){
			//System.out.println("poll="+queue.poll()); //返回第一个元素，并在队列中删除
	        Message cur = msgState.getMsg();
	        Socket address = cur.getSource();
	        JSONObject msg= cur.getMsg();
	        String type = (String) msg.get("type");
	       System.out.println("msg:"+msg);
	        
	        if(type.equals("list"))
	        	listMembers(address);
	        else if(type.equals("who"))
	        	responseWho(address);
	        else if(type.equals("createroom"))
	        {
	        	String roomid = (String) msg.get("roomid");
	        	createRoom(address,roomid);
	        	
	        }
	        else if(type.equals("join"))
	        {
	        	String roomid = (String) msg.get("roomid");
	        	joinRoom(address,roomid);
	        }
	        else if(type.equals("deleteroom"))
	        {
	        	String roomid = (String) msg.get("roomid");
	        	deleteRoom(address,roomid);
	        }
	        else if(type.equals("message"))
	        {
	        	String content = (String) msg.get("content");
	        	sendMessage(address,content);
	        }
	        else if(type.equals("quit"))
	        {
	        	QuitOperation quit = new QuitOperation();
	        	quit.quit(address);
	        }
	        	//quitClient(address);
	        
	        }
		}
	private void createRoom(Socket address, String roomid) {
		// TODO Auto-generated method stub
		ClientState client = ClientState.getInstance();
		String cid = client.getClientId(address);
		String fid = client.getRoomId(address);
		serverConnection server = serverConnection.getInstance();
		
		JSONObject re = new JSONObject();
		re.put("type", "createroom");
		re.put("roomid",roomid);
		
		JSONObject br = new JSONObject();
		br.put("type", "releaseroomid");
		br.put("serverid",server.getLocalId());
		br.put("roomid", roomid);
		
		ChatRoomState chatroom = ChatRoomState.getInstance();
		chatroom.lockRoom(roomid);
		
		
		if(isPermitted(fid,cid,roomid)&&checkName(roomid))
		{	
			
			chatroom.addLocalRoom(cid, roomid);
			chatroom.addRoomlist(roomid,server.getLocalId());
			chatroom.broadCast(cid, fid, roomid);
			client.updateRoom(cid, roomid);
			chatroom.addMember(cid,roomid);
			chatroom.removeRoomMember(cid,fid);
			re.put("approved","true");
			client.sendResponse(address, re);	
			br.put("approved", "true");
			server.broadCastServer(br);
			
		}
		else
		{
			re.put("approved","false");
			client.sendResponse(address, re);
			br.put("approved", "false");
			server.broadCastServer(br);
		}
		chatroom.releaselocklist(roomid);
		
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
	
	private boolean isPermitted(String fid,String cid, String roomid) {
		// TODO Auto-generated method stub
		ChatRoomState chatroom = ChatRoomState.getInstance();
		serverConnection server = serverConnection.getInstance();
		if(fid.equals(("MainHall-").concat(server.getLocalId())))
		{
			 if(chatroom.checkRoom(roomid))
					return false;
				else if(server.lockRoom(cid, roomid))
						return true;
				else
					return false;
		}
		else if(checkOwner(roomid,cid))
    		return false;
		else if(chatroom.checkRoom(roomid))
			return false;
		else if(server.lockRoom(cid, roomid))
				return true;
		else
			return false;
	}
/*	private void quitClient(Socket address) {
		
		ClientState client = ClientState.getInstance();
		ChatRoomState room = ChatRoomState.getInstance();
		serverConnection server = serverConnection.getInstance();
		
		String rid = client.getRoomId(address);
		String cid = client.getClientId(address);
		if(checkOwner(rid,cid))
		{
			room.ownerQuit(rid,cid);
			
			JSONObject br = new JSONObject();
			br.put("type", "deleteroom");
			br.put("serverid", server.getLocalId());
			br.put("roomid", rid);
			server.broadCastServer(br);
			
		}
		else
		{
			
			room.broadCast(cid, rid, "");
			room.removeRoomMember(cid, rid);
		}
		client.removeClient(cid);
		
		
		
		
	}*/
	private void sendMessage(Socket address, String content) {
		
		ChatRoomState room = ChatRoomState.getInstance();
		ClientState client = ClientState.getInstance();
		String cid = client.getClientId(address);
		String rid = client.getRoomId(address);
		JSONObject msg = new JSONObject();
		msg.put("type","message");
		msg.put("identity", cid);
		msg.put("content", content);
		System.out.println(msg);
		room.broadCastMsg(rid,cid,msg);
	}
	private void deleteRoom(Socket address, String roomid) {
		
		ChatRoomState room = ChatRoomState.getInstance();
		ClientState client = ClientState.getInstance();
		serverConnection server = serverConnection.getInstance();
		String cid = client.getClientId(address);
		if(checkOwner(roomid,cid))
		{
			JSONObject br = new JSONObject();
			br.put("type", "deleteroom");
			br.put("serverid", server.getLocalId());
			br.put("roomid",roomid);
			server.broadCastServer(br);
			room.deleteRoom(roomid);
			JSONObject re = new JSONObject();
			re.put("type", "deleteroom");
			re.put("roomid", roomid);
			re.put("approved", "true");
			client.sendResponse(address, re);
			
		}
		else
		{
			JSONObject re = new JSONObject();
			re.put("type", "deleteroom");
			re.put("roomid", roomid);
			re.put("approved", "false");
			client.sendResponse(address, re);
		}
		
		
	}
	private void joinRoom(Socket address, String roomid) {
		// TODO Auto-generated method stub
		ClientState client = ClientState.getInstance();
		ChatRoomState chatRoom = ChatRoomState.getInstance();
		serverConnection server = serverConnection.getInstance();
		String rid = client.getRoomId(address);
		String cid = client.getClientId(address);
		
		if(roomid.equals(rid))
		{
			JSONObject re = new JSONObject();
			re.put("type", "roomchange");
			re.put("identity", cid);
			re.put("former",rid);
			re.put("roomid", rid);
			client.sendResponse(address, re);
			return;
		}
		if(!chatRoom.exist(roomid))
		{
			JSONObject re = new JSONObject();
			re.put("type", "roomchange");
			re.put("identity", cid);
			re.put("former",rid);
			re.put("roomid", rid);
			client.sendResponse(address, re);
			return;
		}
		else if(rid.equals(("MainHall-").concat(server.getLocalId())))
		{
			 if(chatRoom.checkRoom(roomid))
			 {
				chatRoom.joinRoom(cid, roomid);
				chatRoom.deleteMember(cid, rid);
				client.updateRoom(cid,roomid);
				chatRoom.broadCast(cid, rid, roomid);
			 }
			else 
			{
				redirectClient(cid,roomid,address,rid);
			
			   	
			}
			
		}
		else if(!checkOwner(rid,cid))
		{ 
			 if(checkRoomExistence(roomid))
			 {
				chatRoom.joinRoom(cid, roomid);
				chatRoom.deleteMember(cid, rid);
				client.updateRoom(cid,roomid);
				chatRoom.broadCast(cid, rid, roomid);
			 }
		  
			else
			{
			    String sid = chatRoom.findServer(roomid);
			    InetAddress host = server.getHost(sid);
			    int port = server.getPort(sid);
			   	JSONObject re = new JSONObject();
			   	re.put("type", "route");
			   	re.put("roomid", roomid);
			   	re.put("host",host);
			   	re.put("port", port);
			   	client.sendResponse(address, re);
			}
		}
		else
		{
				JSONObject re = new JSONObject();
				re.put("type", "roomchange");
				re.put("identity", cid);
				re.put("former",rid);
				re.put("roomid", rid);
				client.sendResponse(address, re);
		}
			
				
				
				
		
	}
	private void redirectClient(String cid, String roomid,Socket address,String rid) {
		// TODO Auto-generated method stub
		 ChatRoomState chatRoom = ChatRoomState.getInstance();
		 serverConnection server = serverConnection.getInstance();
		 ClientState client = ClientState.getInstance();
		 
		 String sid = chatRoom.findServer(roomid);
		 String host = server.getHost(sid).getHostAddress();
		 int port = server.getPort(sid);
		/* 
		 JSONObject br = new JSONObject();
		 br.put("type","redirectClientLock");
		 br.put("serverid", server.getLocalId());
		 br.put("clientId", client.getClientId(address));
		 server.redirectClient(br,sid);
		 */
		 
		 JSONObject re = new JSONObject();
		 re.put("type", "route");
		 re.put("roomid", roomid);
		 re.put("host",host);
		 re.put("port", Integer.toString(port));
		 client.sendResponse(address, re); 
		 //System.out.println("here redirect to client"+re);
		 client.removeClient(cid);
		 chatRoom.removeRoomMember(cid, rid);
		 chatRoom.broadCast(cid, rid, roomid);;
		 
	}
	private boolean checkRoomExistence(String roomid) {
		
		ChatRoomState chatRoom = ChatRoomState.getInstance();
		return chatRoom.checkRoom(roomid);
	}
	private boolean checkOwner(String rid, String cid) {
		// TODO Auto-generated method stub
		ChatRoomState chatRoom = ChatRoomState.getInstance();
		return ((chatRoom.getOnwer(rid)).equals(cid));
	}
	
	private void responseWho(Socket address) {
		// TODO Auto-generated method stub
		ClientState client = ClientState.getInstance();
		ChatRoomState chRoom = ChatRoomState.getInstance();
		String roomid = client.getRoomId(address);
		String owner = chRoom.getOnwer(roomid);
		ArrayList<String> identities = chRoom.getMembers(roomid);
		
		JSONObject re = new JSONObject();
		re.put("type", "roomcontents");
		re.put("roomid",roomid);
		re.put("identities", identities);
		re.put("owner",owner);
		client.sendResponse(address, re);
	}
	
	private void listMembers(Socket address) {
		// TODO Auto-generated method stub
		ChatRoomState cRoom = ChatRoomState.getInstance();
		
		 ArrayList<String> result = cRoom.listRoom();
		JSONObject re = new JSONObject();
		re.put("type", "roomlist");
		re.put("rooms",result);
		ClientState client = ClientState.getInstance();
		client.sendResponse(address, re);
		
		
	}
		
	}


