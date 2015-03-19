import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class Broadcaster implements ReliableBroadcast {
	
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
	public void rbBroadcast(Message m) {
		
	}
	
	public void bebBroadcast(Message m) {
		for(Process p : members) {
			Socket s;
			try {
				s = new Socket(p.IP, p.port);
				ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
				oos.writeObject(m);
				oos.flush();
				s.close();
			} catch (IOException e) {
				System.out.printf("%s is not online\n", p.ID);
			}
		}
	}
	
}
