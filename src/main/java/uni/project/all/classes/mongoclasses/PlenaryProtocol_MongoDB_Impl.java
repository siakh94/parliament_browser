package uni.project.all.classes.mongoclasses;

import org.bson.Document;
import uni.project.all.classes.PlenaryProtocol_File_Impl;

/**
 * An additional Protocol class which inherits Protocols
 * and stores mainly a protocol's document (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class PlenaryProtocol_MongoDB_Impl extends PlenaryProtocol_File_Impl {

    private Document doc;

    /**
     * Constructor
     *
     * @param doc
     */
    public PlenaryProtocol_MongoDB_Impl(Document doc) {
        this.doc = doc;
    }

    /**
     * @return title as a String
     */
    @Override
    public String getTitle() {
        return doc.getString("Title");
    }

    /**
     * @return election period  as an integer
     */
    @Override
    public int getElectionPeriod() {
        return doc.getInteger("ElectionPeriod");
    }

    /**
     * @return date  as a String
     */
    @Override
    public String getDate() {
        return doc.getString("Date");
    }

    /**
     * @return start time  as a String
     */
    @Override
    public String getBeginTime() {
        return doc.getString("Starttime");
    }

    /**
     * @return end time as a String
     */
    @Override
    public String getEndTime() {
        return doc.getString("Endtime");
    }

    /**
     * @return protocol's index as an integer
     */
    @Override
    public int getIndex() {
        return doc.getInteger("Index");
    }

    /**
     * @return place  as a String
     */
    @Override
    public String getPlace() {
        return doc.getString("Place");
    }

    /**
     * sets a title both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setTitle(String oldValue, String newValue) {
        doc.replace("Title", oldValue, newValue);
    }

    /**
     * sets an election period both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setElectionPeriod(int oldValue, Integer newValue) {
        doc.replace("ElectionPeriod", oldValue, newValue);
    }

    /**
     * sets a date both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setDate(String oldValue, String newValue) {
        doc.replace("Date", oldValue, newValue);
    }

    /**
     * sets start time both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setBeginTime(String oldValue, String newValue) {
        doc.replace("Starttime", oldValue, newValue);
    }

    /**
     * sets end time both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setEndTime(String oldValue, String newValue) {
        doc.replace("Endtime", oldValue, newValue);
    }

    /**
     * sets index both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setIndex(int oldValue, Integer newValue) {
        doc.replace("Index", oldValue, newValue);
    }

    /**
     * sets place both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setPlace(String oldValue, String newValue) {
        doc.replace("Place", oldValue, newValue);
    }

    /**
     * @return PlenaryProtocolObj as a String
     */
    @Override
    public String toString() {
        return " Protocol's Title : " + getTitle() +
                "; \tProtocol's Index : " + getIndex() + "; \tElectionPeriod : " + getElectionPeriod()
                + ";     Date : " + getDate() + ";    BeginTime  : " + getBeginTime() +
                ";     EndTime : " + getEndTime() +
                ";     Place : " + getPlace() + "\n";
    }


}