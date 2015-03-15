
public interface ReliableBroadcast {
	public void init ( Process currentProcess, BroadcastReceiver br );
	public void addMember ( Process member );
	public void removeMember ( Process member );
	public void rbBroadcast ( Message m );
}
