package parkly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// singleton
public class TicketService {
	// --- CONSTANTS ---
	private static final String TICKET_DELIMITER = "\n"; // Unique separator between tickets
	
	private static int nextTicketID;
	private static synchronized int getNextTicketID() {return nextTicketID++;};
	private static final TicketService instance = new TicketService();
	
	private final static Map<String, Ticket> allTickets = new ConcurrentHashMap<>(); // To generate all reports
	private final static Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();
	private final static List<Ticket> paidTickets = new ArrayList<>();
	
	private TicketService() {
		nextTicketID = 500;
	}
	public static TicketService getInstance() {
		return instance;
	}
	
	// ------------------------------------------------------------------
	// REFRACTORED: getActiveTickets()
	// Returns a single long string containing all active Ticket data, separated by TICKET_DELIMITER.
	// ------------------------------------------------------------------
	// ------------------------------------------------------------------
	// FIXED: getActiveTickets() - Uses pre-pending delimiter logic
	// ------------------------------------------------------------------
	public String getActiveTickets() {
	    StringBuilder allTicketsString = new StringBuilder();
	    boolean first = true; // Flag to track the first iteration
	    
	    // Iterate over the values (Ticket objects) in the thread-safe map
	    for (Ticket ticket : activeTickets.values()) {
	        
	        // 1. Prepend the delimiter only if it's NOT the first ticket
	        if (!first) {
	            allTicketsString.append(TICKET_DELIMITER); // Appends "|\n"
	        }
	        
	        // 2. Append the string representation of the current ticket
	        allTicketsString.append(toString(ticket));
	        
	        first = false; // Set flag to false for all subsequent iterations
	    }
	    
	    return allTicketsString.toString();
	}
	
	// generateNewTicket remains the same, as it calls toString().
	public String generateNewTicket(String ticketData) {
		if (ticketData != null) {
			String[] parts = ticketData.split("\\|");
			String employeeID = parts[0];
			String gateID = parts[1];
			int newTicketID = getNextTicketID();
			Ticket newTicket = new Ticket(newTicketID, employeeID, gateID);
			
			activeTickets.put(String.valueOf(newTicketID), newTicket);
			allTickets.put(String.valueOf(newTicketID), newTicket);
			String returnTicket = toString(newTicket);
			System.out.println("TS.generateNewTicket: New ticket generated: \n" + returnTicket);
			return returnTicket;
		}
		return "ERROR: GENERATING NEW STRING";
	}
	
	private void sortPaidList() {
		synchronized (paidTickets) {
			Collections.sort(paidTickets, new Comparator<Ticket>() {
				public int compare(Ticket t1, Ticket t2) {
					return t1.getTicketID().compareTo(t2.getTicketID());
				}
			});
		}
	}
	
	// findTicketAsString() remains the same, as it calls toString().
	public String findTicketAsString(String ticketID) {
		Ticket ticket = allTickets.get(ticketID); 
		
		if (ticket != null) {
			ticket.setExitStamp();
			return toString(ticket);
		}
		return null;
	}
	
	public Ticket findTicket(String ticketID) {
		Ticket ticket = activeTickets.get(ticketID);
		if (ticket != null) {
			ticket.setExitStamp();
			return ticket;
		}
		return null;
	}
	
	public void setExitStamp (String ticketID) {
		Ticket foundTicket = findTicket(ticketID);
		if (foundTicket != null && !foundTicket.isPaid()) {
			foundTicket.setExitStamp();
		}
	}
	
	public boolean markTicketPaid (String ticketID) {
		Ticket foundTicket = this.findTicket(ticketID);
		if (foundTicket == null) {
			return false;
		}
		foundTicket.markPaid();
		if (foundTicket.isPaid()) {
			System.out.println("9. TS.MTP: Marked ticket " + foundTicket.getTicketID() + " paid: " + foundTicket.isPaid());
			removeTicketFromList(foundTicket);
			return true;
		}
		return false;
	}
	
	private void removeTicketFromList (Ticket ticket) {
		synchronized(paidTickets) {
			paidTickets.add(ticket);
			sortPaidList();
		}
		activeTickets.remove(ticket.getTicketID());
		
		System.out.println("10. Added ticket to paid ticket and removed it form active tickets.");
	}
	
	// toString() remains the same.
	public String toString(Ticket ticket) {
		System.out.println("toString Called.");
		String result;
		if (ticket == null) {
			return "ERROR MAKING TICKET TO STRING";
		}
		
		// The string format is determined by whether the ticket is paid.
		if (!ticket.isPaid()) {
			// Unpaid String: 8 fields
			// ID | EMP | G | E_DATE | E_TIME | TOTAL_TIME | TOTAL_FEES | IS_PAID (false)
			result = ticket.getTicketID() + "|" + ticket.getEmployeeID() + "|" + ticket.getGateID() +  "|" + ticket.getEntryDate() + "|" + ticket.getEntryTime() + "|" + ticket.getTotalTime() + "|" + ticket.getTotalFees() + "|" + ticket.isPaid();
		} else {
			// Paid String: 10 fields
			// ID | EMP | G | E_DATE | E_TIME | X_DATE | X_TIME | TOTAL_TIME | TOTAL_FEES | IS_PAID (true)
			result = ticket.getTicketID() + "|" + ticket.getEmployeeID() + "|" + ticket.getGateID() + "|" +ticket.getEntryDate() + "|" + ticket.getEntryTime() + "|" + ticket.getExitDate() + "|" + ticket.getExitTime() + "|" + ticket.getTotalTime() + "|" + ticket.getTotalFees() + "|" + ticket.isPaid();
		}
		System.out.println("Returning:\n" + result);
		return result;
	}
	
}