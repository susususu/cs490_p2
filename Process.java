import java.io.Serializable;


public class Process implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3994399201017810065L;
	String IP;
	int port;
	String ID;

	public String getIP() {
		return IP;
	}

	public int getPort() {
		return port;
	}

	public String getID() {
		return ID;
	}
	
}
