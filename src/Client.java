import java.net.Socket;


//sync
@SuppressWarnings("unused")
public class Client {
	
	private String identity;
	private Socket address;
	private String serverid;
	private String room;
	
	public Client(Socket clientSocket, String identity,String server,String room){
		this.identity = identity;
		address = clientSocket;
		serverid= server;
		this.room = room;
	}
	
	public String getRoom(){
		return room;
	}
	
	public Socket getAddress(){
		return address;
	}
	
	public String getIdentity(){
		return identity;
	}
	
	public void updateRoom(String roomid)
	{
		room = roomid;
	}
	
	
}
