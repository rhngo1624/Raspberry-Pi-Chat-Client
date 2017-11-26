import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
Change log:
11 / 17 : Working on JFrame.
11 / 19 : Work complete.
11 / 20 : Added function to accept online list broadcasts.
11 / 22 : Changing from default layout to GridBagLayout for better-looking interface.
11 / 24 : Completed character counting.
*/

/**
GUI.
**/
public class ClientInterface extends JFrame implements ActionListener, KeyListener {
	
	// Not really sure what this does since this class isn't serializable.
	private static final long serialVersionUID = 1L;
	
	private JLabel label; // For prompts.
	private JLabel characterCount; // Label that changes with the number of characters in a text.
	private JTextField textField; // For user to enter messages.
	
	private JButton login; // login and logout should appear seperately,
	private JButton logout; // depending on the status of the user.
	
	private JTextArea online; // List of who is online.
	private JTextArea messages; // Chat room.
	
	private boolean connected; // Is the user connected?
	private Client client; // The client itself.
	
	private static final int USERNAME_LIMIT = 14;
	private static final int MESSAGE_LIMIT = 280;
	
	// Defaults; change later.
	private static String server = "127.0.0.1";
	private static int port = 2000;
	private int defaultPort;
	private String defaultHost;
	
	/**
	Make window.
	**/
	public ClientInterface(String host, int port) {
		super("Chat Client"); // Window title.
		
		defaultHost = host;
		defaultPort = port;
		
		/*
		May prove useful:
		
		field.setBackground(Color.WHITE);
		field.setHorizontalAlignment(SwingConstants.SOUTH);
		*/
		
		// North panel
		// Not sure what should go here.  Some menus and more functionality?
		
		// South panel
		JPanel southPanel = new JPanel(new GridLayout(3, 1, 5, 5)); // 3 rows 1 column, margins 5
		label = new JLabel("What would you like to be known as in the chat room?"); // Label as a prompt.
		characterCount = new JLabel("(0 / 14)"); // Changes with num of characters in textField.
		textField = new JTextField("Anon"); // Field to answer prompt.
		textField.addKeyListener(this);
		southPanel.add(label);
		southPanel.add(characterCount);
		southPanel.add(textField);
		add(southPanel, BorderLayout.SOUTH);
		
		// Center panel
		JPanel centerPanel = new JPanel(new GridLayout(1, 1));
		messages = new JTextArea("Hello!\n", 80, 80); // 80 rows 80 columns
		messages.setLineWrap(true);
		messages.setEditable(false);
		centerPanel.add(new JScrollPane(messages)); // You can scroll up and down.
		add(centerPanel, BorderLayout.CENTER);
		
		// Right panel
		JPanel rightPanel = new JPanel(new GridLayout(2, 1));
		JLabel onlineLabel = new JLabel("Online:");
		online = new JTextArea("", 10, 10); // Testing rows and columns.
		online.setEditable(false);
		rightPanel.add(onlineLabel);
		rightPanel.add(new JScrollPane(online));
		add(rightPanel, BorderLayout.EAST);
		
		// Left panel
		JPanel leftPanel = new JPanel(new GridLayout(2, 1));
		// Login and logout buttons should switch depending on user status.
		login = new JButton("Login");
		logout = new JButton("Logout");
		login.addActionListener(this);
		logout.addActionListener(this);
		logout.setEnabled(false);
		leftPanel.add(login);
		leftPanel.add(logout);
		add(leftPanel, BorderLayout.WEST);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(600, 600); // width, height
		// pack();
		setLocationRelativeTo(null); // Window appears at the center of your screen.
		setVisible(true);
		textField.requestFocusInWindow(); // Sets the | caret to textField.
	}
	
	/**
	Adds a received message to the center panel.
	**/
	public void printMessage(String message) {
		messages.append(message);
		// Places the blinking text caret (|) below the message.
		messages.setCaretPosition(messages.getText().length() - 1);
	}
	
	/**
	If anyone logs in or out, the list on the right should update.
	**/
	public void updateOnlineList(String userList) {
		online.setText("");
		online.append(userList);
	}
	
	/**
	Events from buttons and text fields.
	**/
	public void actionPerformed(ActionEvent e) {
		Object o = e.getSource();
		
		if (o == logout) {
			client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));		
			return;
		}
		
		// User pressed enter / return
		if (connected) {
			String message = textField.getText();
			
			if (message.length() > MESSAGE_LIMIT || message.trim().equals("")) {
				printMessage("Messages can be up to " + MESSAGE_LIMIT + " characters long" +
							" and cannot be empty.\n");
				return;
			}
			
			client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, message));
			textField.setText("");
			
			return;
		}
		
		if (o == login) {
			enterChatRoom();
		}
	}
	
	public void keyTyped(KeyEvent e) {
		
	}
	
	public void keyPressed(KeyEvent e) {
		
	}
	
	/**
	For dynamic character count.
	**/
	public void keyReleased(KeyEvent e) {
		String string = textField.getText();
		int count = string.length();
		
		if (connected == false) {
			characterCount.setText("(" + count + " / " + USERNAME_LIMIT + ")");
			
			if (count > USERNAME_LIMIT || count == 0) {
				characterCount.setForeground(Color.RED);
			} else {
				characterCount.setForeground(Color.BLACK);
			}
			
		} else {
			characterCount.setText("(" + count + " / " + MESSAGE_LIMIT + ")");
			
			if (count > MESSAGE_LIMIT || count == 0) {
				characterCount.setForeground(Color.RED);
			} else {
				characterCount.setForeground(Color.BLACK);
			}
		}
	}
	
	/**
	Press the login button to enter the chat room.
	**/
	public void enterChatRoom() {
		String username = textField.getText().trim();
		
		if (username.length() == 0 || username.length() > USERNAME_LIMIT) {
			printMessage("Usernames can be up to " + USERNAME_LIMIT + " characters long and must not be empty." +
										"  Please try again.\n");
			return; // We don't want non-existant usernames, nor long ones.
		}
		
		client = new Client(server, port, username, this);
		
		if (!client.start()) {
			return; // Client failed to run.
		}
		
		connected = true;
		login.setEnabled(false);
		logout.setEnabled(true);
		label.setText(username + "'s Message:");
		textField.setText("");
		textField.addActionListener(this);
		characterCount.setText("(0 / 280)");
	}
	
	/**
	Otherwise known as connection lost.
	Revert back to the start of the application.
	**/
	public void connectionFailed() {
		login.setEnabled(true);
		logout.setEnabled(false);
		label.setText("Enter your username below:");
		textField.setText("Anon");
		textField.removeActionListener(this);
		characterCount.setText("(0 / 14)");
		connected = false;
	}

	public static void main(String[] args) {
		new ClientInterface(server, port);
	}
}