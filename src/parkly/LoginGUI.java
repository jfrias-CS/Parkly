package parkly;

import java.awt.*; // Import AWT components for layout and styling
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.*; // Import Swing components


public class LoginGUI {
	private final static String title = "Parkly Employee GUI";
	private final JFrame mainFrame;
	private final JDialog loginScreen;
	private final JTextField usernameField;
	private final JPasswordField passwordField;
	private final JTextField entryGateIdField;
//	private final JTextField exitGateIdField;
	private final JButton loginButton;
	private final JButton cancelButton;
	private static final Map<String, char[]> VALID_USERS = new HashMap<>();
	private static final String RESOURCE_PATH = "valid_users.txt";
	private boolean validUser = false;
	private EmployeeService es;
	
	// Constructor
	LoginGUI(EmployeeService es) {
		this.es = es;
		this.mainFrame = new JFrame(title);
		// MODERNIZE: Use title in the dialog box
		this.loginScreen = new JDialog(this.mainFrame, "Parkly Employee Login 🅿️", true); 
		
		// MODERNIZE: Use BorderLayout for outer structure and padding
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 15, 20)); // Padding
		
		// --- NORTH: Title/Header ---
		JLabel titleLabel = new JLabel("Employee Access Required", SwingConstants.CENTER);
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // Bottom padding
		mainPanel.add(titleLabel, BorderLayout.NORTH);
		
		// --- CENTER: Input Fields (Using GridBagLayout for alignment) ---
		JPanel inputPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5); // Padding between components
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		this.usernameField = new JTextField(20);
		this.passwordField = new JPasswordField(20);
		this.entryGateIdField = new JTextField(15);
//		this.exitGateIdField = new JTextField(15);
		// Username Label
		gbc.gridx = 0;
		gbc.gridy = 0;
		inputPanel.add(new JLabel("Username: ", SwingConstants.RIGHT), gbc);
		
		// Username Field
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0; // Allow field to expand horizontally
		inputPanel.add(this.usernameField, gbc);
		
		// Password Label
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0; // Reset weight for label
		inputPanel.add(new JLabel("Password: ", SwingConstants.RIGHT), gbc);
		
		// Password Field
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1.0;
		inputPanel.add(this.passwordField, gbc);
		
		// Entry Gate Label
		gbc.gridx = 0; 
		gbc.gridy = 2; 
		
		// Entry Gate Field
		inputPanel.add(new JLabel("Entry Gate ID: ", SwingConstants.RIGHT), gbc);
		gbc.gridx = 1; 
		gbc.gridy = 2; 
		gbc.weightx = 1.0; 
		inputPanel.add(this.entryGateIdField, gbc);

		
		mainPanel.add(inputPanel, BorderLayout.CENTER);
		
		// --- SOUTH: Buttons ---
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Align buttons to the right
		this.loginButton = new JButton("🔑 Login");
		this.cancelButton = new JButton("🚪 Close");
		
		// Style buttons
		this.loginButton.setBackground(new Color(0, 123, 255)); // Blue
		this.loginButton.setForeground(Color.WHITE);
		this.loginButton.setOpaque(true);
		this.loginButton.setBorderPainted(false);
		this.loginButton.setFont(new Font("SansSerif", Font.BOLD, 12));
		
		this.cancelButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
		
		buttonPanel.add(this.loginButton);
		buttonPanel.add(this.cancelButton);
		
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		this.loginScreen.setContentPane(mainPanel);
		
		this.validUser = false;
		// Use button to run user input through validation
		// MODERNIZE: Use Lambda for ActionListener
		this.loginButton.addActionListener(e -> {
			String username = usernameField.getText();
			char[] password = passwordField.getPassword();
			String entranceGate = entryGateIdField.getText().trim();
//			String exitGate = exitGateIdField.getText().trim();
//			String username = "manager";
//			char[] password = {'a', 'd', 'm', 'i', 'n', '1', '2', '3'};
//			String entranceGate = "G1";
//			String exitGate = "G2";
			if (username.isEmpty() || password.length == 0 || entranceGate.isEmpty() /*|| exitGate.isEmpty()*/) {
		        JOptionPane.showMessageDialog(loginScreen, "Please enter Username, Password, and Gate ID.", "Input Required", JOptionPane.ERROR_MESSAGE);
		        validUser = false;
		    } else {
		        // TRY TO CHANGE TO SERVER CALL
		    	validateUsers(username, password, entranceGate/*, exitGate*/);
		    }
			Arrays.fill(password, ' ');
		});
		
		// Close window
		this.cancelButton.addActionListener(e -> {
			if (JOptionPane.showConfirmDialog(loginScreen, "Are you sure you want to close the application?", "Confirm Close", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		});	
		
	}
	
	// Validation function to approve users
	private void validateUsers(String username, char[] password, String entranceGate/*, String exitGate*/) {
//		String strPassword = new String(password);
		new LoginTask(username, new String(password), entranceGate/*, exitGate*/).execute();
	}
	
	// Return boolean value for inputs
	public boolean isAuthenticated() {
		return this.validUser;
	}
	
	// Display login window to user upon running application code
	public void show() {
		this.loginScreen.pack();
		this.loginScreen.setLocationRelativeTo(null);
		this.loginScreen.setVisible(true);
	}

	public String getUsernameInput() {
		return this.usernameField.getText();
	}
	
	public String getEntryGateIdInput() {
		return this.entryGateIdField.getText();
	}
//	public String getExitGateIdInput() {
//		return this.exitGateIdField.getText();
//	}
	// Conceptual LoginTask.java

	private class LoginTask extends SwingWorker<Boolean, Void> {
	    private final String username;
	    private final String password;
	    private final String entranceGate;
//	    private final String exitGate;
	    private String exitGate;
	    public LoginTask(String username, String password, String entranceGate/*, String exitGate*/) {
	        this.username = username;
	        this.password = password;
	        this.entranceGate = entranceGate;
//	        this.exitGate = exitGate;
	    }

	    @Override
	    protected Boolean doInBackground() throws Exception {
	        // 1. Package credentials into a Message (Type: LOGIN, Status: REQUEST)
	        // Combine all data into the message text, delimited for server parsing
	        String credentials = username + "|" + password + "|" + entranceGate; //+ "|" + exitGate;
	        Message loginMsg = new Message("LOGIN", "REQUEST", credentials);

	        // 2. Send the message and synchronously wait for the response
	        // You'll need to create a static method in SocketConnectionService 
	        // to handle this specific synchronous wait.
	        Message response = es.sendLoginRequest(loginMsg); 

	        // 3. Process the server's response
	        if (response != null && response.getStatus().equalsIgnoreCase("SUCCESS")) {
	        	this.exitGate = response.getText(); // Text is the key value of the login gate value
//	        	System.out.println("Exit gate: " + this.exitGate);
	            return true; // Login successful
	        }
	        return false; // Login failed or server error
	    }

	    @Override
	    protected void done() {
	        try {
	            boolean success = get(); // Get result from doInBackground()
	            if (success) {
	            	LoginGUI.this.validUser = true;
	                // If successful, create the Employee session and proceed to dashboard
	                Employee newEmployee = new Employee(username, entranceGate, exitGate);
	                es.setLoggedInEmployee(newEmployee);
	                LoginGUI.this.loginScreen.dispose();
	                System.out.println("LoginGUI.done(): Successful Login. Creating Dashboard...");
	                EmployeeGUI newGUI = new EmployeeGUI();
	                newGUI.createEmployeeDashboard(es);
	            } else {
	            	LoginGUI.this.validUser = false;
	                JOptionPane.showMessageDialog(null, "Login failed: Invalid credentials or gate ID.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
	                // Important: Disconnect the socket if login failed
	                passwordField.setText("");
	                passwordField.requestFocus();
	            }
	        } catch (Exception e) {
	            JOptionPane.showMessageDialog(null, "Server communication error during login.", "Network Error", JOptionPane.ERROR_MESSAGE);
	            es.disconnect();
	        }
	    }
	}
}