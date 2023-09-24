package uni.project.all.classes.mongoclasses;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * An additional Speaker class which inherits Speakers
 * and stores mainly a speaker's document (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class Speech_MongoDB_NLP extends Speech_MongoDB_Impl {

    /**
     * constructor
     *
     * @param doc
     */
    public Speech_MongoDB_NLP(Document doc) {
        super(doc);

    }

    /**
     * @return SpeechID as a String
     */
    @Override
    public String getSpeechID() {
        return doc.getString("_id");
    }


    /**
     * @return average sentiment as a double
     */
    public Double getAvgSentiment() {
        return doc.getDouble("Avg-Sentiment");
    }

    /**
     * @return persons as a list
     */
    public List<String> getPersons() {
        return doc.getList("Persons", String.class);
    }

    /**
     * @return Location as a list
     */
    public List<String> getLocations() {
        return doc.getList("Locations", String.class);
    }

    /**
     * @return organisations as a list
     */
    public List<String> getOrganisations() {
        return doc.getList("Organisations", String.class);
    }

    /**
     * @return SpeechNLP Object as a document
     */
    @Override
    public Document getDoc() {
        return doc;
    }


    /**
     * @return a SpeechNLP Document as a String
     */
    @Override
    public String toString() {
        return "\nPersons : " + getPersons() + "\tLocations : " + getLocations() +
                "\tOrganisations : " + getOrganisations() + "\tAvg-Sentiment : " + getAvgSentiment() + "\n";
    }


}
