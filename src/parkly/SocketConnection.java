package parkly;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SocketConnection implements Runnable {
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Message msg;
	private volatile Message loginResponse = null;
	private volatile String incomingTicket = null;
	private volatile String ticketList = null;
	private volatile String incomingPaymentInfo = null;
	private volatile List<Payment> approvedPayments = null;
	private String entryGateStatus;
	private String entryGateId;
	private String exitGateId;
	private Scanner sc;
	private String input;
	private String type;
	private String status;
	private String text;
	private volatile boolean running = true;
	private volatile Boolean releaseSuccess = null;
	
	
	// consider adding host and port number to parameters for constructor, not implemented for testing ATM.
	SocketConnection(String host, int port) throws IOException {
		System.out.println("SocketConnection: Creating SocketConnection.");
		try {
			this.socket = new Socket(host, port);
			if (socket != null) {
				System.out.println("SocketConnection: Created successful socket connection.");
			}
			this.sc = new Scanner(System.in);
			this.oos = new ObjectOutputStream(this.socket.getOutputStream());
			this.oos.flush();
			this.ois = new ObjectInputStream(this.socket.getInputStream());
//			this.type = "text";
			System.out.println("Successful creation of SocketConnection Object.");
		} catch (IOException e) {
//			e.printStackTrace();
			throw new IOException("Server response invalid.", e);
		}
	}
	
	// Listens to incoming messages from the server here
	@Override
	public void run() {
		try {
			System.out.println("SocketConnection: Success in run method()");
			// Listens for incoming messages
			while (running) {
				// Check what object we receive first
				Object receivedObject = ois.readObject();
				if (!(receivedObject instanceof ObjectTag)) {
					System.err.println("Protocol error: Received object cannot be identified.");
					continue;
				}
				ObjectTag taggedObject = (ObjectTag) receivedObject;
				String tag = taggedObject.getObjectTag();
				switch (tag) {
					case "MESSAGE": 
						this.msg = (Message) taggedObject;
						String type = this.msg.getType();
						String status = this.msg.getStatus();
						String text = this.msg.getText();
						
						// LOGIN 
						if (type.equalsIgnoreCase("LOGIN")) {
							this.loginResponse = this.msg;
							System.out.println("SocketConnection.run: Received LOGIN reponse: " + status);
						} 
						// LOGOUT
						else if (type.equalsIgnoreCase("LOGOUT") && status.equalsIgnoreCase("DISCONNECT")) {
							EmployeeGUI.appendServerMessage("Server: " + text);
						}
						// NEW TICKET 
						else if (type.equalsIgnoreCase("TICKET") && status.equalsIgnoreCase("SUCCESS")) {
							this.incomingTicket = text;
						}
						else if (type.equalsIgnoreCase("TICKET") && status.equalsIgnoreCase("FOUND")) {
							this.incomingTicket = text;
						}
						else if(type.equalsIgnoreCase("TICKET LIST") && status.equalsIgnoreCase("SUCCESS")) {
							this.ticketList = text;
						}
						// open gate
						else if (type.equalsIgnoreCase("GATE") && status.equalsIgnoreCase("SUCCESS") && text.equalsIgnoreCase("GATE OPEN")) {
							System.out.println("SocketConnection.run: Receieved Gate Open Message");
							this.entryGateStatus = "OPEN";
//										EmployeeGUI.appendServerMessage("Server: GATE " + this.entryGateId + " is OPEN.");
							setupGateCloseTimer(this.entryGateId);
						} 
						// close gate
						else if (type.equalsIgnoreCase("GATE") && status.equalsIgnoreCase("SUCCESS") && text.equalsIgnoreCase("GATE CLOSED")) {
							System.out.println("SocketConnection.run: Received Gate Closed Message");
							EmployeeGUI.appendServerMessage("Server: " + text);
							this.entryGateStatus = "CLOSED";
						}
						else if (type.equalsIgnoreCase("TICKET LIST") && status.equalsIgnoreCase("START")) {
							EmployeeGUI.appendServerMessage(type + ": " + status);
						}
						else if (type.equalsIgnoreCase("TICKET LIST") && status.equalsIgnoreCase("END")) {
//										EmployeeGUI.appendServerMessage(type + ": " + status + " | " + text);
						}
						else if (type.equalsIgnoreCase("PAYMENT INFO") && status.equalsIgnoreCase("SUCCESS")) {
							EmployeeGUI.appendServerMessage(text);
							this.incomingPaymentInfo = text;
						}
						else {
//										EmployeeGUI.appendServerMessage("Server: " + text + "\n");
						} break;
						
						
//					case "TICKET":
//						Ticket receivedTicket = (Ticket) taggedObject;
//						if (this.activeTickets != null) {
//							this.activeTickets.add(receivedTicket);
//							System.out.println("SocketConnection: TICKET added to temp list. Current size: " + this.activeTickets.size());
//						} else {
//							System.out.println("SocketConnection: Single TICKET received outside of request.");
//						}
////						this.incomingTicket = receivedTicket;
//						System.out.println("SocketConnection.run: TICKET OBJECT DETECTED: " + receivedTicket.getTicketID());
//						System.out.println("\tFOUND TICKET: " + receivedTicket.getTicketID());
//						System.out.println("\tTicket Data:\n\tENTRY TIME: " + receivedTicket.getEntryTime() + "\n\tEXIT TIME: " + 
//											receivedTicket.getExitTime() + "\n\tFEES DUE: " + receivedTicket.getTotalFees() + "\n\tPAID: " + receivedTicket.isPaid());
////									EmployeeGUI.appendServerMessage("\tTicket Data:\n\tENTRY TIME: " + receivedTicket.getEntryTime() + "\n\tEXIT TIME: " + 
////											receivedTicket.getExitTime() + "\n\tFEES DUE: " + receivedTicket.getTotalFees() + "\n\tPAID: " + receivedTicket.isPaid());
						
//						break;
					
//					case "PAYMENT":
//						Payment receivedPayment = (Payment) taggedObject;
//						if (this.approvedPayments != null) {
//							this.approvedPayments.add(receivedPayment);
//							System.out.println("SocketConnectio.run: PAYMENT added to temp list. Current size: " + this.approvedPayments.size());
//						} else {
//							System.out.println("SocketConnection: Single PAYMENT received outside of request.");
//						}
//						this.incomingPayment = receivedPayment;
//						System.out.println("13. SocketConnection.run: PAYMENT OBJECT DETECTED: " + receivedPayment.getPaymentID());
//						System.out.println("\tFOUND TICKET: " + receivedPayment.getPaymentID());
//						System.out.println("\tPayment Data:" + "\n\tEmployeeID: " + receivedPayment.getEmployeeID() + "\n\tGateID: " + receivedPayment.getGateID() + 
//											"\n\tPayment Date: " + receivedPayment.getPaymentDate() + "\n\tPayment TIME: " + 
//											receivedPayment.getPaymentTime() + "\n\tTicket ID: " + receivedPayment.getTicketID() + "\n\tAmmount: " + receivedPayment.getPaymentAmount());
//						break;
					default: 
						System.err.println("Unknown object tag received: " + tag);
						break;
				}
			}
		} catch (java.net.SocketException e) {
			if (!running) {
				System.out.println("SocketConnection: Expected socket shutdown detected.");
			} else {
//				e.printStackTrace();
				System.err.println("SocketConnection: Unexpected connection failure.");
			}
		} catch (EOFException e) {
			if (!running) {
				System.out.println("SocketConnection: Stream ended gracefullly (EOF).");
			} else {
				System.err.println("SocketConnection: Server closed connection unexpectedly.");
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    System.out.println("SocketConnection: Exiting run method. Final cleanup.");
		    
		    // Use a nested try-catch for cleanup to ensure all resources are attempted to be closed
		    try {
		        // 1. Close streams first (if they haven't been closed by socket.close())
		        if (this.oos != null) {
		            this.oos.close();
		        }
		        if (this.ois != null) {
		            this.ois.close();
		        }
		    } catch (IOException e) {
		        System.out.println("Warning: Error closing streams.");
		    }

		    // 2. Close the socket (The socket variable is already defined and checked outside)
		    if (socket != null && !socket.isClosed()) {
		        try {
		            socket.close();
		            System.out.println("SocketConnection: Socket closed properly, Goodbye!");
		        } catch (IOException e) {
		            System.out.println("Warning: Error closing socket.");
		        }
		    }
		}
	}
	
	public Message getMessage() {
		return this.msg;
	}
	
	public void sendMessage(Message msg) {
		if (oos == null || socket.isClosed()) {
			System.err.println("Connection is closed. Cannot send message.");
			return;
		}
		try {
			// Send message through function
			if (msg != null && !msg.getText().isEmpty()) {
				System.out.println("SocketConnection.sendMessage:\n\tSending msg: " + msg.getType() + " | " + msg.getStatus() + " | " + msg.getText());
				this.oos.reset();
				this.oos.writeObject(msg);
				this.oos.flush();
			}
			long startTime = System.currentTimeMillis();
			final long TIMEOUT_MS = 1000;
			while ((System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error sending message: " + e.getMessage());
		}
	}
	// String credentials = username + "|" + password + "|" + gateId;
    // Message loginMsg = new Message("LOGIN", "REQUEST", credentials);
	public Message sendLoginRequest(Message loginMsg) {
		this.loginResponse = null;
		sendMessage(loginMsg);
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 1000;
		while (this.loginResponse == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		Message response = this.loginResponse;
		this.loginResponse = null;
		return response;
	}
	
	public String generateTicket(String employeeID, String gateId) {
		System.out.println("SocketConnection.generateTicket: Passing message to sendMessage");
		sendMessage(new Message("TICKET", "NEW TICKET", employeeID + "|" + gateId));
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 3000;
		while (this.incomingTicket == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		String returnTicket =  this.incomingTicket;
		System.out.println("SC.generateTicket: " + returnTicket);
		this.incomingTicket = null;
		return returnTicket;
	}
	
	public String findTicket(String ticketID) {
		this.incomingTicket = null;
		sendMessage(new Message("FIND TICKET", "REQUEST TICKET", ticketID));
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 1000; // 5 seconds
		// wait until ticket is received OR timeout expires
		while (this.incomingTicket == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		// if we exit the lopp and incomingTicket is still null
		if (this.incomingTicket == null) {
			System.out.println("SocketConnection.incomingTicket:\n\tTimed out waiting for server response.");
		}
		
		String returnTicket = this.incomingTicket;
		this.incomingTicket = null;
		if (returnTicket != null) {
			System.out.println("SC:findTicke - TICKET FOUND - " + returnTicket);
		} else {
			System.out.println("SocketConnection.findTicket: \n\tTicket not found or timed out");
		}
		return returnTicket;
	}
	public String getActiveTickets() {
		this.ticketList = null;
		
		sendMessage(new Message("TICKET LIST", "REQUEST", "TICKETS"));
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 1000; // 1 seconds
		// wait until ticket is received OR timeout expires
		while (this.ticketList == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException ignored) {}
		
		String returnTickets = this.ticketList;
		this.ticketList = null;
		this.incomingTicket = null;
		if (returnTickets != null) {
			System.out.println("SocketConnection.getActiveTickets: success.");
		} else {
			System.out.println("SocketConnection.getActiveTickets: \n\tTickets not found or timed out");
		}
		return returnTickets;
	}
	
	public String makePayment(String payTicketID, String payType, double amount, String employeeID, String exitGate) {
		System.out.println("4. SC.MP");
		sendMessage(new Message("PAY TICKET", "MAKE PAYMENT", payTicketID + "|" + payType + "|" + amount + "|" + employeeID + "|" + exitGate));
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 10000;
		while (this.incomingTicket == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		System.out.println("14. SocketConnection.makePayment: \n\tPaidTicket: ");
		System.out.println("14. \n\tPayment: ");
		String returnPayment = this.incomingPaymentInfo;
		this.incomingPaymentInfo = null;
		System.out.println("Payment info: " + returnPayment);
		return returnPayment;
	}
	
	public boolean releaseTicket (String ticketID, String gateId) {
		this.releaseSuccess = null;
		sendMessage(new Message("RELEASE TICKET", "REQUEST", ticketID + " | " + gateId));
		long startTime = System.currentTimeMillis();
	    final long TIMEOUT_MS = 1000;
	    while (this.releaseSuccess == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
	        try {
	            Thread.sleep(50);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            return false;
	        }
	    }
	    
	    boolean result = (this.releaseSuccess != null && this.releaseSuccess.booleanValue());
	    this.releaseSuccess = null;
	    return result;
	}
	
	// Not currently in use
	public String openEntryGate(String entryGateId) {
		this.entryGateId = entryGateId;
		this.entryGateStatus = null;
		sendMessage(new Message("GATE", "OPEN REQUEST", entryGateId));
		
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 1000;
		while (this.entryGateStatus == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return "GATE FAILED TO OPEN";
			}
		}
		
		String gateStatus = this.entryGateStatus;
		this.entryGateStatus = null;
		return gateStatus;
	}
	
	private void setupGateCloseTimer(String gateId) {
		SwingUtilities.invokeLater(() -> {
			EmployeeGUI.appendServerMessage("Server: Gate " + gateId + " is now open. Closing in 5 seconds...");
		});
		
		int delay = 5000;
		Timer closeGateTimer = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Client Timer: 5 seconds elapsed. Sending CLOSE GATE request for " + gateId);
				EmployeeGUI.appendServerMessage("Client: Sent request to close Gate " + gateId);
				closeEntryGate(new Message("GATE", "CLOSE REQUEST", gateId));
			}
		});
		closeGateTimer.setRepeats(false);
		closeGateTimer.start();
	}
	
	public void closeEntryGate(Message closeGateMsg) {
		this.entryGateId = closeGateMsg.getText();
		this.entryGateStatus = null;
		sendMessage(closeGateMsg);
		long startTime = System.currentTimeMillis();
		final long TIMEOUT_MS = 1000;
		while (this.entryGateStatus == null && (System.currentTimeMillis() - startTime < TIMEOUT_MS)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.out.println("Failed to close gate.");
			}
		}
	}
	public String openExitGate(String gateId) {
		sendMessage(new Message("OPEN GATE", "SUCCESS", gateId));
		return "Could not open gate";
	}

	public void logout() {
	    // 1. Send logout request
		System.out.println("SocketConnection.logout: Sending LOGOUT request.");
	    sendMessage(new Message("LOGOUT", "LOGOUT", "LOGOUT"));
	    this.running = false;
	    if (socket != null && !socket.isClosed()) {
	    	try {
	    		socket.close();
	    		System.out.println("SocketConnection.logout: Socket closed forcefully to interrupt run() thread.");
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		System.out.println("SocketConnection.logout: Error closing socket.");
	    	}
	    }
	}
}


