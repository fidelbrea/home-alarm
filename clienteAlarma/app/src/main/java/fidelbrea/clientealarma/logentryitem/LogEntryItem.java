package fidelbrea.clientealarma.logentryitem;

public class LogEntryItem {

    private String timestamp;
    private String event;

    public LogEntryItem(String timestamp, String event){
        this.timestamp = timestamp;
        this.event = event;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
