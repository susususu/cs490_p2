import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

class ChatClient extends Process implements BroadcastReceiver, Runnable {

	Broadcaster broadcaster;
	ArrayList<String> group;
	
	//connection to the server
	Socket s;
	
	//Socket for incoming chat
	ServerSocket serverSocket;
	Socket _client;
	
	//ThreadPool
	ThreadPoolExecutor executor;
	
	//IO buffer
	ObjectOutputStream oos;
	ObjectInputStream ois;
	
	//universal scanner( for solving the closing warning )
	Scanner sc;

	//server attributes
	String serverAddress;
	int portNumber;
	
	int HEARTBEAT_RATE = 5;
	int THREAD_POOL_CAPACITY = 10;
	
	public ChatClient( String serverAddress, int portNumber) throws UnknownHostException, IOException {
		
		this.serverAddress = serverAddress;
		this.portNumber = portNumber;
		
		//connecting to server
		this.s = new Socket(serverAddress, portNumber);
		this.oos = new ObjectOutputStream(this.s.getOutputStream());
		this.ois = new ObjectInputStream(this.s.getInputStream());
		
		//Listening for incoming chat
		this.serverSocket = new ServerSocket(0);
		this.executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(THREAD_POOL_CAPACITY);
		
		//setting up own attributes
		this.IP = s.getLocalAddress().toString().substring(1);
		this.port = serverSocket.getLocalPort();
		this.sc = new Scanner(System.in);
		
		//listening for incoming chat
		this.listen();
	}
	
	private void listen() {
		this.executor.execute(this);
	}
	
	private boolean register() throws IOException, ClassNotFoundException {
	        //get the name
	        System.out.print("Please Enter Your Name: ");
	        this.ID = sc.nextLine();
	        
	        //send registering message
	        String str = this.ID + "," + this.port + "," + this.IP;
	        Message m = new Message(0, str, 'r', this.ID);
	        this.oos.writeObject(m);
	        this.oos.flush();
	       
	        //read the message back
	        m = (Message)this.ois.readObject();
	        
	        return(m.getContents().equals("Success"));
	}

	@Override
	public void recieve(Message m) {
		System.out.printf("%s : %s\n", m.sender, m.contents);
	}

	@Override
	public void run() {
		
	}
	
	public void sendHeartbeat() {
		this.executor.execute(new Runnable() {
			
			ChatClient cc;
			
			@Override
			public void run() {
				try {
					while(true) {
						String str = cc.ID + ',' + cc.port + ',' + cc.IP;
						Message m = new Message(0, str, 'h', cc.ID);
						cc.oos.writeObject(m);
						cc.oos.flush();
						
						Thread.sleep(HEARTBEAT_RATE * 1000);
					}
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
			public Runnable init(ChatClient cc) {
				this.cc = cc;
				return this;
			}
			
		}.init(this));
	}
	
	@SuppressWarnings("unchecked")
	public void prompt() {
		while(true) {
			System.out.print("> ");
			String s = this.sc.nextLine();
			if(s.equals("exit")) {
				try {
					this.s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			} else if(s.equals("get")) {
				try {
					//try to get the list
					this.oos.writeObject(new Message(0, "", 'g', ""));
					this.oos.flush();
					
					//print out the list
					this.group  = (ArrayList<String>) this.ois.readObject();
					System.out.println(this.group.toString());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		                
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		ChatClient cc = null;
		
		if(args.length == 2){
			cc = new ChatClient( args[0], Integer.parseInt(args[1]) );
		} else if(args.length == 1) {
			cc = new ChatClient ( "localhost", Integer.parseInt ( args[0] ) );
		}

		while(true) {
			if(cc.register()) break;
		}
		
		cc.sendHeartbeat();
		cc.prompt();
	}
	
}