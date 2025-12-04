package parkly;

// Local version of payment object (client side)
public class LocalPayment {
	private final String tag = "PAYMENT";
	private final int paymentID;
	private final String ticketID;
	private final String employeeID;
	private final String gateID;
	private final String paymentDate;
	private final String paymentTime;
	private final double amountPaid;
	private final String paymentMethod;
	
	LocalPayment(int paymentID, String ticketID, String employeeID, String gateID, String paymentDate, String paymentTime, String paymentMethod, String amountPaid) {
		this.paymentID = paymentID;
		this.ticketID = ticketID;
		this.employeeID = employeeID;
		this.gateID = gateID;
		this.amountPaid = Double.parseDouble(amountPaid);
		this.paymentMethod = paymentMethod;
		this.paymentDate = paymentDate;
		this.paymentTime = paymentTime;
	}
	
	public String getPaymentID() { return String.valueOf(this.paymentID); }
	
	public String getTicketID() { return this.ticketID; }
	
	public String getEmployeeID() { return this.employeeID; }
	
	public String getGateID() { return this.gateID; }
	
	public String getPaymentDate() { return this.paymentDate; }
	
	public String getPaymentTime() { return this.paymentTime; }
	
	public String getPaymentMethod() { return this.paymentMethod; }
	
	public double getPaymentAmount() { return this.amountPaid; }
}
