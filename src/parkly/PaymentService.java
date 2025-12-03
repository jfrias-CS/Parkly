package parkly;

import java.util.ArrayList;
import java.util.List;

public class PaymentService {
	private static int nextPaymentID = 1000;
	private static synchronized int getNextPaymentID() { return nextPaymentID++; }
	private static final PaymentService instance = new PaymentService();
	private static final List<Payment> approvedPayments = new ArrayList<>();
	
	private PaymentService() {}
	
	public static PaymentService getInstance() {
		return instance;
	}
	
	public List<Payment> getPayments() {
		synchronized (approvedPayments) {
			return new ArrayList<>(approvedPayments);
		}
	}
	
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
		Payment newPayment = new Payment(getNextPaymentID(), ticketID, employeeID, gateID, String.valueOf(ticketDue), ticketPayType);
		synchronized (approvedPayments) {
			approvedPayments.add(newPayment);
		}
		System.out.println("11. PS.generateNewPayment: New Payment Made.");
		String paymentString = toString(newPayment);
		return paymentString;
	}
	
	public Payment findPayment(String paymentID) {
		synchronized(approvedPayments) {
			for (Payment payment : approvedPayments) {
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
			for (Payment payment : approvedPayments) {
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
