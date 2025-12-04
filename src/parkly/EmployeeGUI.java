package parkly;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;
import java.util.List;
public class EmployeeGUI {
	private static JFrame mainFrame;
	private static JTextArea serverTextBox;
	private static JPanel panel;
	private static JPanel displayServerPanel;
	private static JLabel serverTextLabel;
	private static JPanel inputTextPanel;
	private static JLabel inputTextLabel;
	private static JTextArea inputText;
	private static JButton sendButton;
	private static JButton logoutButton;
	private static JPanel imagePanel;
	private static ImageIcon parklyImageIcon;
	private static Image scaledIcon;
	private static JLabel imageIconLabel;
	private static JLabel dateTimeLabel;
	private static Timer timer;
	private static JButton openEntryGateButton;
	private static JButton viewActiveTicketsButton;
	private static JButton payFeesButton;
	private static JButton viewPaymentsButton;
	private static JButton openExitGateButton;
	private static JButton spaceTrackerButton;
	private static JButton reportButton;
	private EmployeeService es;
	
	void createEmployeeDashboard(EmployeeService es) {
		this.es = es;
		// Main container
		mainFrame = new JFrame("PARKLY EMPLOYEE DASHBOARD 🅿️");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(900, 700); 
		mainFrame.setPreferredSize(new Dimension(900, 700));
		mainFrame.setLocationRelativeTo(null);
		
		// Main panel to hold sub panels
		panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

		
		// NORTH (Header/Image/DateTime)
		JPanel headerPanel = new JPanel(new BorderLayout(20, 0)); // Panel for icon and clock
		
		imagePanel = new JPanel(); // hold image icon label
		parklyImageIcon = new ImageIcon("images/Parkly_Icon.png"); // hold software icon
		scaledIcon = parklyImageIcon.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH); // re-scale image icon
		parklyImageIcon = new ImageIcon(scaledIcon); // reset image to re-scaled size
		imageIconLabel = new JLabel(parklyImageIcon);
		imagePanel.add(imageIconLabel);
		
		// display date and time
		dateTimeLabel = new JLabel("Loading Time...", JLabel.CENTER);
		dateTimeLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Larger font for clock
		dateTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Align clock to the right
		
		headerPanel.add(imagePanel, BorderLayout.WEST);
		headerPanel.add(dateTimeLabel, BorderLayout.EAST);
		
		panel.add(headerPanel, BorderLayout.NORTH);


		// WEST (Control Buttons)
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBorder(BorderFactory.createTitledBorder("Gate & Transaction Controls"));
		
		spaceTrackerButton = new JButton("View Space Tracking");
		spaceTrackerButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center buttons in the box layout
		spaceTrackerButton.setMaximumSize(new Dimension(300, 40)); // Standardize size
		spaceTrackerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("EmployeeGUI: View Spaces.");
				String spaces = es.getSpaces();
				String[] parts = spaces.split("\\|");
	    		if (parts.length == 3) {
	    			String capacity = parts[0];
	    			String quantity = parts[1];
	    			String available = parts[2];
	    			EmployeeGUI.appendServerMessage("Capacity: " + parts[0] + "\nQuantity: " + parts[1] + "\nAvailable: " + parts[2]); 
	    		}
			}
		});
		
		// View Valid Tickets
//		viewActiveTicketsButton = new JButton("🎟View Active Tickets️");
		viewActiveTicketsButton = new JButton("View Active Tickets️");
		viewActiveTicketsButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center buttons in the box layout
		viewActiveTicketsButton.setMaximumSize(new Dimension(300, 40)); // Standardize size
		viewActiveTicketsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("EmployeeGUI: View Active Tickets.");
				appendServerMessage("Active Tickets Start:");
				Map<String, LocalTicket> activeTicketsMap = es.getLocalActiveTickets();
				EmployeeGUI.appendServerMessage("Size: " + activeTicketsMap.size());
			    for (LocalTicket current : activeTicketsMap.values()) {
			        EmployeeGUI.appendServerMessage(
			            "\tTicket ID: " + current.getTicketID() + "\n"
			          + "\tEmployeeID: " + current.getEmployeeID() + "\n"
			          + "\tGateID: " + current.getGateID() + "\n"
			          + "\tEntry Date: " + current.getEntryDate() + "\n"
			          + "\tEntryTime: " + current.getEntryTime() + "\n"
			        );
			    }
			    appendServerMessage("Active Tickets End.");
			}
		});
		
		// Open entry gate button + Generate ticket
//		openEntryGateButton = new JButton("🚪 Open Entry Gate + Make New Ticket");
		openEntryGateButton = new JButton("Open Entry Gate + Make New Ticket");
		openEntryGateButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center buttons in the box layout
		openEntryGateButton.setMaximumSize(new Dimension(300, 40)); // Standardize size
		openEntryGateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("EmployeeGUI: Open Gate and Create Ticket.");
				LocalTicket newTicket = es.generateTicket();
				if (newTicket != null) {
					openEntryGateButton.setEnabled(false);
					EmployeeGUI.appendServerMessage("TICKET REQUESTED");
					String serverMessage = "Ticket ID: " + newTicket.getTicketID() + "\n\tEmployee: " + newTicket.getEmployeeID() + "\n\tGate: " + newTicket.getGateID() + "\tEnrty Date: " + newTicket.getEntryDate() + "\n\t"
					         + "Entry Time: " + newTicket.getEntryTime() + "\n\t"
					         + "Total Time: " + newTicket.getTotalTime() + " minutes\n";
					
					EmployeeGUI.appendServerMessage(serverMessage);
					openEntryGateButton.setEnabled(true);
					es.openEntryGate();
				} else {
					openEntryGateButton.setEnabled(true);
				}
			}
		});
		
//		viewPaymentsButton = new JButton ("💸View Payments");
		viewPaymentsButton = new JButton ("View Payments");
		viewPaymentsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		viewPaymentsButton.setMaximumSize(new Dimension(300, 40));
		viewPaymentsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("EmployeeGUI: View Payments.");
				appendServerMessage("Payments: ");
				Map<String, LocalPayment> payments = es.getLocalPayments();
				EmployeeGUI.appendServerMessage("Size: " + payments.size());
			    for (LocalPayment current : payments.values()) {
			        EmployeeGUI.appendServerMessage(
			            "\tPayment ID: " + current.getTicketID() + "\n"
			          + "\tEmployeeID: " + current.getEmployeeID() + "\n"
			          + "\tGateID: " + current.getGateID() + "\n"
			          + "\tEntry Date: " + current.getPaymentDate() + "\n"
			          + "\tEntry Time: " + current.getPaymentTime() + "\n"
			          + "\tPayment Type: " + current.getPaymentMethod() + "\n"
			          + "\tPayment Amount: " + current.getPaymentAmount() + "\n"
			        );
			    }
			}
		});
		
		// Pay fees
//		payFeesButton = new JButton("💳Lookup Ticket + Pay Fees");
		payFeesButton = new JButton("Lookup Ticket + Pay Fees");
		payFeesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		payFeesButton.setMaximumSize(new Dimension(300, 40));
		payFeesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FeeGUI feePaymentWindow = new FeeGUI(mainFrame, es);
				feePaymentWindow.setVisible(true);
//				feePaymentWindow.getProcessedTicket();
			}
		});
		
		// Open Exit Gate Button
//		openExitGateButton = new JButton("🚧 Open Exit Gate");
		openExitGateButton = new JButton("Open Exit Gate");
		openExitGateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		openExitGateButton.setMaximumSize(new Dimension(300, 40));
		openExitGateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OpenGateGUI openGateWindow = new OpenGateGUI(mainFrame, es);
				openGateWindow.setVisible(true);
//				openGateWindow.getProcessedTicket();
			}
		});
		
		
		// Report Button not functional yet
		/*
		reportButton = new JButton("Generate Report");
		reportButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		reportButton.setMaximumSize(new Dimension(300, 40));
		reportButton.addActionListener(e -> {
			String date = JOptionPane.showInputDialog(mainFrame, "Enter report date (M/d/yyy): ", "Generate Reporrt", JOptionPane.QUESTION_MESSAGE);
			if (date == null || date.trim().isEmpty()) {
				return;
			}
			Report report = EmployeeService.getReport(date.trim());
			if (report == null) {
				JOptionPane.showMessageDialog(mainFrame, "Could not retreive report (server error or timeout).", "Report Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			List<LocalTicket> tickets = report.getTickets();
			if (tickets.isEmpty()) {
		        JOptionPane.showMessageDialog(
		                mainFrame,
		                "No tickets found for " + report.getDate(),
		                "Report",
		                JOptionPane.INFORMATION_MESSAGE);
		        return;
		    }

		    StringBuilder sb = new StringBuilder();
		    sb.append("Report for ").append(report.getDate()).append("\n\n");
		    for (Ticket t : tickets) {
		        sb.append("Ticket ")
		          .append(t.getTicketID())
		          .append(" | Hours: ").append(t.getTotalTime())
		          .append(" | Amount Paid: $")
		          .append(String.format("%.2f", t.getTotalFees() / 1.0))
		          .append(" | Paid: ").append(t.isPaid() ? "Yes" : "No")
		          .append("\n");
		    }
		    
		    EmployeeGUI.appendServerMessage(
		    	    "Server: Generated report for " + report.getDate() +
		    	    " (" + tickets.size() + " tickets)"
		    	);


		    JTextArea area = new JTextArea(sb.toString(), 15, 60);
		    area.setEditable(false);
		    JOptionPane.showMessageDialog(
		            mainFrame,
		            new JScrollPane(area),
		            "Report for " + report.getDate(),
		            JOptionPane.INFORMATION_MESSAGE);
		});
		*/
		
		// Logout button
//		logoutButton = new JButton("❌ Logout");
		logoutButton = new JButton("Logout");
		logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		logoutButton.setMaximumSize(new Dimension(300, 40));
		logoutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to logout of dashboard?", "Logout of dashboard?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
					try {
						if (timer != null && timer.isRunning()) {
							timer.stop(); 
						}
						es.disconnect();
						mainFrame.dispose();
						EmployeeGUI.startLoginAndConnectFlow();
					} catch (Exception e1) {
						System.err.println("EmployeeGUI: Error during socket disconnection, proceeding with exit: " + e1.getMessage());
					}
					
				}
			}
		});
		
		// Add buttons to Panel
		// Add vertical spacing between buttons
		controlPanel.add(Box.createVerticalStrut(15));
		controlPanel.add(spaceTrackerButton);
		controlPanel.add(Box.createVerticalStrut(15));
		controlPanel.add(viewActiveTicketsButton);
		controlPanel.add(Box.createVerticalStrut(15));
		controlPanel.add(openEntryGateButton);
		controlPanel.add(Box.createVerticalStrut(15));
		controlPanel.add(payFeesButton);
		controlPanel.add(Box.createVerticalStrut(15));
		controlPanel.add(viewPaymentsButton);
		controlPanel.add(Box.createVerticalStrut(15));
		controlPanel.add(openExitGateButton);
		controlPanel.add(Box.createVerticalStrut(30)); // Extra space before logout
		controlPanel.add(Box.createVerticalStrut(30));
//		controlPanel.add(reportButton);
		controlPanel.add(logoutButton);
		controlPanel.add(Box.createVerticalStrut(15));
		
		panel.add(controlPanel, BorderLayout.WEST);
		
		
		// CENTER & SOUTH (Server Console and Manual Input)
		
		JPanel consoleAndInputPanel = new JPanel(new BorderLayout(5, 5));

		// Display Communication Panel
		displayServerPanel = new JPanel(new BorderLayout());
		displayServerPanel.setBorder(BorderFactory.createTitledBorder("Server Communication Console"));
		
		serverTextLabel = new JLabel("Server: ", JLabel.LEFT);
		// displayServerPanel.add(serverTextLabel, BorderLayout.NORTH); // Moved label inside titled border
		
		String response = "Connection established. Ready to receive server messages...\n";
		serverTextBox = new JTextArea(response, 10, 10);
		serverTextBox.setEditable(false);
		serverTextBox.setFont(new Font("Monospaced", Font.PLAIN, 12)); // Monospaced font for console
		displayServerPanel.add(new JScrollPane(serverTextBox), BorderLayout.CENTER);
		
		consoleAndInputPanel.add(displayServerPanel, BorderLayout.CENTER);
		
		
		// South User input text panel
//		inputTextPanel = new JPanel(new GridLayout(3, 1)); // panel to send messages
		inputTextPanel = new JPanel(new BorderLayout(5, 5));
		inputTextPanel.setBorder(BorderFactory.createTitledBorder("Manual Command (Dev/Test)"));

		inputTextLabel = new JLabel("Enter text: ", JLabel.LEFT);
		inputText = new JTextArea(3, 10); // Give input text box some height
		inputText.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		// Send button
		sendButton = new JButton("⬆️ Send Command");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String inputToSend = inputText.getText();
				if (!inputToSend.isEmpty()) {
				Message msgToSend = new Message("text", "success", inputToSend);
				System.out.println("Message being sent: " + msgToSend.getType() + " | " + msgToSend.getStatus() + " | " + msgToSend.getText());
//				socket.sendMessage(msgToSend);
				appendServerMessage("You: " + inputToSend + "\n");
				SocketConnectionService.sendMessage(msgToSend);
				inputText.setText("");
				}
			}
		});
		
		inputTextPanel.add(inputTextLabel, BorderLayout.WEST);
		inputTextPanel.add(new JScrollPane(inputText), BorderLayout.CENTER); // Wrap input in JScrollPane
		inputTextPanel.add(sendButton, BorderLayout.EAST);
		
		consoleAndInputPanel.add(inputTextPanel, BorderLayout.SOUTH);
		
		panel.add(consoleAndInputPanel, BorderLayout.CENTER); // Add console and input to main center
		
		
		// Close window logs out / disconnects correctly 
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
			    if (JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to close the dashboard and disconnect?", "Close Application?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
			        try {
			            // Attempt to disconnect socket, but don't let it hang the app
			        	if (timer != null && timer.isRunning()) {
			        		timer.stop();
			        	}
			            es.disconnect(); 
			        } catch (Exception e) {
			            System.err.println("Error during socket disconnection, proceeding with exit: " + e.getMessage());
			        }
			        mainFrame.dispose();
			        System.exit(0);
			        
			    }
			}
		});
		
		mainFrame.add(panel);
		mainFrame.pack();
		mainFrame.setVisible(true);
		
		// Action listener to update label
		ActionListener updateTimeAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalDateTime now = java.time.LocalDateTime.now();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy | HH:mm:ss"); 
				String formattedDateTime = now.format(formatter);
				dateTimeLabel.setText(formattedDateTime);
			}
		};
		// Timer to keep track of time
		timer = new Timer(1000, updateTimeAction);
		timer.start();
	}

	public static void appendServerMessage(String message) {
        // Ensure this update runs on the EDT
        SwingUtilities.invokeLater(() -> {
            if (serverTextBox != null) {
                serverTextBox.append(message + "\n");
                serverTextBox.setCaretPosition(serverTextBox.getDocument().getLength());
            }
        });
    }
	
	public static void startLoginAndConnectFlow() {
		ConnectTask newTask = new ConnectTask(null);
		newTask.execute();
	}
}