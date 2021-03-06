import java.io.*;

/*
Change log:
11 / 10 : Added a new message type.
*/

/**
My understanding of the Serializable interface is that
classes that implement it are labeled serializable;
objects of this class have information that will be serialized into a stream of bytes.
These streams can be sent and deserialized by others so that the object is recreated
on their computers.
**/
public class ChatMessage implements Serializable {
	
	/*
	I think this is some sort of key.
	Almost like an encryption.
	*/
	protected static final long serialVersionUID = 1112122200L;
	
	static final int MESSAGE = 0; // The message is a plain old message.
	static final int LOGOUT = 1; // The message is a request to log out.
	static final int ONLINE = 2; // The message is a request to list users online.
	private int type; // The type are the above variables using numbers.
	private String message; // The message itself.
	
	/**
	Make an object.
	It will be serialized and deserialized.
	**/
	public ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int i) {
		type = i;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
}