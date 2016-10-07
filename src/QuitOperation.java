import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.json.simple.JSONObject;


public class QuitOperation {
	@SuppressWarnings("unchecked")
	public void quit(Socket address)
	{
			
			ClientState client = ClientState.getInstance();
			ChatRoomState room = ChatRoomState.getInstance();
			serverConnection server = serverConnection.getInstance();
			
			String rid = client.getRoomId(address);
			String cid = client.getClientId(address);
			ChatRoom temp = room.getRoom(rid);
			
			client.removeClient(cid);
			temp.delteMember(cid);
			
			JSONObject re = new JSONObject();
			re.put("type", "roomchange");
			re.put("identity", cid);
			re.put("former", rid);
			re.put("roomid", "");
			
			try {
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(address.getOutputStream(),"UTF-8"));
				out.write(re.toJSONString());
				out.newLine();
				out.flush();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					address.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			System.out.println(re);
			
			if(room.getOnwer(rid).equals(cid))
			{  
				room.broadCast(cid, rid, "");
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
				
			}
			
	}
}
