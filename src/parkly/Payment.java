package parkly;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment implements ObjectTag {
	private final String tag = "PAYMENT";
	private final int paymentID;
	private final String ticketID;
	private final String employeeID;
	private final String gateID;
	private final double amountPaid;
	private final String paymentMethod;
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy"); 
	private final transient LocalDateTime paymentDateTime;
	private String paymentDate;
	private String paymentTime;
	
	public Payment(int paymentID, String ticketID, String employeeID, String gateID, String amountPaid, String paymentMethod) {
		this.paymentID = paymentID;
		this.ticketID = ticketID;
		this.employeeID = employeeID;
		this.gateID = gateID;
		this.amountPaid = Double.parseDouble(amountPaid);
		this.paymentMethod = paymentMethod;
		this.paymentDateTime = LocalDateTime.now();
		this.paymentDate = this.paymentDateTime.format(DATE_FORMATTER);
		this.paymentTime = this.paymentDateTime.format(TIME_FORMATTER);
	}
	
	public String getPaymentID() {
		return String.valueOf(this.paymentID);
	}
	
	public double getPaymentAmount() {
		return amountPaid;
	}
	
	public String getPaymentMethod() {
		return this.paymentMethod;
	}
	public String getPaymentDate() {
		return this.paymentDate;
	}
	
	public String getPaymentTime() {
		return this.paymentTime;
	}
	
	public String getEmployeeID() {
		return this.employeeID;
	}
	public String getGateID() {
		return this.gateID;
	}
	public String getTicketID() {
		return this.ticketID;
	}
	@Override
	public String getObjectTag() {
		return this.tag;
	}
	
}
