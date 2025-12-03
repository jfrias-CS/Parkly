package parkly;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Facade class
public class SocketConnectionService {
	private static SocketConnection socket = null;
	
	// Connection logic encapsulation
	public static SocketConnection connect(String host, int port) throws IOException {
		System.out.println("SCS: Attempting to connect...");
		// Constructor call to create connection
		socket = new SocketConnection(host, port);
		System.out.println("SCS: Connection successful.");
		return socket;
	}
	
	// GUI uses this to send data
	public static void sendMessage(Message msg) {
		if (socket != null) {
			System.out.println("SCS.sendMessage: Sending msg:\n\t" + msg.getType() + "\n\t" + msg.getStatus() + "\n\t" + msg.getText());
			socket.sendMessage(msg);
		} else {
			System.err.println("SCS.sendMessage: Not connected.");
		}
	}
	
	public static Message getMessage() {
		if (socket != null) {
			System.out.println("SCS.getMessage: Requesting message");
			return socket.getMessage();
		}
		return null;
	}
//  String credentials = username + "|" + password + "|" + entranceId + "|" + exitId;
    // Message loginMsg = new Message("LOGIN", "REQUEST", credentials);
	public static Message sendLoginRequest(Message loginMsg) {
		if (socket != null) {
			System.out.println("SCS.sendLoginRequest: Sending credentials to server.");
			return socket.sendLoginRequest(loginMsg);
		}
		System.err.println("SCS.sendLoginRequest: Not connected.");
		return new Message("LOGIN", "ERROR", "Not connected.");
	}
	
	
	public static String generateTicket(String employeeID, String gateID) {
		if (socket != null) {
			System.out.println("SCS.generateTicket: Creating new ticket...");
			return socket.generateTicket(employeeID, gateID);
		} else {
			System.err.println("SCS.generateTicket: Not connected.");
		}
		return null;
	}
	
	public static String findTicket(String ticketID) {
		if (socket != null) {
			System.out.println("SCS.findTicket: Finding ticket...");
			String returnedTicket = socket.findTicket(ticketID);
			if (returnedTicket != null) {
				System.out.println("SCS.findTicket: Returning ticket:\n" + returnedTicket);				
			} else {
				System.out.println("SCS.findTicket: Ticket not found (returning null).");
			}
			return returnedTicket;
		} else {
			System.err.println("SCS.findTicket: Not connected.");
		}
		return null;
	}
	
	public static String getActiveTickets() {
		if (socket != null) {
//			System.out.println("SCS.getActiveTickets: Collecting tickets...");
			String returnedTickets = socket.getActiveTickets();
			if (returnedTickets != null) {
				System.out.println("SCS.getActiveTickets: Returning tickets...");				
			} else {
				System.out.println("SCS.getActiveTickets: Tickets not found (returning null).");
			}
			return returnedTickets;
		} else {
			System.err.println("SCS.getActiveTickets: Not connected.");
		}
		return null;
	}
	
	
	
	public static String makePayment(String payTicketID, String payType, double amount, String employeeID, String exitGate) {
		if (socket != null) {
			System.out.println("3. SCS.MAKEPAYMENT");
			String returnedPayment = socket.makePayment(payTicketID, payType, amount, employeeID, exitGate);
			if (returnedPayment != null) {
				System.out.println("15. SCS.MAKEPAYMENT: Returned payment " + returnedPayment);
			} else {
				System.out.println("SCS.makePayment: Ticket not found (returning false).");
			}
			return returnedPayment;
		} else {
			System.err.println("SCS.makePayment: Not connected.");
			return null;
		}
	}
	
	public static String getPayments() {
		if (socket != null) {
			System.out.println("SCS.getPayments: Getting payments...");
			
			String returnedPayments = socket.getPayments();
			if (returnedPayments != null) {
				System.out.println("SCS.getPayments: Returning payments...");
			} else {
				System.out.println("SCS.getPayments: Payments not found...");
				return null;
			}
			return returnedPayments;
		} else {
			System.err.println("SCS.getPayments: Not connected.");
			return null;
		}
	}
	
	public static String openEntryGate(String entryGateId) {
		if (socket != null) {
			System.out.println("SCS.openEntryGate: Sending open entry gate message...");
			return socket.openEntryGate(entryGateId);
		} else {
			System.err.println("SCS.openEntryGate: Not connected.");
			return "SCS: Not connected.";
		}
	}
	
	public static String closeEntryGate(String entryGateId) {
		if (socket != null) {
			System.out.println("SCS.openEntryGate: Sending open entry gate message...");
			return socket.openEntryGate(entryGateId);
		} else {
			System.err.println("SCS.openEntryGate: Not connected.");
			return "SCS: Not connected.";
		}
	}
	
	public static String openExitGate(String gateId) {
		if (socket != null) {
			System.out.println("SCS.openEntryGate: Sending open entry gate message...");
			String returnStatus = socket.openExitGate(gateId);
			System.out.println("SCS.openExitGate: " + returnStatus);
			return returnStatus;
		} else {
			System.err.println("SCS.openEntryGate: Not connected.");
			return "SCS: Not connected.";
		}
	}
	
	public static String closeExitGate(String entryGateId) {
		if (socket != null) {
			System.out.println("SCS.openEntryGate: Sending open entry gate message...");
			return socket.openEntryGate(entryGateId);
		} else {
			System.err.println("SCS.openEntryGate: Not connected.");
			return "SCS: Not connected.";
		}
	}
	
	public static boolean releaseTicket(String ticketID, String gateId) { // NOTE: NEW METHOD
	    if (socket != null) {
	        System.out.println("SCS.releaseTicket: Releasing ticket " + ticketID + " at Gate " + gateId);
	        return socket.releaseTicket(ticketID, gateId);
	    } else {
	        System.err.println("SCS.releaseTicket: Not connected.");
	        return false;
	    }
	}
	
	public static void disconnect() {
		if (socket != null) {
			System.out.println("SCS.disconnect: Initiating client disconnection.");
			
			socket.logout();
			socket = null;
			System.out.println("SCS.disconnect: Connection fully closed.");
		}
	}
}
