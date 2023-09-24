package uni.project.all.classes.mongoclasses;

import org.bson.Document;
import uni.project.all.classes.Party_File_Impl;

import java.util.List;

/**
 * An additional Party class which inherits Parties
 * and stores mainly a PartyDocument (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class Party_MongoDB_Impl extends Party_File_Impl {
    private Document doc;

    /**
     * Constructor
     *
     * @param doc
     */
    public Party_MongoDB_Impl(Document doc) {
        this.doc = doc;
    }

    /**
     * @return PartyName as a string
     */
    public String getParty() {
        return doc.getString("Party");
    }


    /**
     * @return PartyMembers as a set.
     */
    public List<String> getPartyMembers() {
        return doc.getList("Party-Members", String.class);
    }

    /**
     * sets a Party both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setParty(String oldValue, String newValue) {
        doc.replace("Party", oldValue, newValue);
    }

    /**
     * @return The partyId
     */
    public Integer getPartyID() {
        return doc.getInteger("_id");
    }


    /**
     * @return PartyObj as a String
     */
    @Override
    public String toString() {
        return "Party : " + getParty() + ";  \t  PartyMembers : " + getPartyMembers() + "\n";
    }
}