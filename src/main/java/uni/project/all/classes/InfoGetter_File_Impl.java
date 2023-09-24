package uni.project.all.classes;


import org.w3c.dom.Node;
import uni.project.all.*;
import uni.project.mongodb.database.MongoDBConnectionHandler;
import uni.project.mongodb.database.MongoToWeb;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This is one of the most important classes in this project and acts
 * exactly like a bridge between MainClass, other Classes and MongoDB.
 *
 * @author Siamak Choromkheirabadi
 */
public class InfoGetter_File_Impl implements InfoGetter {

    private Set<Speaker> speakers = new HashSet<>();
    private Set<PlenaryProtocol> protocols = new HashSet<>();
    private Set<Fraction> fractions = new HashSet<>();
    private Set<Party> parties = new HashSet<>();
    private MongoDBConnectionHandler mch = new MongoDBConnectionHandler();
    private MongoToWeb mongoToWeb;

    private List<Speaker> mongoSpeakers = new ArrayList<>();
    private List<PlenaryProtocol> mongoProtocols = new ArrayList<>();
    private List<Fraction> mongoFractions = new ArrayList<>();
    private List<Party> mongoParties = new ArrayList<>();
    private List<Speech> mongoSpeeches = new ArrayList<>();
    private List<Comment> mongoComments = new ArrayList<>();
    private List<AgendaItem> mongoAgendaItems = new ArrayList<>();
    private List<Speech> nlpSpeeches = new ArrayList<>();
    private List<Comment> nlpComments = new ArrayList<>();


    /**
     * constructor
     */
    public InfoGetter_File_Impl() {

    }


    /**
     * adds a Protocol object to a specific list
     *
     * @param protocol
     */
    @Override
    public void addMongoProtocol(PlenaryProtocol protocol) {
        this.mongoProtocols.add(protocol);
    }

    /**
     * adds a fraction object to a specific list
     *
     * @param fraction
     */
    @Override
    public void addMongoFraction(Fraction fraction) {
        this.mongoFractions.add(fraction);
    }

    /**
     * adds a party object to a specific list
     *
     * @param party
     */
    @Override
    public void addMongoParty(Party party) {
        this.mongoParties.add(party);
    }

    /**
     * adds a Speaker object to a specific list
     *
     * @param speaker
     */
    @Override
    public void addMongoSpeaker(Speaker speaker) {
        this.mongoSpeakers.add(speaker);
    }

    /**
     * adds a speech object to a specific list
     *
     * @param speech
     */
    @Override
    public void addMongoSpeech(Speech speech) {
        this.mongoSpeeches.add(speech);
    }

    /**
     * adds an AgendaItem object to a specific list
     *
     * @param ai
     */
    @Override
    public void addMongoAgendaItem(AgendaItem ai) {
        this.mongoAgendaItems.add(ai);
    }

    /**
     * adds a comment object to a specific list
     *
     * @param comment
     */
    @Override
    public void addMongoComment(Comment comment) {
        this.mongoComments.add(comment);
    }


    /**
     * @return the MongoProtocol-list
     */
    @Override
    public List<PlenaryProtocol> getMongoProtocols() {
        return this.mongoProtocols;
    }

    /**
     * adds a comment object to a specific list
     *
     * @param comment
     */
    public void addNLPComment(Comment comment) {
        this.nlpComments.add(comment);
    }

    /**
     * @return The NLP-Comment-list
     */
    public List<Comment> getNLPComments() {
        return this.nlpComments;
    }

    /**
     * adds a Speech object to a specific list
     *
     * @param speech
     */
    public void addNLPSpeech(Speech speech) {
        this.nlpSpeeches.add(speech);
    }

    /**
     * @return The NLP-Speech-list
     */
    public List<Speech> getNLPSpeeches() {
        return this.nlpSpeeches;
    }


    /**
     * @return The MongoSpeaker-list
     */
    @Override
    public List<Speaker> getMongoSpeakers() {
        return this.mongoSpeakers;
    }

    /**
     * @return The MongoFraction-list
     */
    @Override
    public List<Fraction> getMongoFractions() {
        return this.mongoFractions;
    }

    /**
     * @return The MongoParty-list
     */
    @Override
    public List<Party> getMongoParties() {
        return this.mongoParties;
    }

    /**
     * @return The MongoSpeech-list
     */
    @Override
    public List<Speech> getMongoSpeeches() {
        return this.mongoSpeeches;
    }

    /**
     * @return The MongoAgendaItem-list
     */
    @Override
    public List<AgendaItem> getMongoAgendaItems() {
        return this.mongoAgendaItems;
    }

    /**
     * @return The MongoComment-list
     */
    @Override
    public List<Comment> getMongoComments() {
        return this.mongoComments;
    }

    /**
     * @return The Speaker-Objects as a set
     */
    @Override
    public Set<Speaker> getSpeakers() {
        return this.speakers;
    }

    /**
     * @return all protocol objects as a set
     */
    @Override
    public Set<PlenaryProtocol> getProtocols() {
        return this.protocols;
    }

    /**
     * adds a protocol object to a list
     *
     * @param protocol
     */
    @Override
    public void addProtocol(PlenaryProtocol protocol) {
        this.protocols.add(protocol);
    }

    /**
     * @return all fraction objects
     */
    @Override
    public Set<Fraction> getFractions() {
        return fractions;
    }

    /**
     * @return all party objects
     */
    @Override
    public Set<Party> getParties() {
        return this.parties;
    }

    /**
     * It adds the given party object to the party list if the item doesn't exist
     *
     * @param party
     * @return a party object after checking if the given party name already exists
     */
    @Override
    public Party getParty(String party) {
        List<Party> parList = this.getParties().stream().
                filter(s -> s.getName().equalsIgnoreCase(party)).
                collect(Collectors.toList());

        if (parList.size() == 1) {
            return parList.get(0);
        } else {
            Party someParty = new Party_File_Impl(party);
            this.parties.add(someParty);
            return someParty;
        }

    }

    /**
     * @param name
     * @return a speaker object after checking if the given name and its related id match
     */
    @Override
    public Speaker getSpeaker(String name) {

        List<Speaker> speakerList = this.getSpeakers().stream().
                filter(s -> s.getID().equals(name)).collect(Collectors.toList());

        if (speakerList.size() == 1) {
            return speakerList.get(0);
        }

        return null;

    }


    /**
     * @param speakerName
     * @return a speaker object after checking if the given name already exists
     */
    public Speaker getSpeakerByName(String speakerName) {

        List<Speaker> speakerList = this.getSpeakers().stream().
                filter(s -> s.getName().
                        equalsIgnoreCase(Speaker_File_Impl.edit(speakerName))).
                collect(Collectors.toList());

        if (speakerList.size() == 1) {
            return speakerList.get(0);
        }
        return null;

    }

    /**
     * adds a speaker object if it doesn't exist
     *
     * @param node
     * @return a speaker object after checking if the object already exists
     * @throws XPathExpressionException
     */
    @Override
    public Speaker getSpeaker(Node node) throws XPathExpressionException {

        Speaker spObj;

        if (!node.getNodeName().equalsIgnoreCase("name")) {
            String someID = node.getAttributes().getNamedItem("id").getTextContent();

            spObj = getSpeaker(someID);

            if (spObj == null) {
                Speaker_File_Impl speakerObj = new Speaker_File_Impl(this, node);
                this.speakers.add(speakerObj);
                spObj = speakerObj;
            }
        } else {
            spObj = getSpeakerByName(node.getTextContent());

            if (spObj == null) {
                Speaker_File_Impl spObj2 = new Speaker_File_Impl(this);
                spObj2.setName(node.getTextContent());

                this.speakers.add(spObj2);
                spObj = spObj2;
            }

        }

        return spObj;
    }

    /**
     * @param name
     * @return a fraction object after checking if the name already exists.
     */

    @Override
    public Fraction getFraction(String name) {

        List<Fraction> fracList = this.getFractions().stream().filter(s -> {
            if (s.getName().startsWith(name.substring(0, 3))) {
                return true;
            }
            return s.getName().equalsIgnoreCase(name.trim());
        }).collect(Collectors.toList());

        if (fracList.size() == 1) {
            return fracList.get(0);
        }

        return null;
    }

    /**
     * adds a new fraction object if the given node doesn't match any objects
     *
     * @param node
     * @return a fraction object after checking if the given node matches the related object
     */
    @Override
    public Fraction getFraction(Node node) {
        String fracName = node.getTextContent();

        Fraction fracObj = getFraction(fracName);

        if (fracObj == null) {
            fracObj = new Fraction_File_Impl(node);
            this.fractions.add(fracObj);
        }

        return fracObj;
    }

    /**
     * This is like a bridge between the MainClass and MongoDBConnectionHandler class
     * it calls the connection() in MongoDBConnectionHandler class
     */
    public MongoDBConnectionHandler connection() {
        mch.connection();
        return mch;
    }

    /**
     * @return a MongoToWeb object
     */
    public MongoToWeb getMongoToWeb() {
        this.mongoToWeb = new MongoToWeb(mch, this);
        return mongoToWeb;
    }


    /**
     * Passes the PlenaryProtocol Object to MongoDBConnectionHandler (MCH) so
     * that other methods get to know the informative Object !
     *
     * @param protocol
     */
    public void mongoDirection(PlenaryProtocol protocol) {
        mch.mongoProtocol(protocol);
    }

    /**
     * calls the mongoFractions method in MCH
     *
     * @param f
     */
    public void allFractions(Fraction f) {
        mch.mongoFractions(f);
    }

    /**
     * calls the mongoParties method in MCH
     *
     * @param p
     */
    public void allParties(Party p) {
        mch.mongoParties(p);
    }

    /**
     * calls the mongoSpeakers method in MCH
     *
     * @param s
     */
    public void allSpeakers(Speaker s) {
        mch.mongoSpeakers(s);

    }


    /**
     * calls the lemmata method in MCH
     *
     * @param col
     */
    public void nounsAndVerbs(String col) {
        mch.lemmata(col);
    }

    /**
     * calls the namedEntities method in MCH
     *
     * @param col
     */
    public void namedEntities(String col) {
        mch.entities(col);
    }

    /**
     * calls the sentis method in MCH
     *
     * @param col
     */
    public void sentiment(String col) {
        mch.sentis(col);

    }


}



