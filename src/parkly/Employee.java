package parkly;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Used to store Employee Login & Lgout times
// Assign employee to gate pairs to track their operations
public class Employee {
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    private final String employeeID;
    private final String entryGateID; 
    private final String exitGateID;
    private final LocalDateTime clockInStamp;
    private final String clockInDate;
    private final String clockInTime;
    private LocalDateTime clockOutStamp;
    private String clockOutDate;
    private String clockOutTime;
    private String hoursWorked;
    private int ticketsProcessedCount;

    public Employee(String employeeID, String entryGateID, String exitGateID) {
        this.employeeID = employeeID;
        this.entryGateID = entryGateID + "_ENTRY";
        this.exitGateID = exitGateID + "_EXIT";
        this.clockInStamp = LocalDateTime.now();
        this.clockInDate = this.clockInStamp.format(DATE_FORMATTER);
        this.clockInTime = this.clockInStamp.format(TIME_FORMATTER);
        this.clockOutStamp = null;
        this.clockOutDate = "";
        this.clockOutTime = "";
        this.hoursWorked = "0";
        
        this.ticketsProcessedCount = 0;
    }

    public void clockOut() {
        this.clockOutStamp = LocalDateTime.now();
        this.clockOutDate = this.clockOutStamp.format(DATE_FORMATTER);
        this.clockOutTime = this.clockOutStamp.format(TIME_FORMATTER);
    }

    public void incrementProcessedCount() {
        this.ticketsProcessedCount++;
    }

    // Getters for reporting
    public String getEmployeeID() { return this.employeeID; }
    public String getEntryGateID() { return this.entryGateID; }
    public String getExitGateID() { return this.exitGateID; }
    public String getClockInDate() { return this.clockInDate; }
    public String getClockInTime() { return this.clockInTime; }
    public String getClockOutDate() { return this.clockOutDate; }
    public String getClockOutTime() { return this.clockOutTime; }
    public int getTicketsProcessedCount() { return this.ticketsProcessedCount; }
}