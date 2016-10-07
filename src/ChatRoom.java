import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.json.simple.JSONObject;



public class ChatRoom {
		private String owner;
		private CopyOnWriteArrayList<String> members = null;
		private String identity;
		
		//@SuppressWarnings("rawtypes")
		public ChatRoom(String id,String creator)
		{
			identity = id;
			owner = creator;
			System.out.println("create"+creator);
			members = new CopyOnWriteArrayList<String>();
		}
		
		//@SuppressWarnings("unchecked")
		public boolean addMember(String newMem)
		{
			return members.add(newMem);
		}
		
		public String getOwner()
		{
			return owner;
		}
		
		public String getId()
		{
			return identity;
		}
		
		public ArrayList<String> getMembers()
		{
			ArrayList<String> temp = new ArrayList<String>();
			for(String value : members)					
		           temp.add(value);
			return temp;
		}

		@SuppressWarnings("unchecked")
		public void broadCast(String cid, String fid, String roomid) {
			// TODO Auto-generated method stub
			JSONObject re = new JSONObject();
			re.put("type", "roomchange");
			re.put("identity",cid);
			re.put("former",fid);
			re.put("roomid", roomid);
			Socket socket = null; 
			ClientState clients = ClientState.getInstance();
			
			for(String clientid: members)
			{
				socket = clients.getClientSocket(clientid);
				try 
		    	{
		        	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
					out.write(re.toJSONString());
					out.newLine();
					out.flush();
				} catch (Exception e) {
					//System.out.println("someone disconnected");
					
					 QuitOperation quit = new QuitOperation();
					 quit.quit(socket);
					}
				}
			}
		

		public void delteMember(String cid) {
			
			members.remove(cid);
			// TODO Auto-generated method stub
			
		}
		
		public void broadCastMsg(JSONObject msg,String cid) {
			// TODO Auto-generated method stub
			
			Socket socket = null; 
			ClientState clients = ClientState.getInstance();
			System.out.println(msg);
			
			for(String clientid: members)
			{
				if(!clientid.equals(cid))
				{
					socket = clients.getClientSocket(clientid);
					if(socket.isClosed())
					{
						System.out.println("alkald");
						QuitOperation quit = new QuitOperation();
			        	quit.quit(socket);
					}
					else{
					try 
			    	{
			        	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
						out.write(msg.toJSONString());
						out.newLine();
						out.flush();
					} catch (IOException e) {
						QuitOperation quit = new QuitOperation();
			        	quit.quit(socket);
					}
					}
				}
				
			}
		}
}
