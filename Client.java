import java.net.*; // For networking; TCP and sockets
import java.io.*; // For streams.
import java.util.*; // For Scanner.

/*
Change log:
10 / 26 : Added a prompt for user for chat room alias under Inner class main().
10 / 28 : Some exception messages commented out; added new system messages.
11 / 10 : Added a message that lists those online.
11 / 15 : Username length set to 14.  Usernames can no longer be whitespace.
	: Message length set to 280.  Messages can no longer be whitespace.
11 / 17 : Client interface linkage started.
11 / 19 : Interface link success.
*/

/**
This class runs on a client's computer.
**/
public class Client {
	
	private String username; // User's alias.
	private String server; // Server IP address.
	private int port; // Server port.
	private ObjectInputStream streamInput; // For inputs.
	private ObjectOutputStream streamOutput; // For outputs.
	private Socket socket; // Connection point of the client.
	private ClientInterface ci; // Interface object; link Client with this.

	/*
	Per the standard of a certain blue chat networking site.
	Oh, wait, they're all blue.  HA.
	*/
	private static final int MESSAGE_LIMIT = 280;
	private static final int USERNAME_LIMIT = 14;
	
	/**
	Sets some variables.
	For command console use.
	**/
	public Client(String server, int port, String username) {
		this(server, port, username, null);
	}
	
	/**
	For GUI use.
	**/
	public Client(String server, int port, String username, ClientInterface ci) {
		this.server = server;
		this.port = port;
		this.username = username;
		this.ci = ci;
	}
	
	/**
	Displays a statement.
	**/
	private void display(String message) {
		if (ci != null) { // Using GUI.
			ci.printMessage(message + "\n");
		} else { // Using command console.
			System.out.println(message);
		}
	}
	
	/**
	Connect to the server.
	Creates a thread to listen to it.
	**/
	public boolean start() {
		try {
			socket = new Socket(server, port); // Socket creation.
											   // IP Address, port number
		} catch (Exception ex) {
			display("There was a problem connecting to the server: " + ex);
			return false; // Don't start client.
		}
		
		// display("Connection accepted to : " + socket.getInetAddress() + ":" + socket.getPort()); // Uncomment for checks.
		
		try { // Create data streams.
			streamInput = new ObjectInputStream(socket.getInputStream());
			streamOutput = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			display("There was a problem creating streams: " + ex);
			return false;
		}
		
		/*
		The client will need a thread to listen to the server.
		So:
		- the server needs multiple threads to service clients.
		- the client needs one thread to listen to the server.
		*/
		new ListenFromServer().start();
		
		try {
			streamOutput.writeObject(username); // Send an alias as a test to see if the server responds.
		} catch (IOException ex) {
			display("Problem with listening to Server: " + ex);
			disconnect(); // No point in running if server is down or w/e.
			return false;
		}
		
		return true; // Connections with server was a success.
	}
	
	/**
	Sends a message to the server.
	**/
	void sendMessage(ChatMessage message) {
		try {
			streamOutput.writeObject(message);
		} catch (IOException ex) {
			display("There was a problem writing to the server: " + ex);
		}
	}
	
	/**
	Closes streams and socket, and disconnects from server.
	Much like the close method from the Server class,
	you'll want to close these things individually.
	**/
	private void disconnect() {
		try {
			if (streamOutput != null) {
				streamOutput.close();
			}
		} catch (Exception ex) {
			// It was probably already closed.
		}
			
		try {
			if (streamInput != null) {
				streamInput.close();
			}
		} catch (Exception ex) {
			// It was probably already closed.
		}
			
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception ex) {
			// It was probably already closed.
		}
		
		if (ci != null) {
			ci.connectionFailed();
		}
	}
	
	/**
	The client program will be using a GUI, not a command prompt.
	
	If this was going to be a serious product (it's not, but - again - "if"),
	we can't assume that every user knows how to work a command prompt.
	They don't know ports.
	They don't know IP Addresses.
	We're beginners, so we probably don't either.  Ha.
	Anyways, until we establish port numbers and addresses, we'll settle for defaults for now.
	**/
	public static void main(String[] args) {
		
		int portNumber = 2000; // Default port.
		String serverAddress = "127.0.0.1"; // Default IP Address for testing.
		// String serverAddress = "172.28.76.1"; // Where the server actually is.
		String username = "Anon"; // Default alias.
		
		/*
		This block is meant for command prompts.
		We won't be using command prompts, but we need it to do some testing.
		*/
		switch (args.length) {
			case 2: // 2 arguments.
				serverAddress = args[1];
			case 1: // 1 argument.
				username = args[0];
				break;
			case 0: // No argument; prompt.
				Scanner scanner = new Scanner(System.in);

				System.out.println("What would you like to be known as in the chat room?");
				String user = scanner.nextLine();
				
				while (user.length() > USERNAME_LIMIT || user.trim().equals("")) {
					String alert = "Usernames can be up to " + USERNAME_LIMIT + " characters long and must not be empty." +
										"  Please try again:";
					System.out.println(alert);
					user = scanner.nextLine();
				}

				username = user;			

				break;
			default: // More than 2 arguments.
				System.out.println("Windows command prompt: > java Client [username] [IP address]");
				return;
		}
		
		Client client = new Client(serverAddress, portNumber, username); // Initialize client.
		
		// If client does not start, end program.
		if (!client.start()) {
			return;
		}
		
		Scanner scanner = new Scanner(System.in); // Our dearly beloved inputter.
		
		// Loops forever, listening for the user's messages.
		while (true) {
			System.out.print("> "); // Just a little indicator to enter a message.
			String message = scanner.nextLine(); // Get input.
			
			if (message.length() > MESSAGE_LIMIT || message.trim().equals("")) {
				// Alert that the message is either too long or empty.
				message = "Messages can be up to " + MESSAGE_LIMIT + " characters long" +
							" and cannot be empty.";
				System.out.println(message);
			}

			/*
			We won't actually be having the user enter 'logout' to log out,
			but this loop has to end in some way in order to disconnect.
			For now, we'll keep this.
			*/
			else if (message.equalsIgnoreCase("LOGOUT")) {
				client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
				break; // Break out of while loop.
			} else if (message.equalsIgnoreCase("ONLINE")) {
				client.sendMessage(new ChatMessage(ChatMessage.ONLINE, ""));
			} else {
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, message)); // Send message.
			}
		}
		
		client.disconnect(); // You get here when the loop is broken (break;).
	}
	
	/**
	Inner class.
	Waits for messages from the server.
	Again, it extends Thread, which uses an interface Runnable, which needs the run method.
	**/
	class ListenFromServer extends Thread {
		public void run() {
			while (true) {
				try {
					String message = (String)streamInput.readObject(); // Take message from server.
					
					if (message.substring(0, 7).equals("!updOn:") && ci != null) {
						String list = message.substring(7, message.length());
						ci.updateOnlineList(list);
					} else if (ci != null) {
						ci.printMessage(message);
					} else {
						System.out.println(message); // Print message.
						System.out.print("> "); // New indicator.
					}
				} catch (IOException ex) {
					// display("The server has closed the connection: " + ex); // For debugging
					display("You have been disconnected from the server."); // Logging out.
					
					if (ci != null) {
						ci.connectionFailed();
					}
					
					break; // Break from while loop.
				} catch (ClassNotFoundException ex) {
					// It's probably nothing.
				}
			}
		}
	}
}