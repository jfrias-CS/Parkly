package parkly;

// simple gate class to keep track of each employees work station
public class Gate {
	private String gateID;
	private String status; // OPEN or CLOSED
	
	Gate(String ID) {
		this.gateID = ID;
		this.status = "CLOSED";
	}
	public String getGateId() {
		return this.gateID;
	}
	public void open() {
		this.status = "OPEN";
	}
	
	public void close() {
		this.status = "CLOSE";
	}
}
