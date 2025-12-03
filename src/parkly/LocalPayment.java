package parkly;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalPayment {
	private final String tag = "PAYMENT";
	private final int paymentID;
	private final String ticketID;
	private final String employeeID;
	private final String gateID;
	private final double amountPaid;
	private final String paymentMethod;
	private String paymentDate;
	private String paymentTime;
	
	LocalPayment(int nextPaymentID, String ticketID, String employeeID, String gateID, String paymentDate, String paymentTime, String paymentMethod, String amountPaid) {
		this.paymentID = nextPaymentID;
		this.ticketID = ticketID;
		this.employeeID = employeeID;
		this.gateID = gateID;
		this.amountPaid = Double.parseDouble(amountPaid);
		this.paymentMethod = paymentMethod;
		this.paymentDate = paymentDate;
		this.paymentTime = paymentTime;
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
	
}
