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
	private static final String TICKET_DELIMITER = "|\n"; // Unique separator between tickets
	
	private static int nextTicketID;
	private static synchronized int getNextTicketID() {return nextTicketID++;};
	private static final TicketService instance = new TicketService();
	
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
	public String getActiveTickets() {
		StringBuilder allTicketsString = new StringBuilder();
		
		// Iterate over the values (Ticket objects) in the thread-safe map
		for (Ticket ticket : activeTickets.values()) {
			// Append the string representation of the current ticket
			allTicketsString.append(toString(ticket));
			
			// Append the unique delimiter after each ticket, EXCEPT the last one.
			// This check is complex with ConcurrentHashMap, so we'll just append it
			// and trim it off later, or rely on the client to handle a trailing delimiter.
			// For simplicity and safety in concurrency, we append the delimiter every time.
			allTicketsString.append(TICKET_DELIMITER);
		}
		
		// Remove the trailing delimiter if the string isn't empty
		if (allTicketsString.length() > TICKET_DELIMITER.length()) {
			allTicketsString.setLength(allTicketsString.length() - TICKET_DELIMITER.length());
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
			
			String returnTicket = toString(newTicket);
			System.out.println("TS.generateNewTicket: New ticket generated: \n" + returnTicket);
			return returnTicket;
		}
		return "ERROR GENERATING NEW STRING";
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
		Ticket ticket = activeTickets.get(ticketID); 
		
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
//			removeTicketFromList(foundTicket);
			return true;
		}
		return false;
	}
	
//	private void removeTicketFromList (Ticket ticket) {
//		synchronized(paidTickets) {
//			paidTickets.add(ticket);
//			sortPaidList();
//		}
//		activeTickets.remove(ticket.getTicketID());
//		
//		System.out.println("10. Added ticket to paid ticket and removed it form active tickets.");
//	}
	
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
			// ID | GATE | EMP | E_DATE | E_TIME | TOTAL_TIME | TOTAL_FEES | IS_PAID (false)
			result = ticket.getTicketID() + "\n" + ticket.getGateID() + "\n" + ticket.getEmployeeID() + "\n" + ticket.getEntryDate() + "\n" + ticket.getEntryTime() + "\n" + ticket.getTotalTime() + "\n" + ticket.getTotalFees() + "\n" + ticket.isPaid();
		} else {
			// Paid String: 10 fields
			// ID | GATE | EMP | E_DATE | E_TIME | X_DATE | X_TIME | TOTAL_TIME | TOTAL_FEES | IS_PAID (true)
			result = ticket.getTicketID() + "\n" + ticket.getGateID() + "\n" + ticket.getEmployeeID() + "\n" +ticket.getEntryDate() + "\n" + ticket.getEntryTime() + "\n" + ticket.getExitDate() + "\n" + ticket.getExitTime() + "\n" + ticket.getTotalTime() + "\n" + ticket.getTotalFees() + "\n" + ticket.isPaid();
		}
		System.out.println("Returning:\n" + result);
		return result;
	}
	
}