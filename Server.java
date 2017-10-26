import java.io.*; // for input-output
import java.net.*; // for TCP and sockets
import java.text.SimpleDateFormat; // for the timestamps
import java.util.*; // for everything else; Date, etc.
	
/**
The server services multiple clients with client threads.
The server recieves and broadcasts messages.

Keep in mind that there is an inner class called ClientThread.
**/
public class Server {
	
	private static int uniqueID; // Each connection will have a unique ID.
	private ArrayList<ClientThread> clients; // A list of client threads.
	private SimpleDateFormat timestamp; // The timestamp of a message.
	private int port; // The port number for connections.
	private boolean loop; // The server should be on a loop and keep on listening for clients.
	
	/**
	Constructor initializes some variables.
	**/
	public Server(int port) {
		this.port = port;
		timestamp = new SimpleDateFormat("HH:mm:ss");
		clients = new ArrayList<ClientThread>();
	}
	
	/**
	The server will show what is happening to it through statements.
	These statements are meant to keep track on what is going on in the server.
	These statements will have timestamps.
	**/
	private void display(String event) {
		String statement = timestamp.format(new Date()) + ": " + event;
		System.out.println(statement);
	}
	
	/**
	Starts the server.
	**/
	public void start() {
		loop = true; // Set loop.
		
		try {
			ServerSocket serverSocket = new ServerSocket(port); // Open up a socket on the server at whatever 'port' is.
			
			while (loop) {
				display("Server waiting for clients at port: " + port + ".");
				
				Socket socket = serverSocket.accept(); // Accept client connections.
				
				if (!loop) { // If the server must stop...
					break; // To break out of this while loop.
				}
				
				ClientThread ct = new ClientThread(socket); // Make a thread to service the client.
				clients.add(ct); // Add that thread to the list of clients.
				ct.start(); // Run thread; service client; make everyone happy.
				
				/*
				After the thread starts, the next iteration of the loop will have the server accept more clients.
				This is the beginning of multi-threading.
				*/
			}
			
			try { // The only way for the program to reach this level is if loop was set to false.
				serverSocket.close(); // Close the server socket; accept no more clients.
				
				for (int i = 0; i < clients.size(); ++i) { // Iterate through the list of clients.
					ClientThread ct = clients.get(i); // For every client thread servicing them...
					
					try { // close the thread's streams and socket.
						ct.streamInput.close();
						ct.streamOutput.close();
						ct.socket.close();
					} catch (IOException ex) {
						System.err.print("There was a problem trying to close thread streams.");
					}
				}
			} catch (Exception ex) {
				display("There was a problem in trying to close the server and clients.");
			}
		} catch (IOException ex) {
			display("There was a problem on creating a server socket.");
		}
	}
	
	/**
	The server broadcasts a message it has received to its client threads.
	**/
	private synchronized void broadcast(String message) {
		String stamp = timestamp.format(new Date());
		String timedMessage = stamp + ": " + message + "\n";
		
		/*
		Print the message on the server.
		This doesn't seem to have a point, but I leave this uncommented for debugging.
		*/
		System.out.println(timedMessage); 
		
		// Iteration occurs in reverse order because clients may disconnect.
		for (int i = clients.size(); --i >= 0; ) {
			ClientThread ct = clients.get(i);
			
			if (!ct.writeMessage(timedMessage)) { // Have a thread send a broadcasted message.
				clients.remove(i); // If the broadcast fails, remove it; the client disconnected.
				display("Client disconnected -> " + ct.username + ".");
			}
		}
	}
	
	/**
	Removes a client from the list.
	**/
	synchronized void removeClient(int id) {
		
		for (int i = 0; i < clients.size(); ++i) { // Go through client list.
			ClientThread ct = clients.get(i);
			
			if (ct.id == id) { // Remove client.
				clients.remove(i);
				return; // End for loop.
			}
		}
	}
	
	/**
	Stops the server.
	**/
	protected void stop() {
		loop = false; // Stops the while loop in start().
		
		/*
		In order to get to the if !loop statement, the server will have to connect to itself.
		This tricks itself into thinking it's a client and allows the while loop to reach the if statement.
		*/
		try {
			new Socket("127.0.0.1", port);
			/*
			127.0.0.1 is an internet protocal address (IP address).
			It means "localhost."  Some people call it "HOME."
			In fact, you may replace the IP address with the word "localhost."
			The result is the same.
			It essentially means, "Connect to your own computer."
			*/
		} catch (Exception ex) {
			System.err.print("There was a problem stopping the server.");
		}
	}
	
	/**
	The idea is for the server to run on a console.
	Cut out the middle man (Eclipse or w/e) and just have the console as your GUI.
	Command prompt makes for a professionial-looking error checker.
	Don't like command prompt?  Let me know.
	
	To run the server, (for Windows) change directory to wherever you stored this file.
	When I open my command prompt, the first thing I see is:
		C:\Users\Ricky>
	
	Let us say my Server.java file (this file you're looking at right now) is on my desktop.
	I enter into my command prompt:
		cd desktop
	
	After pressing enter/return, I now see:
		C:\Users\Ricky\Desktop
	
	I'll probably be giving you a Server.class file as well as a Server.java file.
	Just in case you do not have a .class file...
	After I changed directory to desktop, I enter a new command:
		javac Server.java
		
	I hope your computer has all the tools necessary to compile the .java file.
	After you press enter/return, you'll know.
	If you see a Server.class file with your Server.java file, great - you succeeded.
	If there was some sort of error, look into it on the Internet.
	
	'javac' is a command that compiles a java file, hence java'c'.
	If you go on to software development/engineering or w/e, this is important to know.
	The sooner you get this to work, the happier you'll be.  Probably.
	
	Now that the Server.class file is on your desktop (or wherever the .java file was),
	enter into the command prompt:
		java Server
		
	The server should run.

	To exit the server: ctrl + c
	**/
	public static void main(String[] args) {
		
		int portNumber = 2000; // The default port number the server will service on.
		
		/*
		I mentioned command prompts above because of its convenience.
		You see the String[] args on the main method's signature?  That's for command prompts.
		
		Let's say you didn't want the server to run on port number 2000; you wanted it on 2500.
		Instead of the command 'java Server', why not do this:
			java Server 2500
			
		and let your program do the work from there?
		*/
		switch (args.length) { // Check for command line arguments.
			case 1: // There was one argument.
				try {
					portNumber = Integer.parseInt(args[0]); // Attempt to convert it to int.
				} catch (Exception ex) {
					System.err.print("Invalid port number.\n"); // That was not an int.
					System.err.print("Windows command prompt: > java Server [number]"); // How to use.
					return; // End program.
				}
			case 0: // No argument.
				break; // Get out of switch statement.
			default: // There was more than one argument.
				System.out.println("Windows command prompt: > java Server [number]");
				return; // End program.
		}
		
		Server server = new Server(portNumber);
		server.start();
	}
	
	/**
	Inner class.
	There will be multiple of these running.
	**/
	class ClientThread extends Thread {
		
		Socket socket; // A connection point for the client and the server.
		ObjectInputStream streamInput; // Stream to send messages.
		ObjectOutputStream streamOutput; // Stream to recieve messages.
		int id; // Every thread has its own id (no, now the one in psychology).
		String username; // Every client has some alias.
		ChatMessage cm; // What the message will be.
		
		/**
		Makes a thread using information on a client's socket.
		**/
		ClientThread(Socket socket) {
			id = ++uniqueID; // Set a unique ID based on the server's.
			this.socket = socket;
			
			try { // Using the client's socket, set a thread's variables.
				streamOutput = new ObjectOutputStream(socket.getOutputStream());
				streamInput = new ObjectInputStream(socket.getInputStream());
				username = (String)streamInput.readObject();
				display(username + " just joined the chat room.  Welcome.");
			} catch (IOException ex) {
				display("There was a problem at a thread's stream creation.");
				return; // Don't make the thread.
			} catch (ClassNotFoundException ex) { // I was trying to read an object being sent.  It's probably nothing.
				display("Class not found exception.");
			}
		}
		
		/**
		Since this class extends Thread, and Thread implements Runnable, it must define a run() method.
		The thread will be at a client's beck and call for as long as it possibly can.
		**/
		public void run() {
			boolean loop = true; // Run forever.
			
			while (loop) {
				
				try {
					cm = (ChatMessage)streamInput.readObject();
				} catch (IOException ex) {
					display(username + " has a stream problem: " + ex);
					break;
				} catch (ClassNotFoundException ex) {
					display("Class not found exception.");
					break;
				}
				
				String message = cm.getMessage(); // Get client's message.
				
				switch (cm.getType()) {
					case ChatMessage.MESSAGE:
						broadcast(username + ": " + message); // Ask server to broadcast it.
						break;
					case ChatMessage.LOGOUT:
						broadcast(username + " has left the chat room.");
						loop = false;
						break;
				}
				
				// This thread then runs again, waiting for more of the client's messages.
			}
			
			// The client has disconnected.
			removeClient(id); // Call on Server's removeClient method.
			close(); // Close thread.
		}
		
		/**
		Close streams and socket.
		We close them individually because one may already closed, but not the others.
		If we try to close them all together and an exception occurs, they will revert and not be closed.
		**/
		private void close() {
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
		}
		
		/**
		Send message to client.
		**/
		private boolean writeMessage(String message) {
			if (!socket.isConnected()) { // If client is not connected...
				close(); // close thread.
				return false; // Return false and have server remove user.
			}
			
			try {
				streamOutput.writeObject(message); // Send message to user.
			} catch (IOException ex) {
				display("There was a problem sending a message to " + username);
				display(ex.toString()); // Alert the server that the message could not be sent.
			}
			
			return true; // Don't remove user.
		}
	}
	
	/*
	For future implementation:

	For the client:
	GUI:
	Users online:
		for (int i = 0; i < clients.size(); ++i) {
			ClientThread ct = clients.get(i);
			System.out.println(ct.username);
		}
	*/
}