package parkly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PaymentService {
	private static final String PAYMENT_DELIMITER = "\n";
	private static int nextPaymentID = 1000;
	private static synchronized int getNextPaymentID() { return nextPaymentID++; }
	private static final PaymentService instance = new PaymentService();
	private static final Map<String, Payment> approvedPayments = new ConcurrentHashMap<>();
	
	private PaymentService() {}
	
	public static PaymentService getInstance() {
		return instance;
	}
	
	public String getPayments() {
	    StringBuilder allPaymentsString = new StringBuilder();
	    boolean first = true; // Use a flag to avoid prepending the delimiter on the first payment
	    
	    for (Payment payment : approvedPayments.values()) {
	        // Prepend the delimiter only if it's NOT the first payment
	        if (!first) {
	            allPaymentsString.append(PAYMENT_DELIMITER); // Appends "\n"
	        }
	        allPaymentsString.append(toString(payment));
	        first = false;
	    }
	    System.out.println("PS.getPayments: " + allPaymentsString.toString());
	    return allPaymentsString.toString();
	}
	
//	public String getPayments() {
//	    StringBuilder allPaymentsString = new StringBuilder();
//	    boolean first = true;
//	    
//	    for (Payment payment : approvedPayments.values()) {
//	        if (!first) {
//	            allPaymentsString.append(PAYMENT_DELIMITER);
//	        }
//	        allPaymentsString.append(toString(payment));
//	        first = false;
//	    }
//	    System.out.println("PS.getPayments: " + allPaymentsString.toString());
//	    return allPaymentsString.toString();
//	}
	
	public String recordPayment(String ticketID, String ticketPayType, String amount, String employeeID, String gateID) {
		System.out.println("7. PS.RP: Inside recordPayment.");
		TicketService ticketService = TicketService.getInstance();
		Ticket ticket = ticketService.findTicket(ticketID);
		
		if (ticket == null || ticket.isPaid()) {
			System.err.println("PaymentService: Ticket not found or already paid.");
			return null;
		}
		ticketService.setExitStamp(ticket.getTicketID());
		
		double ticketDue = ticket.getTotalFees();
		double paidAmount = 0.00;
		try {
			paidAmount = Double.parseDouble(amount);
		} catch (NumberFormatException e) {
			System.out.println("Invalid payment entry.");
			return null;
		}
		double changeDue = paidAmount - ticketDue;
		
		if (changeDue < 0) { // underpaid
//			ticket.makePayment(paidAmount);
			System.out.println("PaymentService: Payment failed. Underpaid by: " + (-changeDue));
			return null;
		} 
		
		boolean ticketPaid = ticketService.markTicketPaid(ticketID);
		if (!ticketPaid) {
			System.out.println("TICKET NOT PAID------");
		}
		String paymentString;
		synchronized (approvedPayments) {
		    int paymentID = getNextPaymentID();  // Get ID inside sync block
		    Payment newPayment = new Payment(paymentID, ticketID, employeeID, gateID, String.valueOf(ticketDue), ticketPayType);
		    approvedPayments.put(newPayment.getPaymentID(), newPayment);
		    paymentString = toString(newPayment);
		}
		System.out.println("11. PS.generateNewPayment: New Payment Made.");
		return paymentString;
	}
	
	public Payment findPayment(String paymentID) {
		synchronized(approvedPayments) {
			for (Payment payment : approvedPayments.values()) {
				if (payment.getPaymentID().equals(paymentID)) {
					return payment;
				}
			}
		}
		return null;
	}
	
//	public String findPaymentAsString(String paymentID) {
//		
//	}
	
	public List<Payment> findPaymentsByTicketID(String ticketID) {
		List<Payment> paymentsForTicket = new ArrayList<>();
		synchronized(approvedPayments) {			
			for (Payment payment : approvedPayments.values()) {
				if (payment.getTicketID().equals(ticketID)) {
					paymentsForTicket.add(payment);
				}
			}
		}
		if (paymentsForTicket.isEmpty()) {
			System.out.println("PaymentService: No payments found for Ticket ID: " + ticketID);
		}
		return paymentsForTicket;
	}
	
	public String toString(Payment payment) {
		if (payment == null) {
			return "ERROR MAKING PAYMENT STRING.";
		}
		// payment ID | ticket ID | employee | gate | date | time | payment method | amount paid
		return payment.getPaymentID() + "|" + payment.getTicketID() + "|" + payment.getEmployeeID() + "|" + payment.getGateID() + "|" + payment.getPaymentDate() + "|" + payment.getPaymentTime() + "|" + payment.getPaymentMethod() + "|" + payment.getPaymentAmount();
	}
}
