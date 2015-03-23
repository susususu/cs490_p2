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
	ArrayList<Process> group;
	
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
	
	long messageCount;
	
	int HEARTBEAT_RATE = 5;
	int THREAD_POOL_CAPACITY = 100;
	int messageNumber;
	
	
	public ChatClient( String serverAddress, int portNumber) throws UnknownHostException, IOException {
		
		this.messageNumber = 0;
		this.messageCount = 0;
		
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
		this.group = new ArrayList<Process>();
		
		System.out.print("Do yo want FIFO order? [yn] ");
		String str = sc.nextLine();
		if(str.equals("y")) {
			this.broadcaster = new fifoBroadcaster();
		} else if(str.equals("n")){
			this.broadcaster = new rbBroadcaster();
		} else {
			System.exit(0);
		}
		this.broadcaster.init(this, this);
		
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
	        this.oos.reset();
	        this.oos.writeObject(m);
	        this.oos.flush();
	       
	        //read the message back
	        m = (Message)this.ois.readObject();
	        
	        return(m.getContents().equals("Success"));
	}

	@Override
	public synchronized void recieve(Message m) {
		this.messageCount++;
		this.broadcaster.deliver(m);
	}

	@Override
	public void run() {
		while(true) {
			try {
				Socket client = this.serverSocket.accept();
				this.executor.execute(new Runnable() {

					BroadcastReceiver br;
					Socket client;
					
					@Override
					public void run() {
						try {
							ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
							Message m = (Message) ois.readObject();
							this.br.recieve(m);
							this.client.close();
							Thread.yield();
						} catch (IOException | ClassNotFoundException e) {
							e.printStackTrace();
						}
					}
					
					public Runnable init(BroadcastReceiver br, Socket client) {
						this.br = br;
						this.client = client;
						return this;
					}
					
				}.init(this, client));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
					    cc.oos.reset();
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
			if(s.equals("exit") || s.equals("q")) {
				try {
					this.s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			} else if(s.equals("get")) {
				try {
					//try to get the list
				        this.oos.reset();
					this.oos.writeObject(new Message(0, "", 'g', ""));
					this.oos.flush();
					
					//print out the list
					this.group  = (ArrayList<Process>) this.ois.readObject();
					System.out.printf("%d People Online Now\n", this.group.size());
					for(Process p: this.group) {
						System.out.printf("%s ", p.ID);
					}
					System.out.println();
					this.broadcaster.members = this.group;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else if(s.startsWith("bc ")) {
				try {
					//get the newest version of user
					 this.oos.reset();
					this.oos.writeObject(new Message(0, "", 'g', ""));
					this.oos.flush();
					this.group  = (ArrayList<Process>) this.ois.readObject();
					this.broadcaster.members = this.group;
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//broadcast message
				String str = s.substring(3);
				Message m = new Message(this.messageNumber, str, 'b', this.ID);
				this.messageNumber++;
				this.broadcaster.broadcast(m);
			} else if (s.equals("test")) {
				for(int i = 0; i < 10000; i++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Message m = new Message(this.messageNumber, Integer.toString(i), 'b', this.ID);
					this.messageNumber++;
					this.broadcaster.broadcast(m);
				}
				System.out.printf("In total %d message recieved", this.messageCount);
			} else if (s.equals("count")) {
				System.out.println(this.messageCount);
			} else if (s.equals("test2")) {
				for(int i = 0; i < 3000; i++) {
					Message m = new Message(this.messageNumber, Integer.toString(i), 'b', this.ID);
					this.messageNumber++;
					this.broadcaster.broadcast(m);
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
