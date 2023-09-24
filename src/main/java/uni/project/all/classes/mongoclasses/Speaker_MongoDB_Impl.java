package uni.project.all.classes.mongoclasses;

import org.bson.Document;
import uni.project.all.classes.Speaker_File_Impl;


/**
 * An additional Speaker class which inherits Speakers
 * and stores mainly a speaker's document (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class Speaker_MongoDB_Impl extends Speaker_File_Impl {


    private Document doc;


    /**
     * constructor
     *
     * @param doc
     */
    public Speaker_MongoDB_Impl(Document doc) {
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
     * @return firstname as a String
     */
    @Override
    public String getFirstName() {
        return doc.getString("FirstName");
    }

    /**
     * @return lastname as a String
     */
    @Override
    public String getName() {
        return doc.getString("LastName");
    }


    /**
     * @return fractionName as a String
     */
    public String getFractionName() {
        return doc.getString("Fraction");
    }


    /**
     * @return PartyName as a String
     */
    public String getPartyName() {
        return doc.getString("Party");
    }

    /**
     * @return role as a String
     */
    @Override
    public String getRole() {
        return doc.getString("Role");
    }

    /**
     * @return average length as a double
     */
    @Override
    public double getAvgLength() {
        return doc.getDouble("Speaker's Speech-Length");
    }

    /**
     * @return if a Speaker is leader as a boolean (true or false)
     */
    @Override
    public boolean isLeader() {
        return doc.getBoolean("IsLeader");
    }

    /**
     * @return birthday as a String
     */
    @Override
    public String getBirthday() {
        return doc.getString("Birthday");
    }

    /**
     * @return death date as a String
     */
    @Override
    public String getDeath() {
        return doc.getString("Death");
    }

    /**
     * @return place of birth as a String
     */
    @Override
    public String getBirthPlace() {
        return doc.getString("PlaceOfBirth");
    }

    /**
     * @return sex as a String
     */
    @Override
    public String getSex() {
        return doc.getString("SEX");
    }

    /**
     * @return marital status as a String
     */
    @Override
    public String getMaritalStatus() {
        return doc.getString("MaritalStatus");
    }

    /**
     * @return religion as a String
     */
    @Override
    public String getReligion() {
        return doc.getString("Religion");
    }

    /**
     * @return academic title as a String
     */
    @Override
    public String getAcTitle() {
        return doc.getString("AcademicTitle");
    }

    /**
     * @return occupation as a String
     */
    @Override
    public String getOccupation() {
        return doc.getString("Occupation");
    }

    /**
     * @return ID as a String
     */
    @Override
    public String getID() {
        return doc.getString("_id");
    }

    /**
     * sets firstname both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setFirstName(String oldValue, String newValue) {
        doc.replace("FirstName", oldValue, newValue);
    }

    /**
     * sets title both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setTitle(String oldValue, String newValue) {
        doc.replace("Title", oldValue, newValue);
    }

    /**
     * sets lastname both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setLastName(String oldValue, String newValue) {
        doc.replace("LastName", oldValue, newValue);
    }

    /**
     * sets fraction both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setFraction(String oldValue, String newValue) {
        doc.replace("Fraction", oldValue, newValue);
    }

    /**
     * sets party both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setParty(String oldValue, String newValue) {
        doc.replace("Party", oldValue, newValue);
    }

    /**
     * sets role both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setRole(String oldValue, String newValue) {
        doc.replace("Role", oldValue, newValue);
    }

    /**
     * sets average length both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setAvgLength(double oldValue, Double newValue) {
        doc.replace("Speaker's Speech-Length", oldValue, newValue);
    }

    /**
     * sets leader both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setLeader(boolean oldValue, Boolean newValue) {
        doc.replace("IsLeader", oldValue, newValue);
    }

    /**
     * sets birthday both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setBirthday(String oldValue, String newValue) {
        doc.replace("Birthday", oldValue, newValue);
    }

    /**
     * sets death date both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setDeath(String oldValue, String newValue) {
        doc.replace("Death", oldValue, newValue);
    }

    /**
     * sets place of birth both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setBirthPlace(String oldValue, String newValue) {
        doc.replace("PlaceOfBirth", oldValue, newValue);
    }

    /**
     * sets sex both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setSex(String oldValue, String newValue) {
        doc.replace("SEX", oldValue, newValue);
    }

    /**
     * sets maritial status both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setMaritalStatus(String oldValue, String newValue) {
        doc.replace("MaritalStatus", oldValue, newValue);
    }

    /**
     * sets religion both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setReligion(String oldValue, String newValue) {
        doc.replace("Religion", oldValue, newValue);
    }

    /**
     * sets academic title both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setAcademicTitle(String oldValue, String newValue) {
        doc.replace("AcademicTitle", oldValue, newValue);
    }

    /**
     * sets occupation both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setOccupation(String oldValue, String newValue) {
        doc.replace("Occupation", oldValue, newValue);
    }

    /**
     * sets ID both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setID(String oldValue, String newValue) {
        doc.replace("_id", oldValue, newValue);
    }


    /**
     * @return SpeakerObj as a String
     */
    @Override
    public String toString() {
        return "Speaker's ID : " + getID() + ";\t Title : " + getTitle() + " ;  FirstName : " + getFirstName() +
                ";  Name : " + getName() + ";     Fraction : " + getFractionName() + ";    Party : " + getPartyName() +
                ";    Role : " + getRole() + ";    AvgSpeechLength() : " + getAvgLength() +
                ";    IsLeader ? : " + isLeader() + ";     Birthday : " + getBirthday() + ";    DeathDate : " + getDeath() +
                ";    Place of Birth : " + getBirthPlace() + ";     Sex : " + getSex() +
                ";    MaritalStatus : " + getMaritalStatus() + ";    Religion : " + getReligion() +
                ";     AcTitle : " + getAcTitle() + ";      Occupation : " + getOccupation() + "\n";
    }

}