import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class MultithreadedChatServer implements Runnable {
	
	//pool size
	int THREAD_POOL_CAPACITY = 50;
	
	//main server attributes
	ServerSocket serverSocket;
	static ArrayList<Process> group;
	static ConcurrentHashMap<String, Long> heart_beat;
	ThreadPoolExecutor executor;
	int port;
	
	//child server attributes
	Socket _client;
	String name;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	public MultithreadedChatServer(int port) {
		this.executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(THREAD_POOL_CAPACITY);
		this.port = port;
		
		//create the server socket
		try {
			this.serverSocket = new ServerSocket(port);
		} catch ( Exception e ) {
		}
		
		group = new ArrayList<Process>();
		heart_beat = new ConcurrentHashMap<String, Long>();
	}

	public MultithreadedChatServer(Socket client) {
		this._client = client;
		
		try {
			this.oos = new ObjectOutputStream(this._client.getOutputStream());
			this.ois = new ObjectInputStream(this._client.getInputStream());
		} catch ( Exception e ) {
		}
	}
	
	private synchronized static boolean removeFromGroup(Process s) {
		return MultithreadedChatServer.group.remove(s);
	}

	public synchronized boolean modifyGroup(String s) throws IOException {
			String[] tok = s.split(",");
			System.out.println(s);
			for(Process m : MultithreadedChatServer.group) {
				if(m.ID.equals(tok[0])) {
					String str = "Failed";
				        this.oos.reset();
					this.oos.writeObject(new Message(0, str, 'm', ""));
					this.oos.flush();
					return false;
				}
			}
			
			Process p = new Process();
			p.ID = tok[0];
			p.port = Integer.parseInt(tok[1]);
			p.IP = tok[2];
			
			MultithreadedChatServer.group.add(p);
		        this.oos.reset();
			this.oos.writeObject(new Message(0, "Success", 'm', ""));
			this.oos.flush();
			return true;
	}
	
	public void checkingHeartbeat() {
		
		this.executor.execute(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					System.out.println("Checking Heartbeat");
					
					ArrayList<Process> g = new ArrayList<Process>();
					
					Long time = System.currentTimeMillis();
					for(Process s : MultithreadedChatServer.group) {
						Long t = MultithreadedChatServer.heart_beat.get(s.ID);
						if(time - t > 10000) {
							g.add(s);
						}
					}
					for(Process s : g) {
						MultithreadedChatServer.heart_beat.remove(s.ID);
						if( !removeFromGroup(s)) {
							System.out.println("removing failed");
						}
						System.out.printf("%s removed\n", s);
					}
				}
			}
			
		});
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				Message m = (Message) this.ois.readObject();
				System.out.println(m.flag + " " + m.contents);
				if(m.flag == 'r') {
					this.modifyGroup(m.contents);
					this.name = m.sender;
					MultithreadedChatServer.heart_beat.put(m.sender, System.currentTimeMillis());
				} else if(m.flag == 'g') {
					System.out.println(MultithreadedChatServer.group.toString());
				        this.oos.reset();
					this.oos.writeObject(MultithreadedChatServer.group);
					this.oos.flush();
				} else if(m.flag == 'h') {
					MultithreadedChatServer.heart_beat.put(m.sender, System.currentTimeMillis());
				}
			}
		} catch (Exception e) {
			System.out.printf("Connection to %s lost\n", this.name);
			Thread.yield();
		}
	}
	
	public void startServer() {
		System.out.println("Start!");
		this.checkingHeartbeat();
		while (true) {
			try {
				Socket _client = this.serverSocket.accept();
				MultithreadedChatServer ms = new MultithreadedChatServer(_client);
				this.executor.execute(ms);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("need port number");
			System.exit(1);
		}
		MultithreadedChatServer ms = new MultithreadedChatServer(Integer.parseInt(args[0]));
		ms.startServer();
	}

}
