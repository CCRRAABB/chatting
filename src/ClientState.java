import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;


public class ClientState {

	private Map<String, Client> localClients;
	private Map<String, String> lockClients;
	private static ClientState singleton = null;
	
	private ClientState()
	{
		localClients = new ConcurrentHashMap<String, Client>();
		lockClients = new HashMap<String, String>();
		
	}
	
	public static synchronized  ClientState getInstance()
	{
		if(singleton == null)
			singleton = new ClientState();
		return singleton;
	}
	
	public synchronized void addMoveclient(String id, Socket address, String serverid)
	{

		String initialRoom = ("MainHall-").concat(serverid);
		Client temp= new Client(address,id,serverid,initialRoom);
		localClients.put(id, temp);
	}
	
	
	public synchronized void addClient(String id, Socket address, String serverid)
	{
		String initialRoom = ("MainHall-").concat(serverid);
		Client temp= new Client(address,id,serverid,initialRoom);
		localClients.put(id, temp);
		
		ChatRoomState room = ChatRoomState.getInstance();
		room.joinRoom(id,initialRoom);
		room.broadCast(id, "", initialRoom);
		
		
	}
	
	public synchronized boolean checkClient(String id)
	{
		if( localClients.containsKey(id)
				|| lockClients.containsKey(id))
			return false;
		else
			return true;
	}
	
	public synchronized void addLockClient(String id, String server)
	{
		lockClients.put(id, server);
	}
	
	public synchronized boolean responseLock(String id)
	{
		System.out.println(lockClients.containsKey(id));
		System.out.println(localClients.containsKey(id));
		if (lockClients.containsKey(id))
				return false;
		else if(localClients.containsKey(id))
			 return false;
		else
			return true;
	}

	public void resleaseLock(String identity) {
		// TODO Auto-generated method stub
		lockClients.remove(identity);
	}

	public synchronized Map<String,Client> getClients() {
		// TODO Auto-generated method stub
		return localClients;
	}
	
	public synchronized void sendResponse(Socket address, JSONObject re)
	{
    	try 
    	{
        	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(address.getOutputStream(),"UTF-8"));
			out.write(re.toJSONString());
			System.out.println(re);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			QuitOperation quit = new QuitOperation();
        	quit.quit(address);
			//System.out.println(getClientId(address)+"Disconnected");
			//e.printStackTrace();
		}
    	
		
	}

	public String getRoomId(Socket address)
	{
		
		for (Client cl : localClients.values()) {
			   if(cl.getAddress() == address)
				   return cl.getRoom();
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getClientId(Socket address)
	{
		
		for (Client cl : localClients.values()) {
			   if(cl.getAddress() == address)
				   return cl.getIdentity();
		}
		// TODO Auto-generated method stub
		return null;
	}

	public Socket getClientSocket(String cid) {
		// TODO Auto-generated method stub
		return localClients.get(cid).getAddress();
	}

	public  synchronized void updateRoom(String cid, String roomid) {
		// TODO Auto-generated method stub
		Client cl = localClients.get(cid);
		cl.updateRoom(roomid);
		
	}

	public synchronized void removeClient(String cid) {	
		 for (Iterator<Client> it = localClients.values().iterator(); it.hasNext();)
	        {
	            Client val = it.next();
	            if (val.getIdentity().equals(cid))
	            {
	                it.remove( );
	            }
	        }
			//localClients.remove(cid);	
	}
	
}
