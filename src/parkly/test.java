package parkly;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class test {
	
	Map<String, LocalTicket> activeTickets = new ConcurrentHashMap<>();
	
	
	public Map<String, LocalTicket> getLocalActiveTickets() {
        
        // 1. Get the single, large string of all active tickets from the server.
        // NOTE: This call must be updated to return the delimited string from the server layer.
        String allTicketsString = SocketConnectionService.getActiveTickets();
        
        
        // Create a temporary map to hold the fresh data from the server
        Map<String, LocalTicket> freshActiveTicketsMap = new ConcurrentHashMap<>();

        if (allTicketsString != null && !allTicketsString.trim().isEmpty()) {
            
            // 2. Split the long string into individual ticket data strings.
            // The double backslash is necessary to escape the pipe symbol in regex.
            String[] individualTicketStrings = allTicketsString.split("\\|\\n");

            // 3. Process each ticket string and populate the temporary map.
            for (String ticketString : individualTicketStrings) {
                // Trim whitespace
                LocalTicket ticket = processTicketString(ticketString.trim());
                
                if (ticket != null) {
                    // Add the newly created LocalTicket to the fresh map
                    freshActiveTicketsMap.put(ticket.getTicketID(), ticket);
                }
            }
        } 
        
        // 4. Overwrite the main local map to sync with the server's source of truth.
        // This is the thread-safe synchronization step.
        activeTickets.clear();
        activeTickets.putAll(freshActiveTicketsMap);
        
        System.out.println("EmployeeService: Active tickets synchronized with server. New size: " + activeTickets.size());

        // 5. Return the current contents of the synchronized local map (the Map itself).
        return activeTickets;
    }
	
	
	public LocalTicket processTicketString(String ticketString) {
        if (ticketString == null || ticketString.trim().isEmpty()) {
            System.err.println("Ticket string is null or empty.");
            return null;
        }

        // Split the string by the newline character ("\n")
        String[] parts = ticketString.split("\n");
        
        // Check if we have enough fields for a basic ticket
        if (parts.length < 8) {
            System.err.println("Error: Ticket string has too few fields: " + parts.length);
            return null;
        }

        try {
            // Core fields common to both formats (Index based on assumed 10-part paid string)
            // ----------------------------------------------------
            // CASE 1: UNPAID/Active Ticket (8 parts)
            // Shorter string, missing exit/total details.
            // ----------------------------------------------------
            if (parts.length == 8) {
                // Unpaid String assumed format:
                // (TicketID | EmployeeID | GateID | EntryDate | EntryTime | TotalTime | TotalFees | IsPaid [false])
                
                // Note: The totalTime and totalFees are likely placeholders or minimums 
                // since the ticket is still active/unpaid. We use parts[5] and parts[6].
            	 int ticketID = Integer.parseInt(parts[0].trim());
                 String employeeID = parts[1].trim(); 
                 String gateID = parts[2].trim();
                 String entryDate = parts[3].trim();
                 String entryTime = parts[4].trim();
                 String totalTime = parts[5].trim();
                 String totalFees = parts[6].trim();
//               String isPaid = parts[7].trim();
            	
            	
                // Use the 6-argument constructor
                LocalTicket newTicket = new LocalTicket(
                    ticketID, 
                    employeeID, 
                    gateID, 
                    entryDate, 
                    entryTime, 
                    totalTime,
                    totalFees
//                    , isPaid
                );
                
                // Explicitly mark it UNPAID based on the shorter length logic
                // The isPaid value is at parts[7] and should be "false"
//                if (parts[7].trim().equalsIgnoreCase("false")) {
//                     // isPaid defaults to false in the LocalTicket constructor, so no action needed here.
//                }
                
                return newTicket;
            } 
            // ----------------------------------------------------
            // CASE 2: PAID/Completed Ticket (10 parts)
            // Longer string, includes finalized exit/time/fee details.
            // ----------------------------------------------------
            else if (parts.length == 10) {
                // Paid String assumed format:
                // (TicketID | EmployeeID | GateID | EntryDate | EntryTime | ExitDate | ExitTime | TotalTime | TotalFees | IsPaid [true])
                
            	int ticketID = Integer.parseInt(parts[0].trim());
                String employeeID = parts[1].trim(); 
                String gateID = parts[2].trim();
                String entryDate = parts[3].trim();
                String entryTime = parts[4].trim();
                String exitDate = parts[5].trim();
                String exitTime = parts[6].trim();
                String totalTime = parts[7].trim();
                String totalFees = parts[8].trim();
//                String isPaid = parts[9].trim();
                
                // Use the 9-argument constructor
                LocalTicket newTicket = new LocalTicket(
                    ticketID, 
                    employeeID, 
                    gateID, 
                    entryDate, 
                    entryTime, 
                    exitDate, 
                    exitTime, 
                    totalTime, 
                    totalFees
                );

                // Explicitly mark it PAID based on the longer length logic
                // The isPaid value is at parts[9] and should be "true"
//                if (parts[9].trim().equalsIgnoreCase("true")) {
//                    newTicket.markPaid();
//                }
                
                return newTicket;
            } 
            // ----------------------------------------------------
            // CASE 3: ERROR - Unexpected Length
            // ----------------------------------------------------
            else {
                System.err.println("Error: Received ticket string with unexpected number of fields: " + parts.length);
                return null;
            }
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric fields in ticket string: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error processing ticket string: " + e.getMessage());
            return null;
        }
    }
	
}
