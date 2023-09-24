package uni.project.all.classes;


import org.w3c.dom.Node;
import uni.project.all.Comment;
import uni.project.all.Speaker;
import uni.project.all.Speech;


/**
 * This class stores Comment objects
 *
 * @author Siamak Choromkheirabadi
 */
public class Comment_File_Impl implements Comment {
    //object declaration
    private Speaker speaker = null;
    private Speech speech = null;
    private String text = "";
    private String comment = "";


    /**
     * Constructor, which creates Comment_File_Impl Objects
     *
     * @param speaker
     * @param speech
     * @param text
     */
    public Comment_File_Impl(Speaker speaker, Speech speech, String text) {

        this.setSpeaker(speaker);
        this.setSpeech(speech);
        this.setText(text);

    }

    /**
     * Constructor
     */
    public Comment_File_Impl() {

    }

    /**
     * constructor
     *
     * @param node
     */
    public Comment_File_Impl(Node node) {
        this.text = node.getTextContent();
    }


    /**
     * constructor
     *
     * @param node
     * @param speech
     * @param speaker
     */
    public Comment_File_Impl(Node node, Speech speech, Speaker speaker) {
        this.comment = node.getTextContent();
        this.setSpeech(speech);
        this.setSpeaker(speaker);

    }

    @Override
    public Speech getSpeech() {
        return this.speech;
    }

    @Override
    public Speaker getSpeaker() {
        return this.speaker;
    }

    @Override
    public void setSpeech(Speech speech) {
        this.speech = speech;
    }

    @Override
    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public void setText(String text) {
        this.text = text;
    }


    @Override
    public String getContent() {
        return this.text;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public int hashCode() {
        return this.getContent().hashCode();
    }
}