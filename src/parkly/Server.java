package parkly;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Server {
	private static int connectionCount; // keep track of how many connection are present 

//	private static List<Ticket> activeTickets = new ArrayList<>();
	private static synchronized void increment() { connectionCount++; };
	public static void main(String[] args) {
		
		ServerSocket server = null;
		connectionCount = 0;
		try {
			// server is listening on port 1235
			server = new ServerSocket(9000);
			server.setReuseAddress(true);
			
			// run infinite loop for getting client request
			while (true) { 
				System.out.println("Server started. Waiting for incoming connections...");
				Socket employee = server.accept(); // accept incoming requests from employee computer
				increment(); // increment employee count
				System.out.println("New application connected: ID(" + connectionCount + ") " + employee.getInetAddress().getHostAddress()); // for testing
				
				EmployeeHandler employeeSocket = new EmployeeHandler(connectionCount, employee); // create handler for newly connected employee
				
				new Thread(employeeSocket).start(); // hand off employee handler to new thread for multi-threading
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					System.out.println("Server shutting down.");
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static class EmployeeHandler implements Runnable {
		private final int connectionID; // keep track of this EmployeHandler's employee id
		private final Socket employeeSocket; // for socket manipulation
		private ObjectOutputStream oos = null;
		private ObjectInputStream ois = null;
		private boolean keepRunning = true;
		private Gate entryGate;
		private Gate exitGate;
		private String employeeId = null;
		// Singleton Services
		private final TicketService ticketService = TicketService.getInstance();
		private final PaymentService paymentService = PaymentService.getInstance();
		private final SpaceTracker spaceTracker = SpaceTracker.getInstance(10);
		// constructor
		public EmployeeHandler(int connectionCount, Socket socket) {
			this.connectionID = connectionCount;
			this.employeeSocket = socket;
		}
		
		public void run() {
			try {
				// Start object output and input streams
				this.oos = new ObjectOutputStream(this.employeeSocket.getOutputStream());
				this.ois = new ObjectInputStream(this.employeeSocket.getInputStream());
				System.out.println("Socket connected. Waiting for LOGIN request");
				// Get LOGIN VALIDATION:
				//  String credentials = username + "|" + password + "|" + gateId;
			    // Message loginMsg = new Message("LOGIN", "REQUEST", credentials);
				//					= new Message(type, status, test);
				if (!handleLoginAndAuthentication(this.oos, this.ois)) {
					return;
				}
				
				// Loop to listen for incoming messages from client
				while (keepRunning) {
					// Listening while loop for any new incoming objects
					Object receivedObject = this.ois.readObject();
					
					// Validate we received a tagged object to process
					if(!(receivedObject instanceof ObjectTag)) {
						System.err.println("Protocol error: Received object cannot be identified.");
						oos.writeObject(new Message("error", "protocol", "Unknown object type received."));
						continue;
					}
					// Receive taggedObject
					ObjectTag taggedObject = (ObjectTag) receivedObject;
					// Retrieve Tag identifier
					String tag = taggedObject.getObjectTag();
					
					switch (tag) {
						case "MESSAGE":
							Message msg = (Message) taggedObject;
							handleMessageRequest(msg);
							break;
							
							
						case "TICKET":
							Ticket newTicket = (Ticket) taggedObject;
							
							System.out.println("Successfully received ticket request.");
							break;
							
						case "PAYMENT": 
//							oos.writeObject(new Message("PAYMENT", "PAYMENT SUCCESS", "PAYMENT CONNECTED."));
//							Payment newPayment = (Payment) 
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Employee " + employeeId + " disconnected: " + e.getMessage());
			} catch (ClassNotFoundException c) {
				c.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					System.out.println("Closing socket for client: ID(" + this.employeeId + ") ");
					try {
						if (oos != null) { 
							this.oos.close();
						}
						if (ois != null) {
							this.ois.close();
						}
					} catch (IOException e) {
						System.err.println("Error closing streams: " + e.getMessage());
					}
					employeeSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private boolean handleLoginAndAuthentication(ObjectOutputStream oos, ObjectInputStream ois) throws IOException, ClassNotFoundException {
			while (this.employeeId == null) {
				// 1. Read the next object from the client
	            Object receivedObject = ois.readObject();
	            if (!(receivedObject instanceof Message)) {
	                oos.writeObject(new Message("LOGIN", "FAILURE", "Protocol error: Not a Message."));
	                oos.flush();
	                continue; // Wait for the next object
	            }
	            Message loginMessage = (Message) receivedObject;
	            if (loginMessage.getType().equalsIgnoreCase("LOGIN") && loginMessage.getStatus().equalsIgnoreCase("REQUEST")) {
	                
	                String text = loginMessage.getText();
	                String[] parts = text.split("\\|");

	                // 2. Check message format
	                if (parts.length != 3) {
	                    System.out.println("Server.LoginHandler: Login FAILED - Invalid format: " + text);
	                    oos.writeObject(new Message("LOGIN", "FAILURE", "Invalid login format."));
	                    oos.flush();
	                    continue; // Stay in loop, wait for retry
	                }

	                String username = parts[0];
	                String password = parts[1];
	                String entryGateId = parts[2];
	                // 3. Verify Credentials and Gate ID
	                boolean credentialsValid = AuthenticationService.verifyCredentials(username, password);
	                boolean gateValid = AuthenticationService.verifyGateId(entryGateId);
	                String exitGateId = AuthenticationService.getExitGateId(entryGateId);
	                if (credentialsValid && gateValid) {
	                    // SUCCESS: Set context and BREAK LOOP
	                    this.employeeId = username;
	                    this.entryGate = new Gate(entryGateId + "_ENTRY");
	                    this.exitGate = new Gate(exitGateId + "_EXIT");
	                    
	                    System.out.println("Server.LoginHandler: Employee [" + username + "] successfully logged in.");
	                    
	                    // Send SUCCESS confirmation back to client
	                    oos.writeObject(new Message("LOGIN", "SUCCESS", exitGateId));
	                    oos.flush();
	                    // Loop terminates automatically since employeeId is now set
	                    
	                } else {
	                    // FAILURE: Send response and stay in loop
	                    System.out.println("Server.LoginHandler: Login FAILED - Credentials or Gate Invalid for user: " + username);
	                    oos.writeObject(new Message("LOGIN", "FAILURE", "Invalid credentials or gate ID."));
	                    oos.flush();
	                    // Loop continues, waiting for the client's next attempt
	                }
	                
	            } else if (loginMessage.getType().equalsIgnoreCase("DISCONNECT") || loginMessage.getType().equalsIgnoreCase("LOGOUT")) {
	                // Handle client manually closing the login window
	                return false; // Exit the run method, letting finally close the socket
	            } else {
	                // Non-login message received before authentication
	                oos.writeObject(new Message("LOGIN", "FAILURE", "Unauthenticated request."));
	                oos.flush();
	                // Stay in loop
	            }
			}
			return this.employeeId != null;
		}
		
		private void handleMessageRequest(Message request) throws IOException, InterruptedException {
			String type = request.getType().toUpperCase().trim();
			String status = request.getStatus().toUpperCase().trim();
			String text = request.getText().toUpperCase().trim();
			
			Message messageResponse = null;
			Ticket ticketFound = null;
			Ticket ticketToPay = null;
			Payment paymentResponse = null;
			
			System.out.println("Server.run (MESSAGE): Successfully received message. \n\tTYPE: " + type + "\n\tSTATUS: " + status + "\n\tTEXT: " + text);
			
			if (text.equalsIgnoreCase("text") && status.equalsIgnoreCase("success")) {
			    this.oos.reset();
			    this.oos.writeObject(new Message(type, status, text.toUpperCase().trim()));
			    this.oos.flush();
			    // Logout 
			} 
			
			else if (type.equalsIgnoreCase("LOGOUT") && text.equalsIgnoreCase("LOGOUT")){
			    status = "logout";
			    System.out.println("Server.run (LOGOUT):\n\tLogout received: " + text);
			    this.oos.reset();
			    this.oos.writeObject(new Message("LOGOUT", "DISCONNECT", "CONNECTION CLOSED!"));
			    this.oos.flush();	 
			    this.keepRunning = false;
			} 
			
			// NEW TICKET: "ticket" "new ticket" gateId
			else if (type.equalsIgnoreCase("TICKET") && status.equalsIgnoreCase("NEW TICKET")) {
				String returnTicket;
				if (spaceTracker.isFull()) {
					this.oos.reset();
					this.oos.writeObject(new Message("TICKET", "FAILURE", "ERROR: MAX CAPACITY REACHED"));
				} else {
					// No issues
					returnTicket = ticketService.generateNewTicket(text);
					spaceTracker.increment();
					System.out.println("Returning ticket from server\n" + returnTicket);
					this.oos.reset();
					this.oos.writeObject(new Message("TICKET", "SUCCESS", returnTicket));
					this.oos.flush();
				}
			} 
			
			// FIND TICKET
			else if (type.equalsIgnoreCase("FIND TICKET") && status.equalsIgnoreCase("REQUEST TICKET")) {
			    System.out.println("Server.run (FIND TICKET): Searching for ticket: " + text);
			    String ticketID = text;
			    String returnTicket = ticketService.findTicketAsString(ticketID); // returns string method
			    if (returnTicket != null) { 
			            this.oos.reset();
			            this.oos.writeObject(new Message("TICKET", "FOUND", returnTicket));
			            this.oos.flush();
			    } else {
			        this.oos.reset();
			        this.oos.writeObject(new Message("ERROR", "FAILURE", "Ticket Not Found."));
			        this.oos.flush();
			    }
			} 
			// REQUEST TICKET LIST
			else if (type.equalsIgnoreCase("TICKET LIST") && status.equalsIgnoreCase("REQUEST") && text.equalsIgnoreCase("TICKETS")) {
			    System.out.println("Server.run (RETURN ACTIVE TICKETS): Returning tickets");
//			    sendTicketList(ticketService.getActiveTickets());
			    this.oos.reset();
			    this.oos.writeObject(new Message("TICKET LIST", "SUCCESS", ticketService.getActiveTickets()));
			    this.oos.flush();
			}

			// MAKE PAYMENT
			else if (type.equalsIgnoreCase("PAY TICKET") && status.equalsIgnoreCase("MAKE PAYMENT")) {
				System.out.println("5. SERVER: Attempting to Pay ticket.");
				if (text.isEmpty()) {
					this.oos.reset();
					this.oos.writeObject(new Message("ERROR", "FAILURE", "TICKET NOT PAID."));
					this.oos.flush();
				}
				String[] parts = text.split("\\|");
				if (parts.length != 5 || parts == null) {
					this.oos.reset();
					this.oos.writeObject(new Message("ERROR", "FAILURE", "TICKET NOT PAID."));
					this.oos.flush();
				}
				
				String ticketID = parts[0];
				String payType = parts[1];
				String amountOwed = parts[2];
				String employeeID = parts[3];
				String exitGate = parts[4];
				System.out.println("6. SERVER: PASSING ID TO PS.RP: " + ticketID);
			    
			    String returnPayment = paymentService.recordPayment(ticketID, payType, amountOwed, employeeID, exitGate);
			    spaceTracker.decrement();
			    if (returnPayment == null) {
			    	this.oos.reset();
					this.oos.writeObject(new Message("ERROR", "FAILURE", "TICKET NOT PAID."));
					this.oos.flush();
			    }
//			    System.out.println("12. SERVER: Returning payment " + returnPayment.getPaymentID());
			    this.oos.reset();
//			    this.oos.writeObject(returnPayment);
			    this.oos.writeObject(new Message("PAYMENT INFO", "SUCCESS", returnPayment));
			    this.oos.flush();
			    
			} 
			// GET PAYMENTS
			else if (type.equalsIgnoreCase("PAYMENT LIST") && status.equalsIgnoreCase("REQUEST") && text.equalsIgnoreCase("PAYMENTS")) {
				System.out.println("SERVER: Making payment list");
				this.oos.reset();
				this.oos.writeObject(new Message("PAYMENT LIST", "SUCCESS", paymentService.getPayments()));
				this.oos.flush();
			}
			// Open Employees entry gate
			// "GATE", "OPEN REQUEST", "G1_ENTRY
			else if (type.equalsIgnoreCase("ENTRY GATE") && status.equalsIgnoreCase("OPEN REQUEST")) {
			    if (text.equalsIgnoreCase(this.entryGate.getGateId())) {
			        System.out.println("OPENING GATE: " + this.entryGate.getGateId());
			        this.entryGate.open();
			        this.oos.reset();
			        this.oos.writeObject(new Message("ENTRY GATE", "SUCCESS", "GATE OPEN"));
			        this.oos.flush();
			    } 
//							System.out.println("Server.run (OPEN ENTRY GATE):\n\tOpening front gate...");
			    
			} 

			// Close Employees entry gate
			else if (type.equalsIgnoreCase("ENTRY GATE") && status.equalsIgnoreCase("CLOSE REQUEST")) {
			    if (text.equalsIgnoreCase(this.entryGate.getGateId())) {
			        System.out.println("CLOSING GATE: " + this.entryGate.getGateId());
			        this.entryGate.close();
			        this.oos.reset();
			        this.oos.writeObject(new Message("ENTRY GATE", "SUCCESS", "GATE CLOSED"));
			        this.oos.flush();
			    }
			}
			
			// Open Employees exit gate
			else if (type.equalsIgnoreCase("EXIT GATE") && status.equalsIgnoreCase("OPEN REQUEST")) {
				if (text.equalsIgnoreCase(this.exitGate.getGateId())) {
					System.out.println("OPENING GATE: " + this.exitGate.getGateId());
					this.exitGate.close();
					this.oos.reset();
					this.oos.writeObject(new Message("EXIT GATE", "SUCCESS", "GATE OPEN"));
					this.oos.flush();
				}
			}
			
			// Close Employees exit gate
			else if (type.equalsIgnoreCase("EXIT GATE") && status.equalsIgnoreCase("CLOSE REQUEST")) {
				if (text.equalsIgnoreCase(this.exitGate.getGateId())) {
					System.out.println("CLOSING GATE: " + this.exitGate.getGateId());
					this.exitGate.close();
					this.oos.reset();
					this.oos.writeObject(new Message("EXIT GATE", "SUCCESS", "GATE CLOSED"));
					this.oos.flush();
				}
			}

			else {
			    System.out.println("Could not Process message message:");
			    System.out.println("\n\tTYPE: " + type + "\n\tSTATUS: " + status + "\n\tTEXT: " + text);
			    this.oos.writeObject(new Message("text", "success", "Failed."));
			    this.oos.flush();
			}
			
		}
		
	}
}
