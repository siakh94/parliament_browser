package uni.project.all.classes.mongoclasses;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores all NLP-Comment results extra here.
 *
 * @author Siamak Choromkheirabadi
 */
public class Comment_MongoDB_NLP extends Comment_MongoDB_Impl {


    /**
     * Constructor
     *
     * @param doc
     */
    public Comment_MongoDB_NLP(Document doc) {
        super(doc);
    }

    /**
     * @return average sentiment of a comment
     */
    public Double getAvgSentiment() {
        return doc.getDouble("Avg-Sentiment");
    }

    /**
     * @return SpeechID
     */
    @Override
    public String getSpeechID() {
        return doc.getString("SpeechID");
    }

    /**
     * @return NLP-Comment doc
     */
    @Override
    public Document getDoc() {
        return doc;
    }

    /**
     * @return NLP-Comment as a string.
     */
    @Override
    public String toString() {
        return "SpeechID : " + getSpeechID() + "\tAvg-Sentiment : " + getAvgSentiment() + "\n";
    }


}
