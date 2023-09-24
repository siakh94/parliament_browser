package uni.project.all.classes.mongoclasses;

import org.bson.Document;
import uni.project.all.classes.Fraction_File_Impl;

import java.util.List;

/**
 * An additional Fraction class which inherits fractions
 * and stores mainly a fraction's document (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class Fraction_MongoDB_Impl extends Fraction_File_Impl {

    private Document doc;

    /**
     * Constructor
     *
     * @param doc
     */
    public Fraction_MongoDB_Impl(Document doc) {
        this.doc = doc;
    }

    /**
     * @return FractionName as String
     */
    public String getFraction() {
        return doc.getString("Fraction");
    }

    /**
     * @return FractionMembers as a list
     */
    public List<String> getFractionMembers() {
        return doc.getList("Fraction-Members", String.class);
    }

    /**
     * sets a Fraction both in MongoDB and internally (used for updating and deleting)
     *
     * @param oldValue
     * @param newValue
     */
    public void setFraction(String oldValue, String newValue) {
        doc.replace("Fraction", oldValue, newValue);
    }

    /**
     * @return FractionID as an Integer
     */
    public Integer getFractionID() {
        return doc.getInteger("_id");
    }


    /**
     * @return This method returns infos from a Fraction-Object
     */
    @Override
    public String toString() {
        return "Fraction : " + getFraction() + "; \t   Members : " + getFractionMembers() + "\n";
    }


}