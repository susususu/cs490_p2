import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class Broadcaster implements Broadcast {
	
	ArrayList < Process > members;
	Process currentProcess;
	BroadcastReceiver br;

	@Override
	public void init(Process currentProcess, BroadcastReceiver br) {
		this.members = new ArrayList < Process > ();
		this.currentProcess = currentProcess;
		this.br = br;
		this.members.add(currentProcess);
	}

	@Override
	public void addMember(Process member) {
		this.members.add(member);
	}

	@Override
	public void removeMember(Process member) {
		this.members.remove(member);
	}
	
	@Override
	public void bebBroadcast(Message m) {
		for(Process p : members) {
			try (Socket s = new Socket(p.IP, p.port)) {
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject(m);
				oos.flush();
				s.close();
			} catch (IOException e) {
				System.out.printf("%s is not online\n", p.ID);
			}
		}
	}

	@Override
	public void broadcast(Message m) {
		//no matter what the message is create with the count sequence number
		//so just bebBroadcast
		this.bebBroadcast(m);
	}

	@Override
	public void deliver(Message m) {
		//dummy method
	}
	
}
