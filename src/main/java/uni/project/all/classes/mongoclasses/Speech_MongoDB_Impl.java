package uni.project.all.classes.mongoclasses;

import org.bson.Document;
import uni.project.all.classes.Speech_File_Impl;

/**
 * An additional Speaker class which inherits Speakers
 * and stores mainly a speaker's document (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class Speech_MongoDB_Impl extends Speech_File_Impl {

    protected Document doc;

    /**
     * constructor
     *
     * @param doc
     */
    public Speech_MongoDB_Impl(Document doc) {
        this.doc = doc;
    }

    /**
     * @return Text as a String
     */
    @Override
    public String getText() {
        return doc.getString("Speech");
    }

    /**
     * @return SpeechID as a String
     */
    public String getSpeechID() {
        return doc.getString("_id");
    }

    /**
     * sets SpeechID both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setSpeechID(String oldValue, String newValue) {
        doc.replace("_id", oldValue, newValue);
    }


    /**
     * @return firstname as a String
     */
    public String getSFirstname() {
        return doc.getString("Speaker's Firstname");
    }

    /**
     * @return lastname as a String
     */
    public String getSLastname() {
        return doc.getString("Speaker's Lastname");
    }

    /**
     * @return Protocol's index as a String
     */
    public String getProtocolIndex() {
        return doc.getString("ProtocolID");
    }

    /**
     * sets Text both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setText(String oldValue, String newValue) {
        doc.replace("Speech", oldValue, newValue);
    }

    /**
     * sets first name both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setFirstname(String oldValue, String newValue) {
        doc.replace("Speaker's Firstname", oldValue, newValue);
    }

    /**
     * sets lastname both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setLastname(String oldValue, String newValue) {
        doc.replace("Speaker's Lastname", oldValue, newValue);
    }

    /**
     * sets protocol index both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setProtocolIndex(String oldValue, String newValue) {
        doc.replace("ProtocolID", oldValue, newValue);
    }

    /**
     * @return Speech Document
     */
    public Document getDoc() {
        return doc;
    }

    /**
     * sets a SpeechDocument
     *
     * @param document
     */
    public void setDoc(Document document) {
        this.doc = document;

    }


    /**
     * @return a speechObj as a String
     */
    @Override
    public String toString() {
        return "SpeechID : " + getSpeechID() + "\tProtocol's Index : " + getProtocolIndex() + ";  \t FIRSTNAME : "
                + getSFirstname() +
                ";  LASTNAME : " + getSLastname() + ";\t Text : " + getText() + "\n";
    }
}
