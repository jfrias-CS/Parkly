package parkly;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.io.Serializable;

public class Ticket implements ObjectTag, Serializable {
//	private static final long serialVerisionUID = 1L;
	private static int nextTicketID = 0;
	private  final String employeeID;
	private final String gateID;
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
	private final String tag = "TICKET";
	private int ticketID;
	private transient LocalDateTime entryStamp; // transient is needed to send object over stream because LocalDateTime is not serializable 
	private String entryDate;
	private String entryTime;
    private transient LocalDateTime exitStamp;
    private String exitDate;
    private String exitTime;
    private int totalTime;
    private int fee;
    private int totalDue;
    private int totalPaid;
    private boolean isPaid;
    
    
    public Ticket(int ticketID, String employeeID, String gateID) {
    	this.ticketID = ticketID;
    	this.employeeID = employeeID;
    	this.gateID = gateID;
        this.entryStamp = LocalDateTime.now(); // create new date time object here.
        this.entryDate = this.entryStamp.format(DATE_FORMATTER);
        this.entryTime = this.entryStamp.format(TIME_FORMATTER);
        this.exitStamp = null;
        this.exitDate = "";
        this.exitTime = "";
        this.totalTime = 0;
        this.fee = 5;
        this.totalDue = this.fee;
        this.isPaid = false;
    }
    
    // Get object tag
    public String getObjectTag() {
    	return this.tag;
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
    // Return LocalDateTime entry stamp
    public LocalDateTime getEntryStamp() {
        return this.entryStamp;
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
    // Return LocalDateTime exit stamp
    public LocalDateTime getExitStamp() {
    	if (this.exitStamp != null) {
    		return this.exitStamp;    		
    	}
    	return this.entryStamp;
    }
    // Set LocalDateTime exit stamp
    public void setExitStamp() {
    	this.exitStamp = LocalDateTime.now();
    	this.exitDate = this.exitStamp.format(DATE_FORMATTER);
    	this.exitTime = this.exitStamp.format(TIME_FORMATTER);
    	calcTotalTime();
    	calcFeeTotals();
//    	System.out.println("TICKET.setExitStamp:\n\tExit Date: " + this.exitDate + "\n\tExit Time: " + this.exitTime);
    }
    public String getExitTime() {
    	return this.exitTime;
    }
    // return total minutes
    public void calcTotalTime() {
    	if (this.exitStamp != null) {
    		Duration timeDifference = Duration.between(this.entryStamp, this.exitStamp);
    		this.totalTime = (int) timeDifference.toMinutes();
    		this.totalTime = (this.totalTime + 59) / 60;
    	}
    }
    
    public int getTotalTime() {
    	return this.totalTime;
    }
    public void calcFeeTotals() {
        this.totalDue = this.fee * this.totalTime;
    }

    public int getTotalFees() {
    	return this.totalDue;
    }
    
    public void makePayment(double paymentAmount) {
    	this.totalDue -= paymentAmount;
    }
    public void markPaid() {
    	this.isPaid = true;
    }

    public boolean isPaid() {
    	return this.isPaid;
    }
    
    @Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		
		Ticket ticket = (Ticket) obj;
		return Objects.equals(ticketID,  ticket.ticketID);
	}
    
    @Override
    public int hashCode() {
    	return Objects.hash(ticketID);
    }
}




