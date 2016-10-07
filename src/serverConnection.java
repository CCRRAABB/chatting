import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class serverConnection {

	//private int serverNum = 0;
	private Map<String, serverInfo> serverInformation;
	private static serverConnection singleton = null;
	
	private serverInfo localhost;
	
	private serverConnection(){
		//serverNum = 0;
		serverInformation = new HashMap<String, serverInfo>();
		localhost = null;
	}
	
	public static synchronized serverConnection getInstance()
	{
		if(singleton == null)
			singleton = new serverConnection();
		return singleton;
	}

	public synchronized void addServer(String id, String address,int port,int coPort )
	{
		
		serverInfo temp = new serverInfo(id,address,port,coPort);
		serverInformation.put(id,temp );
		//serverNum++;
	}
	
	public synchronized void initialLocalhost(String id, String address, int port, int coPort)
	{
		localhost = new serverInfo(id,address,port,coPort);
	}
	
	public synchronized int getServerCoport()
	{
		return localhost.getCoport();
	}
		
	public synchronized String getLocalId()
	{
		return localhost.getId();
	}
	@SuppressWarnings("unchecked")
	public synchronized boolean sendLockId(String identity)
	{	
		Iterator<Entry<String, serverInfo>> entries = serverInformation.entrySet().iterator();  
		  System.out.println("ijlj");
		while (entries.hasNext()) {  
		  
		    @SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) entries.next();  
		   
		    @SuppressWarnings("unused")
			String key = (String)entry.getKey();  
		  
		    serverInfo value = (serverInfo)entry.getValue(); 
		    InetAddress address = value.getAddress();
		    int coport =  value.getCoport();
		  
		    JSONObject lock = new JSONObject();
		    lock.put("type","lockidentity");
		    lock.put("serverid",localhost.getId());
		    lock.put("identity", identity);
		    
		    Socket socket = null;
		    System.out.println(lock.toString());
		    //System.out.println(address+coport);
		    try
		    {	
		    	
		    	socket = new Socket(address,coport);
		    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		    	out.write(lock.toJSONString());
		    	out.newLine();
		    	out.flush();
		    	
		    	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
		    	String reply = in.readLine();
		    	
		    	JSONParser parser = new JSONParser();
		    	JSONObject replyLock = (JSONObject) parser.parse(reply);
		    	System.out.println("recieve"+replyLock.toString());
		    	// if no lockidentity
		    	
		    	//String type = (String) replyLock.get("type");
		    	//String reidentity = (String) replyLock.get("identity");
		    	String reLock = (String) replyLock.get("locked");
		    	
		    	//if(type.equals("lockidentity")&&reidentity.equals(identity))
		    	if(reLock.equals("false"))
		    		return false;
		        socket.close();
		    	
		    }
		    catch(Exception e)
		    {
		    	e.printStackTrace();
		    }
		    
		  
		}  
		//System.out.println("gg");
		return true;
	}
	
	


	public void response(Socket otherServer, JSONObject response) {
		// TODO Auto-generated method stub
		try
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(otherServer.getOutputStream()));
			out.write(response.toJSONString());
			out.newLine();
			out.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public int getServerPort() {
		return localhost.getPort(); 
	}

	public InetAddress getHost(String sid) {
		// TODO Auto-generated method stub
		return serverInformation.get(sid).getAddress();
	}

	public int getPort(String sid) {
		// TODO Auto-generated method stub
		return serverInformation.get(sid).getPort();
	}

	public synchronized void broadCastServer(JSONObject br) {
		// TODO Auto-generated method stub
		Socket socket= null;
		try {
			Iterator<Entry<String, serverInfo>> entries = serverInformation.entrySet().iterator();  
			  
			while (entries.hasNext()) {  
			  
			    @SuppressWarnings("rawtypes")
				Map.Entry entry = (Map.Entry) entries.next(); 
			    serverInfo temp = (serverInfo) entry.getValue();
			    InetAddress address = temp.getAddress();
			    int coport = temp.getCoport();
			    		
			    socket = new Socket(address,coport);
		    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		    	out.write(br.toJSONString());
		    	out.newLine();
		    	out.flush();
		    	socket.close();
			    
			} 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@SuppressWarnings("unchecked")
	public boolean lockRoom(String cid, String roomid) {
		// TODO Auto-generated method stub
		Socket socket= null;
		JSONObject br = new JSONObject();
		br.put("type", "lockroomid");
		br.put("serverid", localhost.getId());
		br.put("roomid",roomid);
		
		try {
			Iterator<Entry<String, serverInfo>> entries = serverInformation.entrySet().iterator();  
			  
			while (entries.hasNext()) {  
			  
			    @SuppressWarnings("rawtypes")
				Map.Entry entry = (Map.Entry) entries.next(); 
			    serverInfo temp = (serverInfo) entry.getValue();
			    InetAddress address = temp.getAddress();
			    int coport = temp.getCoport();
			    		
			    socket = new Socket(address,coport);
		    	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
		    	out.write(br.toJSONString());
		    	out.newLine();
		    	out.flush();
		    	
		    	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
		    	String reply = in.readLine();
		    	JSONParser parser = new JSONParser();
		    	JSONObject replyLock = (JSONObject) parser.parse(reply);
		    	String locked = (String) replyLock.get("locked");
		    	
		    	socket.close();
		    	if(locked.equals("false"))
		    		return false;	
			} 
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (ParseException e){
			 e.printStackTrace();
		 }
		
		
		
		return false;
	}

	public void redirectClient(JSONObject br,String sid) {
		// TODO Auto-generated method stub
		
		serverInfo temp = serverInformation.get(sid);
	    InetAddress address = temp.getAddress();
	    int coport = temp.getCoport();
	    		
	    Socket socket = null;
		try {
			socket = new Socket(address,coport);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
	    	out.write(br.toJSONString());
	    	out.newLine();
	    	out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		
	}

}
