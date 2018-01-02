package imapim.data;

public class PubGPGKey{
    private String fingerPrint;
    private String userID;
    private String date;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public PubGPGKey(String fingerPrint, String userID, String date ){
        this.date = date;

        this.fingerPrint = fingerPrint;
        this.userID = userID;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    @Override
    public String toString() {
        return String.format("%s\n%s\n%s\n", userID, date, fingerPrint);
    }
}
