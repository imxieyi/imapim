package imapim.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;

/**
 * Model class for a Person.
 *
 * @author Marco Jakob
 */
public  class Person implements java.io.Serializable{
    private static final long serialVersionUID = 1L;

    private  final  StringProperty name;
    private  final StringProperty emile;
    private  final StringProperty key;
    private  final StringProperty ID;


    /**
     * Default constructor.
     */
    public Person() {
        this(null, null);
    }

    /**
     * Constructor with some initial data.
     *
     * @param name
     * @param emile
     */
    public Person(String name, String emile) {
        this.name = new SimpleStringProperty(name);
        this.emile = new SimpleStringProperty(emile);

        // Some initial dummy data, just for convenient testing.
        this.key = new SimpleStringProperty("some key" );
        this.ID = new SimpleStringProperty("some ID");
    }

    public String getFirstName() {
        return name.get();
    }

    public void setFirstName(String firstName) {
        this.name.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return name;
    }

    public String getLastName() {
        return emile.get();
    }

    public void setLastName(String lastName) {
        this.emile.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return emile;
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getID() {
        return ID.get();
    }

    public void setID(String ID) {
        this.ID.set(ID);
    }

    public StringProperty IDProperty() {
        return ID;
    }

    public static byte[] serialByte(Object obj)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Object deSerialByte(byte[] by)
    {
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(by));
            return ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}