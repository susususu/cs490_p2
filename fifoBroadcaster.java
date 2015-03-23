import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;




public class fifoBroadcaster extends rbBroadcaster {

	ConcurrentHashMap<String, Integer> messagePlot;
	ConcurrentHashMap<String, ArrayList<Message>> pendingSet;
	ThreadPoolExecutor executor;
	
	public fifoBroadcaster() {
		super();
		this.messagePlot = new ConcurrentHashMap<String, Integer>();
		this.pendingSet = new ConcurrentHashMap<String, ArrayList<Message>>();
		this.executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(5);
	}

	@Override
	public void deliver(Message m) {
		//FIFO Deliver
		if(!this.messagePlot.containsKey(m.sender)) {
			this.messagePlot.put(m.sender, 1);
		}
		int no = this.messagePlot.get(m.sender);
		if(m.messageNumber < no) {
			this.rbDeliver(m);
			if(m.messageNumber + 1 == no) {
				no++;
				this.messagePlot.put(m.sender, no);
			}
		} else {
			ArrayList<Message> l = this.pendingSet.get(m.sender);
			if(l == null) {
				l = new ArrayList<Message>();
			}
			l.add(m);
			if(l.size() > 1) {
				l.sort(new Comparator<Message>() {
	
					@Override
					public int compare(Message o1, Message o2) {
						return o1.messageNumber - o2.messageNumber;
					}
					
				});
			}
			this.pendingSet.put(m.sender, l);
		}
		
		ArrayList<Message> l = this.pendingSet.get(m.sender);
		if(l == null) return;
		ArrayList<Message> newlist = new ArrayList<Message>();
		for(Message m1 : l) {
			if(m1.messageNumber < no) {
				this.rbDeliver(m1);
				
			} else {
				newlist.add(m1);
			}
		}
		this.pendingSet.put(m.sender, newlist);
		
	}
}
