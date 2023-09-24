package uni.project.all.classes.mongoclasses;

import org.bson.Document;
import uni.project.all.classes.Comment_File_Impl;

/**
 * An additional Comment class which inherits Comments
 * and stores mainly a Comment's document (bson) internally.
 *
 * @author Siamak Choromkheirabadi
 */
public class Comment_MongoDB_Impl extends Comment_File_Impl {

    protected Document doc;

    /**
     * Constructor
     *
     * @param doc
     */
    public Comment_MongoDB_Impl(Document doc) {
        this.doc = doc;
    }

    /**
     * @return comment document
     */
    public Document getDoc() {
        return doc;
    }

    /**
     * @param document
     */
    public void setDoc(Document document) {
        this.doc = document;

    }

    /**
     * @return Comment as a String
     */
    @Override
    public String getComment() {
        return doc.getString("Comment");
    }

    /**
     * @return Commentator as a String
     */
    public String getCommentator() {
        return doc.getString("Commentator");
    }

    /**
     * @return SpeechID as a String
     */
    public String getSpeechID() {
        return doc.getString("SpeechID");
    }

    /**
     * This method sets comments internally and in db. (not used !)
     *
     * @param oldValue
     * @param newValue
     */
    public void setComment(String oldValue, String newValue) {
        doc.replace("Comment", oldValue, newValue);
    }

    /**
     * This method sets Commentator internally and in db.
     *
     * @param oldValue
     * @param newValue
     */
    public void setCommentator(String oldValue, String newValue) {
        doc.replace("Commentator", oldValue, newValue);
    }

    /**
     * sets SpeechID internally and in db.
     *
     * @param oldValue
     * @param newValue
     */
    public void setSpeechID(String oldValue, String newValue) {
        doc.replace("SpeechID", oldValue, newValue);
    }

    /**
     * This method returns infos from a Comment-Object
     */
    @Override
    public String toString() {
        return "SpeechID : " + getSpeechID() + "; \t Comment :    " + getComment() + "; \t   COMMENTATOR : "
                + getCommentator() + "\n";
    }
}