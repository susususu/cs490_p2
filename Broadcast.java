
public interface Broadcast {
	public void init ( Process currentProcess, BroadcastReceiver br );
	public void addMember ( Process member );
	public void removeMember ( Process member );
	public void bebBroadcast ( Message m );
	public void broadcast ( Message m );
	public void deliver ( Message m );
}
