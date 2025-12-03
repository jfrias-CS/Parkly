package parkly;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter; // Import if you need to display formatted dates

public class FeeGUI extends JDialog {
    // --- Components ---
	private final JPanel mainPanel;
	private final EmployeeService es;
	private final JTextField searchTicketText;
	private final JButton searchTicketButton;
	private final JPanel detailsPanel;
	private JTextArea ticketDetailsArea; // Changed from Label to Area for scroll and multi-line
	private JLabel feeLabel;
	private JTextField paymentAmountText;
	private JComboBox<String> payTypeCombo;
	private JButton payButton;
	private String ticket; // For Finding and returning the result
	
    // --- Modernization: Look and Feel setup (Optional, call once at application start) ---
    public static void setLookAndFeel() {
        try {
            // Use the Nimbus L&F for a modern look
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, fall back to default
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Handle exception
            }
        }
    }

	public FeeGUI(JFrame owner, EmployeeService es) {
		super(owner, "Pay Parking Fees", ModalityType.APPLICATION_MODAL);
		this.es = es;
        // 1. Modernize Initial Setup
        // setLookAndFeel(); // Call this once in your main application entry point

		this.setSize(600, 450); // Increase size for better spacing
		this.setLocationRelativeTo(owner);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		mainPanel = new JPanel(new BorderLayout(15, 15)); // Use BorderLayout for main structure
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Add padding

        // --- Top Search Panel ---
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Use FlowLayout for alignment
        JLabel searchTicketLabel = new JLabel("💳 Enter Ticket ID:");
		searchTicketText = new JTextField(15);
		searchTicketButton = new JButton("🔍 Search Ticket");
        
		searchPanel.add(searchTicketLabel);
		searchPanel.add(searchTicketText);
		searchPanel.add(searchTicketButton);
		
		mainPanel.add(searchPanel, BorderLayout.NORTH);
		
		// --- Center Details Panel (Dynamically populated) ---
		detailsPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for flexible internal layout
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Ticket Information & Payment"));
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
		
		this.getContentPane().add(mainPanel);
		this.pack();
		
		// --- Action Listener (Modernized with Lambda) ---
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

	private void displayTicketDetails(LocalTicket foundTicket) {
		detailsPanel.removeAll();
        // Use GridBagConstraints for organized, modern layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding around components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
		System.out.println("TICKET RECEIVED: " + foundTicket);
		
		// 1. Ticket Details Section (Using a JTextArea inside a JScrollPane)
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
        
        ticketDetailsArea = new JTextArea(detailsText, 5, 30); // 5 rows, 30 columns
        ticketDetailsArea.setEditable(false);
        ticketDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ticketDetailsArea.setBackground(detailsPanel.getBackground()); // Match background for cleaner look
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span two columns
        JScrollPane scrollPane = new JScrollPane(ticketDetailsArea);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        detailsPanel.add(scrollPane, gbc);
        
		// 2. Fees Due Label
        boolean isTicketPaid = foundTicket.isPaid();
		int fee = foundTicket.getTotalFees();
		double feeDouble = (double) fee;
		if (isTicketPaid) {
			feeLabel = new JLabel("✅ TICKET ALREADY PAID!");
			feeLabel.setForeground(Color.green.darker());
		} else {
			feeLabel = new JLabel("💰 Total Fees Due: $" + String.format("%.2f", feeDouble));
		    feeLabel.setForeground(new Color(34, 139, 34));
		}
//        feeLabel = new JLabel("💰 Total Fees Due: $" + String.format("%.2f", feeDouble));
        feeLabel.setFont(new Font("SansSerif", Font.BOLD, 18)); // Make fee stand out
        feeLabel.setForeground(new Color(34, 139, 34)); // Dark green color
        
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
//        detailsPanel.add(feeLabel);
        detailsPanel.add(feeLabel, gbc);
        
		// 3. Payment Input Fields
		JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		
        // Amount Paid Field
        paymentPanel.add(new JLabel("Amount Paid:"));
		paymentAmountText = new JTextField(String.format("%.2f", feeDouble), 8);
		paymentAmountText.setHorizontalAlignment(JTextField.RIGHT);
		paymentPanel.add(paymentAmountText);
		
        // Pay Type Combo Box
		paymentPanel.add(new JLabel("Payment Method:"));
		payTypeCombo = new JComboBox<>(new String[] {"Cash", "Credit/Debit Card", "Mobile Payment"}); // More descriptive options
		payTypeCombo.setSelectedItem("Cash");
		paymentPanel.add(payTypeCombo);
		
        gbc.gridy = 2;
        detailsPanel.add(paymentPanel, gbc);
		
		// 4. Payment button (Use a new look for the button)
		payButton = new JButton("✅ Process Payment");
        payButton.setBackground(new Color(0, 123, 255)); // Blue background
        payButton.setForeground(Color.BLACK);
        payButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        // Pay ticket
		payButton.addActionListener(e -> processPayment(foundTicket));
		paymentAmountText.setEnabled(!isTicketPaid);
		payTypeCombo.setEnabled(!isTicketPaid);
		payButton.setEnabled(!isTicketPaid);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        detailsPanel.add(payButton, gbc);
		
		
		// Repaint window to show new components
		this.revalidate();
		this.repaint();
		this.pack();
	}
	
	public String getProcessedTicket() {
		return this.ticket;
	}
	
	private void processPayment(LocalTicket paidTicket) {
		// Finalized payment logic
		String payType = (String) payTypeCombo.getSelectedItem();
		double amount = 0.0;
        try {
            amount = Double.parseDouble(paymentAmountText.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
		
        payButton.setEnabled(false);
        new PayTicketTask(paidTicket, payType, amount).execute();
		JOptionPane.showMessageDialog(this, String.format("Payment of $%.2f successful via %s!", amount, payType), "Payment Success", JOptionPane.INFORMATION_MESSAGE);
		this.dispose();
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
				System.out.println("FeeGUI.done: received ticket: " + foundTicket);
				displayTicketDetails(foundTicket);
				
				if (foundTicket.isPaid()) {
					JOptionPane.showMessageDialog(FeeGUI.this, "Ticket" + foundTicket.getTicketID() + " had already been paid.", "Status Check", JOptionPane.WARNING_MESSAGE);
				}
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
				JOptionPane.showMessageDialog(FeeGUI.this, displayMessage, "Search Error", JOptionPane.ERROR_MESSAGE);
				detailsPanel.removeAll();
				FeeGUI.this.revalidate();
				FeeGUI.this.repaint();
			}
		}
	}
	
	private class PayTicketTask extends SwingWorker<Boolean, Void> {
		private final String payTicket;
		private final String payType;
		private final double amount;
		
		public PayTicketTask(LocalTicket ticket, String payType, double amountPaid) {
			this.payTicket = ticket.getTicketID();
			this.payType = payType;
			this.amount = amountPaid;
			searchTicketButton.setEnabled(false);
		}
		@Override
		protected Boolean doInBackground() throws Exception {
			if (payTicket == null) {
				throw new Exception("Ticket not found in system.");
			}
			System.out.println("1. FEEGUI: START OF PAYMENT PROCESS");
			LocalPayment ticketPaidSuccessfully = es.makePayment(payTicket, payType, amount);
			Thread.sleep(200);
//			System.out.println("Ticket ID: " + ticket.getTicketID() + "| Ticket paid: " + ticket.isPaid());
			System.out.println(ticketPaidSuccessfully);
			System.out.println("17. PAYMENT RETURNED");
			if (ticketPaidSuccessfully != null) {
				return true;
			} else {
				return false;
//				throw new Exception("Ticket was not processed.");
			}
		}
		@Override
		protected void done() {
			searchTicketButton.setEnabled(true);
			payButton.setEnabled(true);
			try {
				if (get()) {
					ticket = payTicket;
					JOptionPane.showMessageDialog(FeeGUI.this, String.format("Payment of $%.2f successful via %s!", amount, payType), "Payment Success", JOptionPane.INFORMATION_MESSAGE);
					FeeGUI.this.dispose();
				}
			} catch (Exception e) {
				String message = "Payment failed due to a network or server error.";
				JOptionPane.showMessageDialog(FeeGUI.this, message, "Payment Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}
}