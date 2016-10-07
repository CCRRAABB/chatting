import java.util.ArrayList;
//import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;


public class ChatRoomState {
	
	private Map<String, ChatRoom> localChatroom = null;
	private static ChatRoomState singleton = null;
	private Map<String,String> roomList = null;
	private ArrayList<String> lockedroom = null;
	
	private ChatRoomState()
	{
		localChatroom = new ConcurrentHashMap<String, ChatRoom>();
		roomList = new ConcurrentHashMap<String, String>();
		lockedroom = new ArrayList<String>();
	}
	
	public static synchronized ChatRoomState getInstance()
	{
		if(singleton == null)
			singleton = new ChatRoomState();
		return singleton;
	}
	
	public synchronized void addLocalRoom(String owner,String id)
	{
		ChatRoom newRoom = new ChatRoom(id,owner);
		localChatroom.put(id, newRoom);
	}
	
	public synchronized void joinRoom(String id,String roomid)
	{
		ChatRoom room = localChatroom.get(roomid);
		room.addMember(id);
		
	}
	
	public synchronized void addRoomlist(String id,String serverid)
	{
		roomList.put(id,serverid);
	}
	
	//@SuppressWarnings("null")
	public synchronized ArrayList<String> listRoom()
	{
		//String[] temp = null;
		ArrayList<String>  temp = new ArrayList<String> (); 
		Iterator<Entry<String, String>> entries = roomList.entrySet().iterator();  
		 //int num= 0; 
		
		while (entries.hasNext()) {  
		  
		    @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) entries.next();
		    String id = (String) entry.getKey();
		    temp.add(id);
		}
		return temp;
		
	}

	public synchronized void broadCast(String cid, String fid, String roomid)
	{
		
		ChatRoom room = localChatroom.get(roomid);
		ChatRoom preRoom = localChatroom.get(fid);
		if(room != null)
			room.broadCast(cid,fid,roomid);
		if(preRoom!=null)
			preRoom.broadCast(cid,fid,roomid);
	}
	
	public synchronized void broadCastMsg(String rid,String cid, JSONObject msg)
	{
		ChatRoom room = localChatroom.get(rid);
		room.broadCastMsg(msg,cid);
	}
	
	public String getOnwer(String roomid) 
	{
		// TODO Auto-generated method stub
		ChatRoom temp = localChatroom.get(roomid);
		String t = temp.getOwner();
		return t;
	}

	public ArrayList<String> getMembers(String roomid) 
	{
		// TODO Auto-generated method stub
		ChatRoom temp = localChatroom.get(roomid);
		return temp.getMembers();
	}

	public synchronized void deleteMember(String cid, String rid) 
	{
		ChatRoom room = localChatroom.get(rid);
		room.delteMember(cid);
		// TODO Auto-generated method stub
		
	}

	public boolean checkRoom(String roomid) {
		return localChatroom.containsKey(roomid);
	}

	public String findServer(String roomid) {
		
		
		// TODO Auto-generated method stub
		return roomList.get(roomid);
	}

	public synchronized void deleteRoom(String roomid) {
		// TODO Auto-generated method stub
		ChatRoom room = localChatroom.get(roomid);
		ArrayList<String> members = room.getMembers();
		serverConnection server = serverConnection.getInstance();
		String serverid = server.getLocalId();
		ChatRoom mainhall = localChatroom.get(("MainHall-".concat(serverid)));
		ClientState client = ClientState.getInstance();
		for(String member:members)
		{
			//room.broadCast(cid, fid, roomid);
			broadCast(member,roomid, (("MainHall-").concat(serverid)));
			mainhall.addMember(member);
			client.updateRoom(member,( ("MainHall-").concat(serverid)));
		}
		
		
		localChatroom.remove(roomid);
		roomList.remove(roomid);
		
	}

	public void ownerQuit(String roomid,String cid)
	{
		ChatRoom room = localChatroom.get(roomid);
		ArrayList<String> members = room.getMembers();
		serverConnection server = serverConnection.getInstance();
		String serverid = server.getLocalId();
		ChatRoom mainhall = localChatroom.get(("MainHall-".concat(serverid)));
		ClientState client = ClientState.getInstance();
		for(String member:members)
		{
				client.updateRoom(member,( ("MainHall-").concat(serverid)));
				broadCast(member, roomid, (("MainHall-").concat(serverid)));
				mainhall.addMember(member);
		}
		
		localChatroom.remove(roomid);
		roomList.remove(roomid);	
	}
	
	
	public  synchronized void removeRoomList(String roomid) {
		// TODO Auto-generated method stub
		roomList.remove(roomid);
	}

	public synchronized String responselock(String roomid,String serverid) {
		if(roomList.containsKey(roomid)|| lockedroom.contains(roomid))
		{	lockRoom(roomid);
			return "false";
		}
		else
		{
			lockRoom(roomid);
			return "true";
		}
			// TODO Auto-generated method stub
		
	}
	
	public synchronized void lockRoom(String roomid)
	{
		lockedroom.add(roomid);
	}
	
	public synchronized void releaselocklist(String roomid)
	{
		lockedroom.remove(roomid);
	}

	public synchronized void removeRoomMember(String cid, String roomid) {
		// TODO Auto-generated method stub
		ChatRoom temp = localChatroom.get(roomid);
		temp.delteMember(cid);
	}
	
	public synchronized ChatRoom getRoom(String roomid)
	{
		return localChatroom.get(roomid);
	}

	public synchronized void addMember(String cid, String roomid) {
		// TODO Auto-generated method stub
		ChatRoom temp = localChatroom.get(roomid);
		temp.addMember(cid);
	}

	public boolean exist(String roomid) {
		
		return roomList.containsKey(roomid);
	}
}
