package uni.project.all.classes;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uni.project.all.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * This class stores Fraction objects and acts as a parent class for most classes
 *
 * @author Siamak Choromkheirabadi
 */
public class PlenaryProtocol_File_Impl extends Protocol_File_Impl implements PlenaryProtocol {


    //object declaration
    private File file;
    private int ind = -1;
    private String date;
    private String beginTime;
    private String endTime;
    private String title;
    private String place;
    private XPath xPath = XPathFactory.newInstance().newXPath();
    private Document doc;

    private List<AgendaItem> agendaStuff = new ArrayList<>(0);


    /**
     * constructor which creates PlenaryProtocol_File_Impl objects and has parameters info and file
     *
     * @param info
     * @param file
     */
    public PlenaryProtocol_File_Impl(InfoGetter info, File file) {
        super(info);
        try {
            apply(file);
        } catch (ParserConfigurationException | IOException | SAXException | ParseException |
                 XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    /**
     * constructor
     */
    public PlenaryProtocol_File_Impl() {

    }


    /**
     * This method mainly adds info to our PlenaryProtocol Object and passes
     * at the end the edited node to AgendaItem Class for further work
     *
     * @param file
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws ParseException
     * @throws XPathExpressionException
     */
    private void apply(File file) throws ParserConfigurationException, IOException, SAXException, ParseException, XPathExpressionException {

        //the main Obj for parsing xml files will be created
        DocumentBuilderFactory docBFac = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docBFac.newDocumentBuilder();
        //takes the passed xml file
        Document someDocument = docBuilder.parse(file);
        this.file = file;
        this.doc = someDocument;

        //finds ElectionPeriod
        String period = xmlNodes(someDocument, "/dbtplenarprotokoll/@wahlperiode");
        this.setElectionPeriod(Integer.parseInt(period));

        String sessionNum = xmlNodes(someDocument, "/dbtplenarprotokoll/@sitzung-nr");
        this.setIndex(Integer.parseInt(sessionNum));

        String title = xmlNodes(someDocument, "/dbtplenarprotokoll/vorspann/kopfdaten/plenarprotokoll-nummer");
        this.setTitle(title);

        if (getTitle().contains("\n")) {
            setTitle(getTitle().replace("\n", ""));

        }
        if (getTitle().contains("\\n")) {
            setTitle(getTitle().replace("\\n", ""));

        }
        if (getTitle().contains("\\t")) {
            setTitle(getTitle().replace("\\t", ""));

        }
        if (getTitle().contains("\t")) {
            setTitle(getTitle().replace("\t", " "));

        }
        if (getTitle().contains("                    ")) {
            setTitle(getTitle().replace("                    ", " "));

        }
        if (getTitle().contains("  ")) {
            setTitle(getTitle().replace("  ", ""));

        }


        String loc = xmlNodes(someDocument, "/dbtplenarprotokoll/vorspann/kopfdaten/veranstaltungsdaten/ort");
        this.setPlace(loc);

        //Date will be found as a String
        String date = xmlNodes(someDocument, "/dbtplenarprotokoll/@sitzung-datum");
        this.setDate(date);


        String stTime = xmlNodes(someDocument, "/dbtplenarprotokoll/@sitzung-start-uhrzeit");
        if (stTime != null) {
            this.setBeginTime(stTime);
        }

        String endTime = xmlNodes(someDocument, "/dbtplenarprotokoll/@sitzung-ende-uhrzeit");
        if (endTime != null) {
            this.setEndTime(endTime);
        }


        //a list of nodes which have the name of ivz-block will be created
        String xmlPath = "/dbtplenarprotokoll/vorspann/inhaltsverzeichnis/ivz-block";

        NodeList blocks = (NodeList) xPath.compile(xmlPath).evaluate(doc, XPathConstants.NODESET);

        for (int b = 0; b < blocks.getLength(); b++) {
            Node item = blocks.item(b);


            //an AgendaItem Object will be created (which is being modified in the next class)
            AgendaItem theChosen = new AgendaItem_File_Impl(this, item);
            if (!(theChosen.getSpeeches().isEmpty())) {
                this.addAgendaItem(theChosen);

            }

        }

    }


    /**
     * @return the file object saved as the attribute in this class
     */
    public File importOriginalFile() {
        return this.file;
    }


    /**
     * XPath factory !
     *
     * @param doc
     * @param tag
     * @return a String which is processed after a node is applied in the xPath function
     * @throws XPathExpressionException
     */
    private String xmlNodes(Document doc, String tag) throws XPathExpressionException {
        String obj = (String) xPath.compile(tag).evaluate(doc, XPathConstants.STRING);
        return obj;
    }

    /**
     * @return the protocol index
     */
    @Override
    public int getIndex() {
        return this.ind;
    }

    /**
     * sets a new protocol index
     *
     * @param indx
     */
    @Override
    public void setIndex(int indx) {
        this.ind = indx;
    }

    /**
     * @return protocol's date
     */
    @Override
    public String getDate() {
        return this.date;
    }

    /**
     * adds a new protocol-date
     *
     * @param date
     */
    @Override
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the protocol's start-time
     */
    @Override
    public String getBeginTime() {
        return this.beginTime;
    }

    /**
     * adds a new start-time for the protocol
     *
     * @param bt
     */
    @Override
    public void setBeginTime(String bt) {
        this.beginTime = bt;
    }

    /**
     * @return an end-time for the protocol
     */
    @Override
    public String getEndTime() {
        return this.endTime;
    }

    /**
     * adds a new end-time for the protocol
     *
     * @param et
     */
    @Override
    public void setEndTime(String et) {
        this.endTime = et;
    }

    /**
     * @return protocol's title
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * adds a new title for the protocol
     *
     * @param title
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the list of AgendaItems (objects)
     */
    @Override
    public List<AgendaItem> getAgendaItems() {
        return this.agendaStuff;
    }

    /**
     * adds a new AgendaItem-Object to a specific list
     *
     * @param item
     */
    @Override
    public void addAgendaItem(AgendaItem item) {
        this.agendaStuff.add(item);
    }

    /**
     * adds a list of AgendaItems to a specific list (list in list but the lists will be united)
     *
     * @param items
     */
    @Override
    public void addAgendaItems(Set<AgendaItem> items) {
        items.forEach(this::addAgendaItem);
    }

    /**
     * @return the protocol's place
     */
    @Override
    public String getPlace() {
        return this.place;
    }

    /**
     * adds a new place for the protocol
     *
     * @param place
     */
    @Override
    public void setPlace(String place) {
        this.place = place;
    }

    /**
     * @return a list of SpeakerObjects (that are of course related to this protocol)
     */
    @Override
    public Set<Speaker> getSpeakers() {
        Set<Speaker> SetOfSpeakers = new HashSet<>(0);

        getAgendaItems().
                forEach(y -> y.getSpeeches().forEach(speech -> SetOfSpeakers.
                        add(speech.getSpeaker())));

        return SetOfSpeakers;
    }

    /**
     * @param ps
     * @return a set of Speakers that have a specific party
     */
    @Override
    public Set<Speaker> getSpeakers(Party ps) {
        return getSpeakers().stream().
                filter(s -> s.getParty().
                        equals(ps)).collect(Collectors.toSet());
    }

    /**
     * @param fs
     * @return a list of speakers that have  a specific fraction
     */
    @Override
    public Set<Speaker> getSpeakers(Fraction fs) {
        return getSpeakers().stream().
                filter(s -> s.getFraction().
                        equals(fs)).collect(Collectors.toSet());
    }

    /**
     * @return a list of speakers that are leaders (not used in our program)
     */
    @Override
    public Set<Speaker> getLeaders() {

        Set<Speaker> setOfSpeakers = new HashSet<>(0);
        this.getAgendaItems().
                forEach(x -> x.getSpeeches().
                        forEach(s -> s.getSpeeches().forEach(i -> {
                            if (i.getSpeaker().isLeader()) {
                                setOfSpeakers.add(i.getSpeaker());
                            }
                        })));

        return setOfSpeakers;
    }

    /**
     * @return the protocol's index, date and place if the raw protocol object is called
     */
    @Override
    public String toString() {
        return this.getIndex() + "    " + this.getDate() + "    " + this.getPlace();
    }

    /**
     * @param o
     * @return a boolean which checks if the hashCodes are equal
     */
    @Override
    public boolean equals(Object o) {
        return this.hashCode() == o.hashCode();
    }


    /**
     * @return an integer indicating the index of the protocol
     */
    @Override
    public int hashCode() {
        return this.getIndex();
    }


    /**
     * @param protocol the object to be compared.
     * @return an integer which checks the date of given protocols
     */
    @Override
    public int compareTo(Protocol protocol) {
        if (protocol instanceof PlenaryProtocol) {
            return ((PlenaryProtocol) protocol).getDate().compareTo(this.getDate());
        }
        return super.compareTo(protocol);
    }
}
