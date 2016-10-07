import org.kohsuke.args4j.Option;

public class CmdLineArgs {
	
	@Option(required = true, name = "-n")
	private String serverid;
	
	@Option(required = true, name = "-l")
	private String server_conf;
	
	private int port = 4444;
	
	public String getServerid()
	{
		return serverid;
	}
	
	public String getServer_conf()
	{
		return server_conf;
	}
	
	public int getPort()
	{
		return port;
	}
	
}
