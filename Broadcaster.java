import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;


public class Broadcaster implements ReliableBroadcast {
	
	LinkedList < Process > members;
	Process currentProcess;
	BroadcastReceiver br;

	@Override
	public void init(Process currentProcess, BroadcastReceiver br) {
		this.members = new LinkedList < Process > ();
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
	
	public void bebBroadcast(Message m) throws UnknownHostException, IOException {
		for(Process p : members) {
			Socket s = new Socket(p.IP, p.port);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			bw.write(m.getContents());
			bw.flush();
			s.close();
		}
	}

}
