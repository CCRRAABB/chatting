
import org.json.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
//import java.util.logging.Handler;




import java.util.Map.Entry;

import org.kohsuke.args4j.*;


@SuppressWarnings("unused")
public class Server {
	
	
	public static int clientNum = 0;
	
	private String serverid = null;
	private String server_conf = null;
	
	
	private void paraserArguments(String[] args)
	{
		
		CmdLineArgs argsBean = new CmdLineArgs();
		
		CmdLineParser parser = new CmdLineParser(argsBean);
		
		try
		{
			parser.parseArgument(args);
			
			serverid = argsBean.getServerid();
			server_conf = argsBean.getServer_conf();
			
		}
		catch(CmdLineException e)
		{
			parser.printUsage(System.err);
			
		}
		
	}
	
	
	private void initialServerInfo(){
		
		//the configure text name
		//initial server info
		File file = new File(server_conf);
		BufferedReader reader = null;
		String tempStr = null;
		String[] temp;
		serverConnection ServerConnection = serverConnection.getInstance();
		ChatRoomState room = ChatRoomState.getInstance();
		
		try
		{
			reader = new BufferedReader(new FileReader(file));
			
			while((tempStr = reader.readLine()) != null )
			{
				temp = tempStr.split("\t");
				
				if(temp[0].equals(serverid))
				{
					ServerConnection.initialLocalhost(temp[0], temp[1], Integer.parseInt(temp[2]), Integer.parseInt(temp[3]));
					room.addLocalRoom("",("MainHall-").concat(serverid));
				}
				else
					ServerConnection.addServer(temp[0], temp[1],  Integer.parseInt(temp[2]),  Integer.parseInt(temp[3]));
				room.addRoomlist(("MainHall-").concat(temp[0]), temp[0]);
			}
			
			reader.close();
		}
		catch( Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Server s1 = new Server();
		
		s1.paraserArguments(args);
		s1.initialServerInfo();
		s1.initalPortBinding();
		
			
	}

	
	
	private void initalPortBinding() {
		
		try{
			
			serverConnection serverState = serverConnection.getInstance();
			ServerSocket server = null;
		
			server = new ServerSocket(serverState.getServerPort());
			

			ConnectClients connect = new ConnectClients(server);
			connect.start();
			
			ServerCommunication serverCom = new ServerCommunication();
			serverCom.start();
			
			MessageHandler messageHandler = new MessageHandler();
			messageHandler.start();
			
		    Socket curClient = null;
		    boolean isQuit = false;
		    ClientState clients = null;
		    
		    
			
			while(!isQuit)
			{
				
				clients = ClientState.getInstance();
				Map<String,Client> cur  = clients.getClients();
				MessageState msgState = MessageState.getInstance(); 
					
				for(Client temp: cur.values()){
					
					if(temp.getAddress().isClosed())
					{
						 QuitOperation quit = new QuitOperation();
						 quit.quit(temp.getAddress());
					}
					else
					{
						BufferedReader in = new BufferedReader(new InputStreamReader((temp.getAddress()).getInputStream(),"UTF-8"));
						if(in.ready())
						{
							String msg = in.readLine();
							System.out.println("add message;"+msg);
							JSONParser parser = new JSONParser();
							JSONObject clientMsg= (JSONObject) parser.parse(msg);
							msgState.addMsg(clientMsg, temp.getAddress());
							
						}
					}
					
				}
				}
			server.close();
			
		}catch(Exception e)
		{
				e.printStackTrace();
		}
		
	}
	

}
