package parkly;


import java.util.List;

public class Report implements ObjectTag {
    private static final long serialVersionUID = 1L;

    private final String tag = "REPORT";
    private final String date;              // e.g. "12/1/2025"
    private final List<Ticket> tickets;     // tickets for that date

    public Report(String date, List<Ticket> tickets) {
        this.date = date;
        this.tickets = tickets;
    }

    @Override
    public String getObjectTag() {
        return tag;
    }

    public String getDate() {
        return date;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
    
}