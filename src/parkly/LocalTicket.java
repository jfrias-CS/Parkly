package parkly;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.io.Serializable;

public class LocalTicket {
	private final int ticketID; 
	private final String employeeID;
	private final String gateID;
	private final String entryDate;
	private final String entryTime;
    private String exitDate;
    private String exitTime;
    private int totalTime;
    private int fee;
    private int totalDue;
    private int totalPaid;
    private boolean isPaid;
    
    
    public LocalTicket(int ticketID, String employeeID, String gateID, String entryDate, String entryTime, String totalDue) {
    	this.ticketID = ticketID;
    	this.employeeID = employeeID;
    	this.gateID = gateID;
    	this.entryDate = entryDate;
    	this.entryTime = entryTime;
        this.exitDate = "";
        this.exitTime = "";
        this.totalTime = 0;
        this.fee = 5;
        try {
        	this.totalDue = Integer.parseInt(totalDue);
        } catch (NumberFormatException e) {
        	System.err.println("Error: The string is not a valid integer format.");
        }
        this.isPaid = false;
    }
    
    public LocalTicket(int ticketID, String employeeID, String gateID, String entryDate, String entryTime, String exitDate, String exitTime, String totalTime, String totalDue) {
    	this.ticketID = ticketID;
    	this.employeeID = employeeID;
    	this.gateID = gateID;
    	this.entryDate = entryDate;
    	this.entryTime = entryTime;
        this.exitDate = exitDate;
        this.exitTime = exitTime;
        try {
        	this.totalTime = Integer.parseInt(totalTime);
        } catch (NumberFormatException e) {
        	System.err.println("Error: The string is not a valid integer format.");
        }
        this.fee = 5;
        try {
        	this.totalDue = Integer.parseInt(totalDue);
        } catch (NumberFormatException e) {
        	System.err.println("Error: The string is not a valid integer format.");
        }
        this.isPaid = false;
    }
    
    // Return ticket ID
    public String getTicketID() {
        return String.valueOf(this.ticketID);
    }
    public String getEmployeeID() {
    	return this.employeeID;
    }
    public String getGateID() {
    	return this.gateID;
    }
    
    // Return String Date 
    public String getEntryDate() {
    	return this.entryDate;
    }
    public String getExitDate() {
    	return this.exitDate;
    }
    // Return String Time Stamp
    public String getEntryTime() {
    	return this.entryTime;
    }
    
    public String getExitTime() {
    	return this.exitTime;
    }
    
    public int getTotalTime() {
    	return this.totalTime;
    }

    public int getTotalFees() {
    	return this.totalDue;
    }
    
    public void markPaid() {
    	this.isPaid = true;
    }

    public boolean isPaid() {
    	return this.isPaid;
    }
    
    public void setExitDate(String date) {
    	this.exitDate = date;
    }
    
    public void setExitTiime(String time) {
    	this.exitTime = time;
    }
    
    public void setTotalTime(String totalTime) {
    	try {
        	this.totalTime = Integer.parseInt(totalTime);
        } catch (NumberFormatException e) {
        	System.err.println("Error: The string is not a valid integer format.");
        }
    }
    @Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		LocalTicket ticket = (LocalTicket) obj;
		return Objects.equals(ticketID,  ticket.ticketID);
	}
    
    @Override
    public int hashCode() {
    	return Objects.hash(ticketID);
    }
}




