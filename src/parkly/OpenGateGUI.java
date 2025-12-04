package parkly;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter; // Import if you need to display formatted dates

public class OpenGateGUI extends JDialog {
    // --- Components ---
	private final JPanel mainPanel;
	private final EmployeeService es;
	private final JPanel searchPanel;
	private final JLabel searchTicketLabel;
	private final JTextField searchTicketText;
	private final JButton searchTicketButton;
	private final JPanel detailsPanel;
	private JTextArea ticketDetailsArea; 
	private JLabel feeLabel;
	private String ticket; 
    private JButton mainActionButton;
    private JButton overrideButton;
    private JTextField overrideReasonText;
    
    // Local copy of the found ticket
    private LocalTicket currentTicket;


    public static void setLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {}
        }
    }

	public OpenGateGUI(JFrame owner, EmployeeService es) {
		super(owner, "Open Exit Gate", ModalityType.APPLICATION_MODAL);
		this.es = es;
        
		this.setSize(600, 450); 
		this.setLocationRelativeTo(owner);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		mainPanel = new JPanel(new BorderLayout(15, 15)); 
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); 

        // --- Top Search Panel ---
		searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); 
        searchTicketLabel = new JLabel("💳 Enter Ticket ID:");
		searchTicketText = new JTextField(15);
		searchTicketButton = new JButton("🔍 Search Ticket");
        
		searchPanel.add(searchTicketLabel);
		searchPanel.add(searchTicketText);
		searchPanel.add(searchTicketButton);
		
		mainPanel.add(searchPanel, BorderLayout.NORTH);
		
		// Center Details Panel (Dynamically populated)
		detailsPanel = new JPanel(new GridBagLayout()); 
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Ticket Information & Gate Control"));
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
		
		this.getContentPane().add(mainPanel);
		this.pack();
		
		// Action Listener
		searchTicketButton.addActionListener(e -> {
			String searchTicketInput = searchTicketText.getText().trim();
			if (!searchTicketInput.isEmpty()) {
				searchTicket(searchTicketInput);
			} else {
                JOptionPane.showMessageDialog(this, "Please enter a Ticket ID.", "Input Required", JOptionPane.WARNING_MESSAGE);
            }
		});
	}
	
	
	// Function to launch task to search for ticket in server data
	private void searchTicket(String input) {
		new TicketSearchTask(input).execute();
	}

     // Dynamically displays ticket details and the appropriate action controls 
     // based on the ticket's paid status.
     
	private void displayTicketDetails(LocalTicket foundTicket) {
		detailsPanel.removeAll();
        this.currentTicket = foundTicket; // Store the ticket locally
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
		System.out.println("TICKET RECEIVED: " + foundTicket.getTotalTime());
		
		// Ticket Details Section 
        String detailsText = String.format(
            "Ticket ID: %s\n" +
            "Entry Date: %s | Entry Time: %s\n" +
            "Exit Date: %s | Exit Time: %s\n" +
            "Total Parked Time (hrs): %d\n" +
            "Ticket Paid: %s\n",
            foundTicket.getTicketID(),
            foundTicket.getEntryDate(), foundTicket.getEntryTime(),
            foundTicket.getExitDate(), foundTicket.getExitTime(),
            foundTicket.getTotalTime(), foundTicket.isPaid()
        );
        
        ticketDetailsArea = new JTextArea(detailsText, 5, 30); 
        ticketDetailsArea.setEditable(false);
        ticketDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ticketDetailsArea.setBackground(detailsPanel.getBackground()); 
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; 
        JScrollPane scrollPane = new JScrollPane(ticketDetailsArea);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        detailsPanel.add(scrollPane, gbc);
        
		// Fees Due Label
        boolean isTicketPaid = foundTicket.isPaid();
		int fee = foundTicket.getTotalFees();
		double feeDouble = (double) fee;
		
		if (isTicketPaid) {
			feeLabel = new JLabel("✅ TICKET IS PAID. READY FOR EXIT.");
			feeLabel.setForeground(Color.green.darker());
		} else {
			feeLabel = new JLabel("⚠️ TICKET NOT PAID. FEES DUE: $" + String.format("%.2f", feeDouble));
		    feeLabel.setForeground(Color.red.darker());
		}
        
        feeLabel.setFont(new Font("SansSerif", Font.BOLD, 18)); 
        
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        detailsPanel.add(feeLabel, gbc);
        
		// Conditional Action Panel
		JPanel actionPanel = new JPanel(new GridBagLayout());
        
        // Use separate GBC for actionPanel components
        GridBagConstraints actionGBC = new GridBagConstraints();
        actionGBC.insets = new Insets(5, 5, 5, 5); 
        actionGBC.fill = GridBagConstraints.HORIZONTAL;
        actionGBC.gridx = 0;
        actionGBC.gridy = 0;
		
        if (isTicketPaid) {
            // --- Paid: Show Open Gate Button and Override Controls ---
            mainActionButton = new JButton("🚪 Open Exit Gate");
            mainActionButton.setBackground(new Color(34, 139, 34)); // Green
            mainActionButton.setForeground(Color.WHITE);
            mainActionButton.setOpaque(true);
            mainActionButton.setBorderPainted(false);
            mainActionButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            mainActionButton.addActionListener(e -> openGate(foundTicket.getTicketID(), false)); // Normal open
            actionPanel.add(mainActionButton, actionGBC);
            
        } else {
            //Unpaid: Show Pay Fees Button and Override Controls
            mainActionButton = new JButton("💳 Pay Ticket Fees");
            mainActionButton.setBackground(new Color(0, 123, 255)); // Blue
            mainActionButton.setForeground(Color.WHITE);
            mainActionButton.setOpaque(true);
            mainActionButton.setBorderPainted(false);
            mainActionButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            mainActionButton.addActionListener(e -> openFeeGUI(foundTicket)); // Open FeeGUI
            actionPanel.add(mainActionButton, actionGBC);
            
            // Override Gate Controls
            overrideButton = new JButton("🚨 OVERRIDE OPEN GATE (Fees Due)");
            overrideButton.setBackground(Color.RED.darker());
            overrideButton.setForeground(Color.WHITE);
            overrideButton.setOpaque(true);
            overrideButton.setBorderPainted(false);
            overrideButton.addActionListener(e -> promptForOverrideReason(foundTicket.getTicketID())); // Prompt for reason
            actionGBC.gridy = 1;
            actionPanel.add(overrideButton, actionGBC);
        }
        
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        detailsPanel.add(actionPanel, gbc);
		
		
		// Repaint window to show new components
		this.revalidate();
		this.repaint();
		this.pack();
	}
	
	public String getProcessedTicket() {
		return this.ticket;
	}
	
    // Open Fee GUI
    private void openFeeGUI(LocalTicket ticket) {
        // Create and show the FeeGUI dialog, passing the current ticket.
        FeeGUI newWindow = new FeeGUI(null, es);
        newWindow.setVisible(true);
    }
    
    // Prompt for Override Reason
    private void promptForOverrideReason(String ticketID) {
         String reason = JOptionPane.showInputDialog(this, "Enter reason for overriding the gate open (e.g., system error, lost ticket):", "Gate Override Reason", JOptionPane.WARNING_MESSAGE);
         
         if (reason != null && !reason.trim().isEmpty()) {
             openGate(ticketID, true, reason.trim());
         } else if (reason != null) {
             JOptionPane.showMessageDialog(this, "Override cancelled. A reason is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
         }
    }
    
    // Call Open Gate Task
    private void openGate(String ticketID, boolean isOverride) {
        openGate(ticketID, isOverride, isOverride ? "Gate Override" : "Ticket Paid");
    }
    
    private void openGate(String ticketID, boolean isOverride, String reason) {
        new OpenGateTask(ticketID, isOverride, reason).execute();
    }


	// New worker task to avoid blocking EDT
	private class TicketSearchTask extends SwingWorker<LocalTicket, Void> {
		private final String ticketID;
		
		public TicketSearchTask(String ticketID) {
			this.ticketID = ticketID;
			searchTicketButton.setEnabled(false); // Disable button while searching for ticket
		}
		
        @Override
		protected LocalTicket doInBackground() throws Exception {
			Message searchRequest = new Message("FIND TICKET", "REQUEST TICKET", ticketID);
			es.sendMessage(searchRequest);
			LocalTicket foundTicket = es.findTicket(ticketID);
			
			if (foundTicket != null) {
				return foundTicket;
			} else {
				throw new Exception("Ticket ID " + ticketID + " not found in system.");
			}
		}
		
		@Override
		protected void done() {
			searchTicketButton.setEnabled(true); // reactivate search button
			try {
				LocalTicket foundTicket = get();
				displayTicketDetails(foundTicket);
			} catch (Exception e) {
				String fullErrorMessage = e.getMessage();
				String prefixToRemove = "java.lang.Exception: ";
				String displayMessage;
				if (fullErrorMessage != null && fullErrorMessage.startsWith(prefixToRemove)) {
					displayMessage = fullErrorMessage.replace(prefixToRemove, "");
				} else if (fullErrorMessage != null) {
					displayMessage = fullErrorMessage;
				} else {
					displayMessage = "An unknown search error ocurred.";
				}
				JOptionPane.showMessageDialog(OpenGateGUI.this, displayMessage, "Search Error", JOptionPane.ERROR_MESSAGE);
				detailsPanel.removeAll();
				OpenGateGUI.this.revalidate();
				OpenGateGUI.this.repaint();
			}
		}
	}
	
    // OpenGateTask
	private class OpenGateTask extends SwingWorker<Boolean, Void> {
		private final String ticketID;
        private final boolean isOverride;
        private final String reason;
		
		public OpenGateTask(String ticketID, boolean isOverride, String reason) {
			this.ticketID = ticketID;
            this.isOverride = isOverride;
            this.reason = reason;
			searchTicketButton.setEnabled(false);
            if (mainActionButton != null) mainActionButton.setEnabled(false);
            if (overrideButton != null) overrideButton.setEnabled(false);
		}
		@Override
		protected Boolean doInBackground() throws Exception {
            System.out.printf("OpenGateGUI: Attempting to open gate for Ticket %s. Override: %s, Reason: %s%n", ticketID, isOverride, reason);

            // Call the server method
//			boolean success = es.openExitGate(ticketID, isOverride, reason);
            boolean success = es.openExitGate();
			boolean success = true;
			if (success) {
				return true;
			} else {
				throw new Exception("Gate operation failed or was denied by the server.");
			}
		}
		@Override
		protected void done() {
			searchTicketButton.setEnabled(true);
            if (mainActionButton != null) mainActionButton.setEnabled(true);
            if (overrideButton != null) overrideButton.setEnabled(true);
			try {
				if (get()) {
					JOptionPane.showMessageDialog(OpenGateGUI.this, "Exit Gate Opened successfully!", "Gate Control Success", JOptionPane.INFORMATION_MESSAGE);
					if (isOverride) {
						String overrideMessage = String.format("Gate was opened via manual override.\nReason recorded: %s" ,  reason);
						JOptionPane.showMessageDialog(OpenGateGUI.this, overrideMessage, "Override Confirmation", JOptionPane.WARNING_MESSAGE);
					}
				} else {
                    // This block will execute if doInBackground returns false (not using a thrown exception)
                    JOptionPane.showMessageDialog(OpenGateGUI.this, "Gate open command failed or was rejected.", "Gate Control Failure", JOptionPane.ERROR_MESSAGE);
                }
			} catch (Exception e) {
				String message = "Gate operation failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown network error.");
				JOptionPane.showMessageDialog(OpenGateGUI.this, message, "Gate Control Error", JOptionPane.ERROR_MESSAGE);
			}
            // Refresh the display to show the updated ticket status (e.g., exit time recorded)
            searchTicket(ticketID); 
		}
	}
}