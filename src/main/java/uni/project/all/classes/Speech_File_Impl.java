package uni.project.all.classes;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uni.project.all.*;
import uni.project.parser.XMLParser;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;


/**
 * This class eventually saves speeches and passes info to other classes for further
 * additions.
 *
 * @author Siamak Choromkheirabadi
 */
public class Speech_File_Impl extends Protocol_File_Impl implements Speech {

    //declaring variables
    private AgendaItem agendaItem;
    private List<Comment> content = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    private List<Speech> speeches = new ArrayList<>();

    private Speaker speaker = null;
    private Speaker speaker_copy = null;

    /**
     * constructor
     */
    public Speech_File_Impl() {

    }


    /**
     * takes an object from AgendaItem and an edited node (which is being iterated in previous class!)
     *
     * @param agendaItem
     * @param node
     * @throws XPathExpressionException
     */
    public Speech_File_Impl(AgendaItem agendaItem, Node node) throws XPathExpressionException {
        super(agendaItem.getProtocol().getInfo());
        this.agendaItem = agendaItem;
        //A speech_File_Impl object is being passes to the 'addSpeech' method
        this.agendaItem.addSpeech(this);
        apply(node);

    }


    /**
     * constructor
     *
     * @param agendaItem
     * @param speechID
     */
    public Speech_File_Impl(AgendaItem agendaItem, String speechID) {
        super(agendaItem.getProtocol().getInfo());
        this.agendaItem = agendaItem;
        this.setID(speechID);
    }


    /**
     * basically adds half the information to different lists in different classes
     *
     * @param node
     * @throws XPathExpressionException
     */
    private void apply(Node node) throws XPathExpressionException {
        //content of the attribute called ID will be stored as ID
        this.setID(node.getAttributes().getNamedItem("id").getTextContent());


        //a list of children nodes from the 'node'
        NodeList nodeList = node.getChildNodes();

        Speaker liveSpeaker = null;
        Speech liveSpeech = this;

        int extra = 1;

        for (int a = 0; a < nodeList.getLength(); a++) {
            //iterating children nodes
            Node n = nodeList.item(a);

            switch (n.getNodeName()) {

                case "p":

                    String cat = "";
                    if (n.hasAttributes()) {
                        //content of the attribute 'klasse' is being shown
                        cat = n.getAttributes().getNamedItem("klasse").getTextContent();
                    }

                    switch (cat) {
                        case "redner":
                            //nodes which have the name 'redner' will be gathered in  a list
                            Speaker speaker = this.getInfo().getSpeaker(XMLParser.getXMLNode(n, "redner"));
                            liveSpeaker = speaker;
                            this.speaker_copy = speaker;
                            liveSpeech = this;
                            //Speech_File_Impl object will be passed
                            speaker.addSpeech(liveSpeech);
                            this.speaker = speaker;

                            break;
                        case "n":
                            //saves the speaker name and also update the speechObj
                            Speaker speaker2 = this.getInfo().getSpeaker(n);
                            liveSpeaker = speaker2;
                            this.speaker_copy = speaker2;
                            speaker2.addSpeech(liveSpeech);

                            break;

                        default:
                            //saves the text in a list
                            liveSpeech.addText(new Comment_File_Impl(liveSpeaker, liveSpeech, n.getTextContent()));
                    }

                    break;

                case "name":
                    //saves the name of speaker; also speechObj and SpeakerObj are updated
                    Speaker speaker3 = this.getInfo().getSpeaker(n);
                    if (speaker3 == this.getSpeaker()) {
                        liveSpeech = this;
                    } else {
                        if (liveSpeaker != speaker3 && speaker3 != null) {
                            liveSpeaker = speaker3;
                            this.speaker_copy = speaker3;
                            liveSpeech = new Speech_File_Impl(getAgendaItem(), getID() + "-" + extra);
                            liveSpeaker.addSpeech(liveSpeech);
                            liveSpeech.setSpeaker(liveSpeaker);
                            speeches.add(liveSpeech);
                            extra++;
                        }
                    }

                    break;

                case "kommentar":
                    Comment_File_Impl commentInSpeech = new Comment_File_Impl(n);
                    //saves comment to texts
                    content.add(commentInSpeech);
                    Comment_File_Impl comment = new Comment_File_Impl(n, this, speaker_copy);
                    //saves comment to a specific list (only comments)
                    comments.add(comment);
                    break;

            }

        }


    }

    /**
     * @return AgendaItem Object
     */
    @Override
    public AgendaItem getAgendaItem() {
        return this.agendaItem;
    }


    /**
     * @return a list of comments
     */
    @Override
    public List<Comment> getComments() {

        return comments;
    }


    /**
     * @return The text with comments inclusive (raw text)
     */
    @Override
    public String getText() {
        //structures the raw text which contains text and comments to be organized
        StringBuilder stBuilder = new StringBuilder();
        content.forEach(t -> {
            if (stBuilder.length() > 0) {
                stBuilder.append(" ");
            }
            if (t != null) {
                stBuilder.append("");
                stBuilder.append("   ").append(t.getContent());
                stBuilder.append("");
            } else {
                stBuilder.append(t.getContent());
            }


        });
        return stBuilder.toString();
    }

    /**
     * @return the protocol object
     */
    @Override
    public PlenaryProtocol getProtocol() {
        return this.getAgendaItem().getProtocol();
    }

    /**
     * @return the speaker object
     */
    @Override
    public Speaker getSpeaker() {
        return this.speaker;
    }

    /**
     * sets the speaker obj
     *
     * @param speaker
     */
    @Override
    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    /**
     * @return the length of the text
     */
    @Override
    public int getLength() {
        return getText().length();
    }

    /**
     * @return the list of speech objects
     */
    @Override
    public List<Speech> getSpeeches() {
        return speeches;
    }

    /**
     * adds the text to a specific list
     *
     * @param text
     */
    @Override
    public void addText(Comment text) {
        this.content.add(text);
    }

    /**
     * @return AgendaItem infos if the raw (Speech) object is called
     */
    @Override
    public String toString() {
        return agendaItem.toString();
    }

}
