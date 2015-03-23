import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class rbBroadcaster extends Broadcaster {

	//message set
	ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>> messageSet;
	
	public rbBroadcaster() {
		super();
		this.messageSet = new ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>>();
	}
	
	@Override
	public void deliver(Message m) {
		this.rbDeliver(m);
	}
	
	public void rbDeliver(Message m) {
		CopyOnWriteArrayList<Integer> n = null;
		if(this.messageSet.containsKey(m.sender)) {
			n = this.messageSet.get(m.sender);
			for(int i : n) {
				if(i == m.getMessageNumber()) {
					return;
				}
			}
		} else {
			n = new CopyOnWriteArrayList<Integer>();
		}
		System.out.printf("%s : %s | ReliableBroadcast\n", m.sender, m.contents);
		n.add(m.getMessageNumber());
		this.messageSet.put(m.sender, n);
		this.bebBroadcast(m);
	}
	
}
