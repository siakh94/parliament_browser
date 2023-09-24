package uni.project.all.classes.mongoclasses;

import org.bson.Document;
import uni.project.all.classes.AgendaItem_File_Impl;

/**
 * An additional AgendaItem class which inherits AgendaItem_File_Impl
 * and stores mainly an AgendaItem's document (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class AgendaItem_MongoDB_Impl extends AgendaItem_File_Impl {
    private Document doc;

    /**
     * Constructor
     *
     * @param doc
     */
    public AgendaItem_MongoDB_Impl(Document doc) {
        this.doc = doc;
    }

    /**
     * @return title as a string
     */
    @Override
    public String getTitle() {
        return doc.getString("Title");
    }

    /**
     * @return protocol's index as a string
     */
    public String getProtocolIndex() {
        return doc.getString("ProtocolID");
    }


    /**
     * sets Title both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setTitle(String oldValue, String newValue) {
        doc.replace("Title", oldValue, newValue);
    }

    /**
     * sets protocol's index both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setProtocolIndex(String oldValue, String newValue) {
        doc.replace("ProtocolID", oldValue, newValue);
    }


    /**
     * @return AgendaItem's Object as a String
     */
    @Override
    public String toString() {
        return " Protocol's Index : " + getProtocolIndex() +
                "; \t AgendaItem's Title : " + getTitle() + "\n";
    }
}