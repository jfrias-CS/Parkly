package parkly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EmployeeService: Manages the client-side session state (logged-in Employee, 
 * Gate ID) and acts as the application layer interface. 
 * It routes all application-level requests (like generateTicket) to the 
 * SocketConnectionService, automatically adding the required session context (Gate ID).
 */
public class EmployeeService {
	private final static String DELIMITER = "|\n";
	// --- STATIC SESSION STATE (Central Source of Truth) ---
    private static EmployeeService instance = new EmployeeService();
    private Message msg = null;
    private static Employee currentEmployee;
    private static String employeeID;
    private static String entryGateID; 
    private static String entryGateStatus;
    private static String exitGateID;
    private static String exitGateStatus; 
    private final Map<String, LocalTicket> activeTickets = new ConcurrentHashMap<>();
    
    // Map for tickets that have been paid and exited (deleted from the server's active list)
    private final Map<String, LocalTicket> deletedTickets = new ConcurrentHashMap<>();
    
    private final Map<String, LocalPayment> localPayments = new ConcurrentHashMap<>();
   private EmployeeService() {
	   this.entryGateStatus = "CLOSED";
	   this.exitGateStatus = "CLOSED";
   }
   public static EmployeeService getInstance() {
	   return instance;
   }
  
    // --- SESSION MANAGEMENT METHODS ---

    /**
     * Sets the Employee object upon successful login and stores the Gate ID.
     */
   
   public Message sendLoginRequest(Message loginMsg) {
	   this.msg = SocketConnectionService.sendLoginRequest(loginMsg);
	   if (this.msg == null || !this.msg.getType().equalsIgnoreCase("LOGIN") || !this.msg.getStatus().equalsIgnoreCase("SUCCESS") || this.msg.getText().isEmpty()) {		   
		   return new Message("ERROR", "ERROR", "Invalid Login.");
	   }
	   return this.msg;
   }
    public void setLoggedInEmployee(Employee employee) {
    	currentEmployee = employee;
    	employeeID = employee.getEmployeeID();        entryGateID = employee.getEntryGateID();        exitGateID = employee.getExitGateID();
        
        System.out.println("EmployeeService: Session started for Employee [" + employee.getEmployeeID() + "] at Gates [" + entryGateID + "|" + exitGateID +  "]");
    }

    /**
     * Gets the current Employee object for local statistics and context.
     */
    public Employee getCurrentEmployee() {
        return currentEmployee;
    }
    
    /**
     * Gets the assigned Gate ID.
     */
    public String getCurrentGateId() {
        return entryGateID + "|" + exitGateID;
    }


    // --- CONNECTION & LIFECYCLE INTERFACE ---

    // NOTE: This delegates to the existing SocketConnectionService's static methods.
    
 // Inside EmployeeService.java

    public SocketConnection connect(String ip, int port) throws IOException {
        try {
            // 1. Delegation: Hand off the connection attempt to the dedicated network layer.
            // The SocketConnectionService handles creating the Socket, ObjectStreams, 
            // and performing the initial login handshake with the server.
            SocketConnection connection = SocketConnectionService.connect(ip, port);
            
            System.out.println("EmployeeService.SocketConnetion.connect: Connection established via SocketConnectionService.");
            
            // 2. Return Handler: The result is the connection handler object (SocketConnection),
            // which includes the running input stream thread (the Runnable instance).
            return connection;
            
        } catch (IOException e) {
            // 3. Error Propagation: Catch any socket errors and re-throw them 
            // up to the calling code (the ConnectTask).
            System.err.println("EmployeeService: Connection failed.");
            throw e;
        }
    }

    public void disconnect() {
        if (currentEmployee != null) {
            // Clock out the employee before disconnecting
            currentEmployee.clockOut(); 
            System.out.println("EmployeeService: Employee [" + currentEmployee.getEmployeeID() + "] clocked out.");
        }
        
        SocketConnectionService.disconnect();
        
        // Clear session data
        currentEmployee = null;
        entryGateID = null;
        exitGateID = null;
    }

    public void sendMessage(Message msg) {
        SocketConnectionService.sendMessage(msg);
    }


    // --- APPLICATION LOGIC METHODS (ADDING CONTEXT BEFORE ROUTING) ---

    /**
     * Generates a new ticket, using the stored Gate ID as context for the server.
     */
//    public LocalTicket generateTicket() {
//        // SocketConnectionService handles the synchronous request/response
//    	System.out.println("EmployeeService.generateTicket: Sending request for ticket at gate: " + entryGateID);
//        String returnTicket = SocketConnectionService.generateTicket(employeeID, entryGateID); 
//        LocalTicket newTicket = processTicketString(returnTicket);
//        if (newTicket != null) {
//        	System.out.println("Adding ticket to ticket list");
//        	employeeActiveTickets.add(newTicket);
//        }
//        return newTicket;
//    }
    
    
    public static Report getReport(String date) {
    	Report report = SocketConnectionService.getReport(date);
	    if (report != null) {
	        System.out.println("EmployeeService.getReport: requesting report for " + date);
	        return report;
	    } else {
	        System.err.println("EmployeeService.getReport: Not connected.");
	        return null;
	    }
    }
    public LocalTicket generateTicket() {
        // SocketConnectionService handles the synchronous request/response
    	System.out.println("EmployeeService.generateTicket: Sending request for ticket at gate: " + entryGateID);
        String returnTicket = SocketConnectionService.generateTicket(employeeID, entryGateID); 
        System.out.println("ES.generateTicket: New Ticket:\n" + returnTicket);
        if (returnTicket != null && !returnTicket.startsWith("ERROR:")) {
        	
        // 1. Convert the server's string response to a LocalTicket object
        LocalTicket newTicket = processTicketString(returnTicket);
        
        if (newTicket != null) {
        	System.out.println("EmployeeService: Adding new ticket " + newTicket.getTicketID() + " to active list.");
            
            // 2. Use the map update helper to add the new ticket.
            // Since it's new and unpaid, it goes into activeTickets.
        	updateLocalTicket(newTicket); 
        } else {
        	System.out.println("TICKET NULL");
        }
        System.out.println("NEW TICKET: \n" + newTicket);
        return newTicket;
        } else {
        	return null;
        }
    }

    public String getSpaces() {
    	System.out.println("ES.getSpaces start.");
    	String spaces = SocketConnectionService.getSpaces();
    	if (spaces == null) {
    		return "ERROR";
    	} else {
    		System.out.println("ES.getSpaces: " + spaces);
    		return spaces;
    	}
    }
    /**
     * Synchronizes the local activeTickets map with the server and returns the current list.
     * @return A List of the current active LocalTicket objects.
     */
    public Map<String, LocalTicket> getLocalActiveTickets() {
        
        // 1. Get the single, large string of all active tickets from the server.
        // NOTE: This call must be updated to return the delimited string from the server layer.
        String allTicketsString = SocketConnectionService.getActiveTickets();
        
        
        // Create a temporary map to hold the fresh data from the server
        Map<String, LocalTicket> freshActiveTicketsMap = new ConcurrentHashMap<>();

        if (allTicketsString != null && !allTicketsString.trim().isEmpty()) {
            
            // 2. Split the long string into individual ticket data strings.
            // The double backslash is necessary to escape the pipe symbol in regex.
            String[] individualTicketStrings = allTicketsString.split("\n");

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
    
    public Map<String, LocalPayment> getLocalPayments() {
    	String allPaymentsString = SocketConnectionService.getPayments();
    	Map<String, LocalPayment> freshPaymentsMap = new ConcurrentHashMap<>();
    	if (allPaymentsString != null && !allPaymentsString.trim().isEmpty()) {
    		String[] individualPaymentStrings = allPaymentsString.split("\n");
    		System.out.println("Individual Payments String:\n" + individualPaymentStrings);
    		for (String paymentString : individualPaymentStrings) {
    			System.out.println("Individual Payment String:\n " + paymentString);
    			LocalPayment payment = processPaymentString(paymentString.trim());
    			if (payment != null) {
    				freshPaymentsMap.put(payment.getPaymentID(), payment);
    			}
    		}
    	}
    	
    	localPayments.clear();
    	localPayments.putAll(freshPaymentsMap);
    	System.out.println("ES: Payments synchronized with server. New size: " + localPayments.size());
    	return localPayments;
    }
    
    
    /**
     * Sends the signal to open the entry gate, using the Gate ID.
     */
    public void openEntryGate() {
        SocketConnectionService.openEntryGate(entryGateID);
    }

    public void openExitGate() {
    	SocketConnectionService.openExitGate(exitGateID);
    }
    
    	
//    public String generateGateReport(String ticketID, boolean isOverride, String reason) {
//    	System.out.println("ES.openExitGate: OPENING GATE: " + this.exitGateID);
//    	String data = "TicketID: " + ticketID + "|" + "Employee: " + this.employeeID + "|" + "Gate Opened: " + this.exitGateID + "|" + "Reason: " + reason + "|" + "Override: " + isOverride;
//    	
////    	Message msg = new Message("GATE", "OPEN REQUEST", data);
//    	String return message = SocketConnectionService.openExitGate(data);
    	
    
    /**
     * Looks up a ticket by ID. (Gate ID is not strictly needed here, but passed through.)
     */
    public LocalTicket findTicket(String ticketID) {
        String returnedTicket = SocketConnectionService.findTicket(ticketID); 
        LocalTicket ticket = processTicketString(returnedTicket);
        System.out.println();
        return ticket;
    }

    /**
     * Sends the payment update, including the Gate ID as context for the transaction record.
     */
    public LocalPayment makePayment(String payTicketID, String payType, double amount) {
    	System.out.println("2. ES.MAKEPAYMENT");
    	String paymentString = SocketConnectionService.makePayment(payTicketID, payType, amount, employeeID, exitGateID); 
    	LocalPayment returnPayment = processPaymentString(paymentString);
    	if (returnPayment == null) {
    		System.out.println("ES.MP: ERROR");
    		return null;
    	}
    	System.out.println("16. RETURNING FROM ES.MP - PAYMENT ID: " + returnPayment);
    	return returnPayment;
    }
    
    /**
     * Sends the 'Release Ticket' command, crucial for freeing up the parking space.
     */
//    public Boolean releaseTicket(String ticketID) {
//        // The server needs the Gate ID to know which exit point is confirming the release
//        return SocketConnectionService.releaseTicket(ticketID, exitGateID); 
//    }
    
    /**
     * Sends the 'Release Ticket' command and moves the ticket from activeTickets 
     * to deletedTickets upon success.
     */
    public Boolean releaseTicket(String ticketID) {
        // 1. Delegate the release request to the server
        Boolean success = SocketConnectionService.releaseTicket(ticketID, exitGateID); 
        
        if (success.booleanValue()) {
            // 2. If the server successfully released it, update local state
            LocalTicket releasedTicket = activeTickets.get(ticketID);
            
            if (releasedTicket != null) {
                // 3. Mark the local copy as paid (assuming release implies payment/completion)
                releasedTicket.markPaid();
                
                // 4. Use the helper to move it (removes from active, adds to deleted)
                updateLocalTicket(releasedTicket);
                System.out.println("EmployeeService: Ticket " + ticketID + " successfully released and moved to deleted list.");
                
            } else {
                System.err.println("EmployeeService: Released ticket " + ticketID + " not found in active list.");
            }
        }
        
        return success; 
    }
    
    public LocalTicket processTicketString(String ticketString) {
        if (ticketString == null || ticketString.trim().isEmpty()) {
            System.err.println("Ticket string is null or empty.");
            return null;
        }

        // Split the string by   ("|")
        String[] parts = ticketString.split("\\|");
        
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
    
    
    public void updateLocalTicket(LocalTicket ticket) {
        String id = ticket.getTicketID();
        
        // Logic 1: If the ticket is paid (completed)
        if (ticket.isPaid()) {
            // Move it from the active list to the completed list (if it was there)
            activeTickets.remove(id);
            deletedTickets.put(id, ticket);
            System.out.println("Ticket " + id + " moved to completed list.");
        } 
        // Logic 2: If the ticket is active/unpaid
        else {
            // Just replace the old version with the new version (update)
            activeTickets.put(id, ticket);
            System.out.println("Ticket " + id + " updated in active list.");
            // Ensure it is NOT in the completed list
            deletedTickets.remove(id);
        }
    }
    
    public void setLocalActiveTickets(List<LocalTicket> serverList) {
        // 1. Clear the old map
        activeTickets.clear();
        
        // 2. Populate with the new list from the server
        for (LocalTicket ticket : serverList) {
            // Use the ticket ID as the key
            activeTickets.put(ticket.getTicketID(), ticket);
        }
        System.out.println("Active ticket list synchronized. New size: " + activeTickets.size());
    }
    
    public LocalPayment processPaymentString(String paymentString) {
        if (paymentString == null || paymentString.trim().isEmpty()) {
            System.err.println("Payment string is null or empty.");
            return null;
        }

        // Split the string by the pipe character ("|"). We must escape it with two backslashes.
        String[] parts = paymentString.split("\\|");
        
        // Check for the expected 8 fields
        final int EXPECTED_PARTS = 8;
        if (parts.length != EXPECTED_PARTS) {
            System.err.println("Error: Payment string has unexpected number of fields (" + parts.length + "). Expected " + EXPECTED_PARTS + ".");
            System.err.println("Raw String: " + paymentString);
            return null;
        }

        try {
            // Incoming format:
            // [0] PaymentID      | [1] TicketID | [2] EmployeeID | [3] GateID 
            // [4] PaymentDate    | [5] PaymentTime | [6] PaymentMethod | [7] AmountPaid (String)
            
            // --- Parse Fields ---
            String paymentID = parts[0].trim(); // Assuming the constructor takes a String ID for simplicity
            String ticketID = parts[1].trim();
            String employeeID = parts[2].trim(); 
            String gateID = parts[3].trim();
            String paymentDate = parts[4].trim();
            String paymentTime = parts[5].trim();
            String paymentMethod = parts[6].trim();
            String amountPaid = parts[7].trim(); // Remains a String as per your constructor

            // --- Use the provided constructor signature ---
            // LocalPayment(int nextPaymentID, String ticketID, String employeeID, String gateID, 
            //              String paymentDate, String paymentTime, String paymentMethod, String amountPaid)
            
            LocalPayment newPayment = new LocalPayment(
                Integer.parseInt(paymentID), // Assuming paymentID is convertible to int for the constructor's 'nextPaymentID'
                ticketID, 
                employeeID, 
                gateID, 
                paymentDate, 
                paymentTime, 
                paymentMethod, 
                amountPaid
            );
            
            return newPayment;

        } catch (NumberFormatException e) {
            // Catches error if paymentID or amountPaid are not valid numbers
            System.err.println("Error parsing numeric fields (PaymentID or AmountPaid). Error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error processing payment string: " + e.getMessage());
            return null;
        }
    }
    
    public void updateLocalPayment(LocalPayment payment) {
        // Get the ID as a String to use as the map key.
        String id = payment.getPaymentID(); // Must return String
        
        // Add/Update: Payments are fixed records.
        localPayments.put(id, payment);
        System.out.println("Payment record [" + id + "] updated/added to local map.");
    }
    
    public void setLocalPayments(List<LocalPayment> serverList) {
        // 1. Clear the old map to ensure full synchronization
        localPayments.clear();
        
        // 2. Populate with the new list from the server
        for (LocalPayment payment : serverList) {
            // Use the String Payment ID as the key for O(1) access
            localPayments.put(payment.getPaymentID(), payment);
        }
        System.out.println("Local payment list synchronized. Total records: " + localPayments.size());
    }
    
}