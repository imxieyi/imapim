package imapim.data;

public class PubGPGKey{
    private final String fingerPrint;
    private final String  userID;
    private final String date;

    public String getFingerPrint() {
        return fingerPrint;
    }

    public String getUserID() {
        return userID;
    }

    public String getDate() {
        return date;
    }

    public PubGPGKey(String fingerPrint, String userID, String date ){
        this.date = date;
        this.fingerPrint =fingerPrint;
        this.userID  = userID;
    }


    @Override
    public String toString() {
        return String.format("%s\n%s\n%s\n", userID, date, fingerPrint);
    }
}
