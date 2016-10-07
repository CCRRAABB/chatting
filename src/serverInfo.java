import java.net.InetAddress;
import java.net.UnknownHostException;


public class serverInfo {
	
	private int port;
	private InetAddress address;
	private int coPort;
	private String id;

	public serverInfo(String id, String address,int port,int coPort){
		this.port = port;
		try {
			this.address = InetAddress.getByName(address);
			if(this.address.getHostAddress().equals(InetAddress.getByName("localhost").getHostAddress()))
				this.address = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(this.address);
		this.coPort = coPort;
		this.id = id;		
	}
	
	public int getPort()
	{
		return port;
	}
	
	public InetAddress getAddress()
	{
		return address;
	}
	
	public String getId()
	{
		return id;
	}
	
	public int getCoport()
	{
		return coPort;
	}
}




