import java.io.Serializable;

public class Message implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5262364781594387438L;
	int messageNumber;
	String contents;
	String sender;
	
	/*
	 * 'r'	: register
	 * 'h'	: heart beat
	 * 'g'	: get
	 * 'c'	: chat from an other client
	 * 'm'	: normal message
	 */
	char flag;

	public Message(int messageNumber, String contents, char flag, String sender) {
		this.messageNumber = messageNumber;
		this.contents = contents;
		this.flag = flag;
		this.sender = sender;
	}
	
	public int getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public char getFlag() {
		return flag;
	}

	public void setFlag(char flag) {
		this.flag = flag;
	}
	
	
}
