package uni.project.all.classes;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uni.project.all.AgendaItem;
import uni.project.all.PlenaryProtocol;
import uni.project.all.Speech;
import uni.project.parser.XMLParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This Class stores AgendaItem Objects and mainly adds related data to those objs
 *
 * @author Siamak Choromkheirabadi
 */
public class AgendaItem_File_Impl extends Protocol_File_Impl implements AgendaItem {

    //variable declaration
    private PlenaryProtocol protocol;
    private String ind = "";
    private String title = "";
    private List<Speech> speechList = new ArrayList<>(0);
    private XPath xPath = XPathFactory.newInstance().newXPath();


    /**
     * Constructor which calls at the beginning the Constructor of Protocol_File_Impl
     * and the calls the apply()
     *
     * @param protocol
     * @param n
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public AgendaItem_File_Impl(PlenaryProtocol protocol, Node n) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        super(protocol.getInfo());
        this.protocol = protocol;
        apply(n);
    }

    /**
     * constructor
     */
    public AgendaItem_File_Impl() {

    }


    /**
     * This method mainly finds related data to AgendaItems and stores them to the
     * related objects
     *
     * @param n
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    private void apply(Node n) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {


        //the single nodes, which are being iterated from the previous class, are being
        //checked for the title
        String topIndex = xPath.compile("./ivz-block-titel").evaluate(n);
        if (topIndex != null) {
            this.setIndex(topIndex.replace(":", ""));
        }

        String title = xPath.compile("./ivz-eintrag/ivz-eintrag-inhalt").evaluate(n);
        if (title != null) {
            this.setTitle(title);
        }


        DocumentBuilderFactory docBFac = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = docBFac.newDocumentBuilder();
        Document originalDocument = docBuilder.parse(((PlenaryProtocol_File_Impl) getProtocol()).importOriginalFile());

        String xmlPath = "/dbtplenarprotokoll/sitzungsverlauf/tagesordnungspunkt";

        //all nodes with the name 'tagesordnungspunkt' will be listed
        NodeList blocks = (NodeList) xPath.compile(xmlPath).evaluate(originalDocument, XPathConstants.NODESET);

        for (int a = 0; a < blocks.getLength(); a++) {
            //those nodes are being iterated
            Node top = blocks.item(a);
            if (top.getAttributes().getNamedItem("top-id").getTextContent().equals(this.getIndex())) {
                //if the titles of the tops match, a new list of nodes will be created
                //and the nod with  the name of 'rede' will be searched
                List<Node> speechNodes = XMLParser.getXMLNodes(top, "rede");
                speechNodes.forEach(r -> {
                    try {
                        //those nodes are iterated and sent to the Speech Class for
                        //further work
                        Speech speechObj = new Speech_File_Impl(this, r);
                    } catch (XPathExpressionException e) {
                        throw new RuntimeException(e);
                    }
                });

            }
        }

    }

    /**
     * @return SpeechObjects in as list
     */
    @Override
    public List<Speech> getSpeeches() {
        return this.speechList;
    }

    /**
     * adds a Speech Object to the list
     *
     * @param sp
     */
    @Override
    public void addSpeech(Speech sp) {
        this.speechList.add(sp);
    }

    /**
     * adds speechObjects to a list
     *
     * @param sps
     */
    @Override
    public void addSpeeches(Set<Speech> sps) {
        this.speechList.addAll(sps);
    }

    /**
     * @return Agendatem's index as a String
     */
    @Override
    public String getIndex() {
        return this.ind;
    }

    /**
     * sets an AgendaItem's index
     *
     * @param indx
     */
    @Override
    public void setIndex(String indx) {
        this.ind = indx;
    }

    /**
     * @return AgendaItem's Title as a String
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /**
     * sets AgendaItem's title as a String
     *
     * @param title
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return a protocol object
     */
    @Override
    public PlenaryProtocol getProtocol() {
        return this.protocol;
    }

    /**
     * @return AgendaItem's Index as the object is called a whole
     */
    @Override
    public String toString() {
        return getIndex();
    }

    /**
     * @param o
     * @return a boolean which indicates if hashcodes are equal
     */
    @Override
    public boolean equals(Object o) {
        return hashCode() == o.hashCode();
    }

    /**
     * @return AgendaItem's index a an Integer (runs through hashcode first)
     */
    @Override
    public int hashCode() {
        return this.getIndex().hashCode();
    }
}
